package gcom.utils;


import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

public class DatabaseHandler {

    public static void main(String[] args) {
        // Connect to the Cassandra cluster
        Cluster cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .addContactPoint("127.0.0.2")
                .addContactPoint("127.0.0.3")
                .build();

        // Connect to the "mykeyspace" keyspace
        Session session = cluster.connect();

        session.execute("CREATE KEYSPACE IF NOT EXISTS mykeyspace WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 3};");
        session.execute("USE mykeyspace");
        session.execute("CREATE TABLE IF NOT EXISTS users (user_id int PRIMARY KEY, firstName text, lastName text);");

        ResultSet resultSet = session.execute("SELECT * FROM users WHERE user_id=1");
        if(!resultSet.iterator().hasNext()) {
            System.out.println("Inserting John.");
            session.execute("INSERT INTO users (user_id, firstName, lastName) VALUES (1, 'John', 'Smith');");
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

    /*private static void startContactPoint(int nr) {
        SimpleClient client = new SimpleClient();
    }*/
}



