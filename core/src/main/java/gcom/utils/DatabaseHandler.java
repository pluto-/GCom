package gcom.utils;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.*;

public class DatabaseHandler {

    private static final String configPath = "resources/config.properties";

    private final Host self;
    Session session;

    public void insertMessage(Message message) {
        String vectorClock = message.getVectorClock().toString();
        String text = message.getText();
        String beenAt = message.getBeenAt().toString();
        boolean isViewChange = false;
        if(message instanceof ViewChange) {
            isViewChange = true;
        }

        ResultSet resultSet = session.execute("SELECT * FROM messages WHERE vectorClock='" + vectorClock + "' AND group='" + message.getGroupName() + "';");
        if(!resultSet.iterator().hasNext()) {
            session.execute("INSERT INTO messages (vectorClock, message, senderAddress, senderPort, group, beenAt, addedBy, isViewChange) VALUES" +
                    "('" +
                    vectorClock + "','" +
                    text + "','" +
                    message.getSource().getAddress().getHostAddress() + "'," +
                    message.getSource().getPort() + ",'" +
                    message.getGroupName() + "','" +
                    beenAt + "','" +
                    self + "'," +
                    isViewChange + ");");

        }

    }

    public boolean hasMember(Host host, String groupName) {
        ResultSet resultSet = session.execute("SELECT * FROM members WHERE group = '" + groupName + "' AND hostAddress = '" + host.getAddress().getHostAddress() + "' AND hostPort =" + host.getPort());
        return resultSet.iterator().hasNext();
    }

    public void addMember(String groupName, Host member, VectorClock vectorClock, boolean isConnected) {
        session.execute("INSERT INTO members (group, hostAddress, hostPort, vectorClock, connected) VALUES" +
                "('" +
                groupName + "','" +
                member.getAddress().getHostAddress() + "'," +
                member.getPort() + ",'" +
                vectorClock.toString() + "'," +
                isConnected +
                ")");
    }

    public void updateMemberConnected(String groupName, Host member, boolean isConnected) {
        session.execute("UPDATE members SET connected=" + isConnected + " WHERE group='"+ groupName +"' AND hostAddress='" + member.getAddress().getHostAddress() + "' AND hostPort=" + member.getPort() + ";");
    }

    public void updateMemberVectorClock(String groupName, Host member, VectorClock vectorClock) {
        session.execute("UPDATE members SET vectorClock='" + vectorClock.toString() + "' WHERE group='"+ groupName +"' AND hostAddress='" + member.getAddress().getHostAddress() + "' AND hostPort=" + member.getPort() + ";");
    }

    public Host getLeader(String groupName) throws UnknownHostException {
        ResultSet resultSet = session.execute("SELECT * FROM groups WHERE groupName='"+ groupName +"';");
        Iterator<Row> iterator = resultSet.iterator();
        if(iterator.hasNext()) {
            Row row = iterator.next();
            return new Host(InetAddress.getByName(row.getString("leaderAddress")), row.getInt("leaderPort"));
        }
        return null;
    }

    public void setLeader(String groupName, Host leader) {
        session.execute("INSERT INTO groups (groupName, leaderAddress, leaderPort) VALUES " +
            "(" +
            "'" + groupName + "'," +
            "'" + leader.getAddress().getHostAddress() + "'," +
            leader.getPort() +
            ")");
    }

    private ArrayList<String> readContactPoints() throws IOException {
        ArrayList<String> contactPoints = new ArrayList<>();

        InputStream fis;
        BufferedReader br;
        String         line;

        fis = new FileInputStream(configPath);
        br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
        while ((line = br.readLine()) != null) {
            contactPoints.add(line);
        }

        // Done with the file
        br.close();
        br = null;
        fis = null;

        return contactPoints;
    }

    public DatabaseHandler(Host self) throws IOException {

        this.self = self;

        Iterator<String> contactPoints = readContactPoints().iterator();

        // Connect to the Cassandra cluster
        Cluster.Builder builder = Cluster.builder();
        while(contactPoints.hasNext()) {
            String next = contactPoints.next();
            builder = builder.addContactPoint(next);

        }

        Cluster cluster = builder.build();

        // Connect to the "mykeyspace" keyspace
        session = cluster.connect();

        session.execute("CREATE KEYSPACE IF NOT EXISTS gcom WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 3};");
        session.execute("USE gcom;");
        session.execute("CREATE TABLE IF NOT EXISTS messages (vectorClock text, message text, senderAddress text," +
                            " senderPort int, group text, beenAt text, addedBy text, isViewChange boolean, PRIMARY KEY(vectorClock, group));");
        session.execute("CREATE TABLE IF NOT EXISTS members (group text, hostAddress text, hostPort int, " +
                                "vectorClock text, connected boolean, PRIMARY KEY(group, hostAddress, hostPort));");
        session.execute("CREATE TABLE IF NOT EXISTS groups (groupName text PRIMARY KEY, leaderAddress text, leaderPort int);");

        try {
            session.execute("CREATE INDEX IF NOT EXISTS ON members(connected);");
        } catch (InvalidQueryException e) {
            if(!e.getMessage().equals("Index already exists")) {

                e.printStackTrace();
            } else {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        //new DatabaseHandler();
    }

    public ArrayList<Host> getMembers(String groupName, boolean onlyConnectedMembers) throws UnknownHostException {
        ArrayList<Host> members = new ArrayList<>();
        ResultSet resultSet;
        if(onlyConnectedMembers) {
            resultSet = session.execute("SELECT hostAddress, hostPort FROM members WHERE connected=true AND group='" + groupName + "';");
        } else {
            resultSet = session.execute("SELECT hostAddress, hostPort FROM members WHERE group='" + groupName + "';");
        }
        Iterator<Row> rows = resultSet.iterator();
        while(rows.hasNext()) {
            Row row = rows.next();
            Host host = new Host(InetAddress.getByName(row.getString("hostAddress")), row.getInt("hostPort"));
            members.add(host);
        }
        return members;
    }

    public VectorClock getVectorClock(String groupName, Host host) throws UnknownHostException {
        ResultSet resultSet = session.execute("SELECT vectorClock FROM members WHERE " +
                "group='" + groupName + "' AND " +
                "hostAddress='"+ host.getAddress().getHostAddress() +"' AND " +
                "hostPort="+ host.getPort() +";");
        if(resultSet.iterator().hasNext()) {
            return VectorClock.fromString(resultSet.iterator().next().getString("vectorClock"));
        }
        return new VectorClock();
    }

    public ArrayList<Message> getNewMessages(String groupName, VectorClock clock) throws UnknownHostException {
        ArrayList<Message> newMessages = new ArrayList<>();
        ArrayList<Message> messages = new ArrayList<>();
        ResultSet resultSet = session.execute("SELECT * FROM messages WHERE group='" +
                groupName + "' ALLOW FILTERING;");
        Iterator<Row> rows = resultSet.iterator();
        while(rows.hasNext()) {
            Row row = rows.next();
            boolean isReliable = false;
            boolean deliverCausally = true;
            String text = row.getString("message");
            String hostAddress = row.getString("senderAddress");
            int hostPort = row.getInt("senderPort");
            Host sender = new Host(InetAddress.getByName(hostAddress), hostPort);
            VectorClock vectorClock = VectorClock.fromString(row.getString("vectorClock"));
            String group = row.getString("group");
            boolean isViewChange = row.getBool("isViewChange");

            Message message;
            if(isViewChange) {
                message = new OfflineViewChange(isReliable, deliverCausally, text, sender, vectorClock, group, null);
            } else {
                message = new Message(isReliable, deliverCausally, text, sender, vectorClock, group);

            }
            messages.add(message);
        }
        Collections.sort(messages);
        for(Message message : messages) {
            if(!clock.getClock().containsKey(message.getSource()) || !clock.hasReceived(message)) {

                newMessages.add(message);
            }
        }
        return newMessages;
    }

    public VectorClock getCurrentVectorClock(String groupName) throws UnknownHostException {
        ResultSet resultSet = session.execute("SELECT vectorClock FROM messages WHERE group = '" + groupName + "' ALLOW FILTERING;");
        Iterator<Row> rows = resultSet.iterator();
        VectorClock clock = new VectorClock();
        while(rows.hasNext()) {
            VectorClock newClock = VectorClock.fromString(rows.next().getString("vectorClock"));
            for(Host host : newClock.getClock().keySet()) {
                if(!clock.getClock().containsKey(host) || clock.getValue(host) < newClock.getValue(host)) {
                    clock.addValue(host, newClock.getValue(host));
                }
            }
        }
        return clock;
    }
}



