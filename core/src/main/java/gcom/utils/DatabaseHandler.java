package gcom.utils;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.util.ArrayList;

public class DatabaseHandler {

    Session session;

    public void insertMessage(Message message) {
        String vectorClock = message.getVectorClock().toString();
        String text = message.getText();
        String beenAt = message.getBeenAt().toString();


        session.execute("INSERT INTO messages (vectorClock, message, senderAddress, senderPort, isReliable, group, beenAt) VALUES" +
                "(" +
                vectorClock + "," +
                text + "," +
                message.getSource().getAddress().getHostAddress() + "," +
                message.getSource().getPort() + "," +
                message.isReliable() + "," +
                message.getGroupName() + "," +
                beenAt + ")");
    }

    public void addMember(String groupName, Host member, VectorClock vectorClock, boolean isConnected) {
        session.execute("INSERT INTO members (group, hostAddress, hostPort, vectorClock, connected) VALUES" +
                "(" +
                groupName + "," +
                member.getAddress().getHostAddress() + "," +
                member.getPort() + "," +
                vectorClock.toString() + "," +
                isConnected +
                ")");
    }

    public void updateMemberConnected(String groupName, Host member, boolean isConnected) {
        session.execute("UPDATE members SET connected=" + isConnected + " WHERE group='"+ groupName +"' AND hostAddress='" + member.getAddress().getHostAddress() + "' AND hostPort=" + member.getPort() + ";");
    }

    public void updateMemberVectorClock(String groupName, Host member, VectorClock vectorClock) {
        session.execute("UPDATE members SET vectorClock='" + vectorClock.toString() + "' WHERE group='"+ groupName +"' AND hostAddress='" + member.getAddress().getHostAddress() + "' AND hostPort=" + member.getPort() + ";");
    }


    public DatabaseHandler() {

        // Connect to the Cassandra cluster
        Cluster cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                //.addContactPoint("94.254.18.40")
                .build();

        // Connect to the "mykeyspace" keyspace
        session = cluster.connect();

        session.execute("CREATE KEYSPACE IF NOT EXISTS gcom WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 3};");
        session.execute("USE gcom");
        session.execute("CREATE TABLE IF NOT EXISTS messages (vectorClock text PRIMARY KEY, message text, senderAddress text, senderPort int, isReliable boolean, group text, beenAt text);");
        session.execute("CREATE TABLE IF NOT EXISTS members (group text, hostAddress text, hostPort int, vectorClock text, connected boolean, PRIMARY KEY(group, hostAddress, hostPort));");

        /*ResultSet resultSet = session.execute("SELECT * FROM users WHERE user_id=3");
        if(!resultSet.iterator().hasNext()) {
            System.out.println("Inserting John.");
            session.execute("INSERT INTO users (user_id, firstName, lastName) VALUES (3, 'Jonas', 'Mark');");
        }*/

        // Execute another statement
       /* resultSet = session.execute("SELECT * FROM users");

        // Print results
        System.out.println("All users:");
        for (Row row : resultSet) {
            System.out.println(String.format("%d %s %s",
                    row.getInt("user_id"),
                    row.getString("firstName"),
                    row.getString("lastName")));
        }*/
    }

    public static void main(String[] args) {
        new DatabaseHandler();
    }
}



