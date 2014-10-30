package gcom.utils;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class DatabaseHandler {

    Session session;

    public void insertMessage(Message message) {
        String vectorClock = message.getVectorClock().toString();
        String text = message.getText();
        String beenAt = message.getBeenAt().toString();

        session.execute("INSERT INTO messages (vectorClock, message, senderAddress, senderPort, isReliable, group, beenAt) VALUES" +
                "('" +
                vectorClock + "','" +
                text + "','" +
                message.getSource().getAddress().getHostAddress() + "'," +
                message.getSource().getPort() + "," +
                message.isReliable() + ",'" +
                message.getGroupName() + "','" +
                beenAt + "')");
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

    public DatabaseHandler(String address) {

        // Connect to the Cassandra cluster
        Cluster cluster = Cluster.builder()
            .addContactPoint(address)
            //.addContactPoint("94.254.18.40")
            .build();

        // Connect to the "mykeyspace" keyspace
        session = cluster.connect();

        session.execute("CREATE KEYSPACE IF NOT EXISTS gcom WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 3};");
        session.execute("USE gcom");
        session.execute("CREATE TABLE IF NOT EXISTS messages (vectorClock text PRIMARY KEY, message text, senderAddress text, senderPort int, isReliable boolean, group text, beenAt text);");
        session.execute("CREATE TABLE IF NOT EXISTS members (group text, hostAddress text, hostPort int, vectorClock text, connected boolean, PRIMARY KEY(group, hostAddress, hostPort));");
        session.execute("CREATE TABLE IF NOT EXISTS groups (groupName text PRIMARY KEY, leaderAddress text, leaderPort int);");


    }

    public static void main(String[] args) {
        //new DatabaseHandler();
    }

    public Group getMembers(String groupName) {
        return null;
    }
}



