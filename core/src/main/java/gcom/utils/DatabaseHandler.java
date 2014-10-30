package gcom.utils;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class DatabaseHandler {

    private final Host self;
    Session session;

    public void insertMessage(Message message) {
        String vectorClock = message.getVectorClock().toString();
        String text = message.getText();
        String beenAt = message.getBeenAt().toString();

        session.execute("INSERT INTO messages (vectorClock, message, senderAddress, senderPort, isReliable, deliverCausally, group, beenAt) VALUES" +
                "('" +
                vectorClock + "','" +
                text + "','" +
                message.getSource().getAddress().getHostAddress() + "'," +
                message.getSource().getPort() + "," +
                message.isReliable() + "," +
                message.deliverCausally() + ",'" +
                message.getGroupName() + "','" +
                beenAt + "','" +
                self + "');");
    }

    public boolean hasMember(Host host, String groupName) {
        ResultSet resultSet = session.execute("SELECT * FROM members WHERE group = '"+ groupName + "' AND hostAddress = '" + host.getAddress().getHostAddress() + "' AND hostPort =" + host.getPort());
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

    public DatabaseHandler(String address, Host self) {

        this.self = self;

        // Connect to the Cassandra cluster
        Cluster cluster = Cluster.builder()
            .addContactPoint(address)
            //.addContactPoint("94.254.18.40")
            .build();

        // Connect to the "mykeyspace" keyspace
        session = cluster.connect();

        session.execute("CREATE KEYSPACE IF NOT EXISTS gcom WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 3};");
        session.execute("USE gcom;");
        session.execute("CREATE TABLE IF NOT EXISTS messages (vectorClock text PRIMARY KEY, message text, " +
                                "senderAddress text, senderPort int, isReliable boolean, deliverCausally boolean, " +
                                    "group text, beenAt text, addedBy text);");
        try {
            session.execute("CREATE INDEX ON messages(group);");
        } catch (InvalidQueryException e) {

        }
        session.execute("CREATE TABLE IF NOT EXISTS members (group text, hostAddress text, hostPort int, " +
                                "vectorClock text, connected boolean, PRIMARY KEY(group, hostAddress, hostPort));");
        try {
            session.execute("CREATE INDEX ON members(connected);");
        } catch (InvalidQueryException e) {

        }
        session.execute("CREATE TABLE IF NOT EXISTS groups (groupName text PRIMARY KEY, leaderAddress text, leaderPort int);");
    }

    public static void main(String[] args) {
        //new DatabaseHandler();
    }

    public ArrayList<Host> getMembers(String groupName, boolean onlyConnectedMembers) throws UnknownHostException {
        ArrayList<Host> members = new ArrayList<>();
        ResultSet resultSet = null;
        if(onlyConnectedMembers) {
            resultSet = session.execute("SELECT * FROM members WHERE connected=true AND group='" + groupName + "';");
        } else {
            resultSet = session.execute("SELECT * FROM members WHERE group='" + groupName + "';");
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
                groupName + "';");
        Iterator<Row> rows = resultSet.iterator();
        while(rows.hasNext()) {
            Row row = rows.next();
            boolean isReliable = row.getBool("isReliable");
            boolean deliverCausally = row.getBool("deliverCausally");
            String text = row.getString("message");
            String hostAddress = row.getString("senderAddress");
            int hostPort = row.getInt("senderPort");
            Host sender = new Host(InetAddress.getByName(hostAddress), hostPort);
            VectorClock vectorClock = VectorClock.fromString(row.getString("vectorClock"));
            String group = row.getString("group");

            Message message = new Message(isReliable, deliverCausally, text, sender, vectorClock, group);
            messages.add(message);
        }
        for(Message message : messages) {
            if(clock.isBeforeOrEqualOnAllValuesExcept(message.getVectorClock(), message.getSource())) {
                newMessages.add(message);
            }
        }
        return newMessages;
    }
}



