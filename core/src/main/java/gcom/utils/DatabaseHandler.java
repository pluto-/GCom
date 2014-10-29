package gcom.utils;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.util.ArrayList;

public class DatabaseHandler {

    Cluster cluster;

    public void updateCluster(ArrayList<Host> hosts) {
        Cluster.Builder builder = Cluster.builder();
        for(Host host: hosts) {
            builder.addContactPoint(host.getAddress().getHostAddress());
        }
        cluster = builder.build();
    }


    public DatabaseHandler() {

        // Connect to the Cassandra cluster
        cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .addContactPoint("127.0.0.2")
                .addContactPoint("94.254.18.40")
                .build();

        // Connect to the "mykeyspace" keyspace
        Session session = cluster.connect();

        session.execute("CREATE KEYSPACE IF NOT EXISTS mykeyspace WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 3};");
        session.execute("USE mykeyspace");
        session.execute("CREATE TABLE IF NOT EXISTS users (user_id int PRIMARY KEY, firstName text, lastName text);");

        ResultSet resultSet = session.execute("SELECT * FROM users WHERE user_id=3");
        if(!resultSet.iterator().hasNext()) {
            System.out.println("Inserting John.");
            session.execute("INSERT INTO users (user_id, firstName, lastName) VALUES (3, 'Jonas', 'Mark');");
        }

        // Execute another statement
        resultSet = session.execute("SELECT * FROM users");

        // Print results
        System.out.println("All users:");
        for (Row row : resultSet) {
            System.out.println(String.format("%d %s %s",
                    row.getInt("user_id"),
                    row.getString("firstName"),
                    row.getString("lastName")));
        }
    }

    public static void main(String[] args) {
        new DatabaseHandler();
    }
}



