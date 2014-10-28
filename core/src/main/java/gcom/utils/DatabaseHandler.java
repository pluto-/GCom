package gcom.utils;


import com.datastax.driver.core.*;
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
        try{
            SimpleStatement toPrepare = new SimpleStatement("USE mykeyspace;");
            PreparedStatement prepared = session.prepare(toPrepare);
            ResultSet resultSet = session.execute(prepared.bind(1));

        }catch(NoHostAvailableException e) {

        }

        // Prepare a statement
        SimpleStatement toPrepare = new SimpleStatement("SELECT * FROM users WHERE user_id=?");
        toPrepare.setConsistencyLevel(ConsistencyLevel.ONE);

        // Execute the statement
        PreparedStatement prepared = session.prepare(toPrepare);
        ResultSet resultSet = session.execute(prepared.bind(1));

        // Print results
        Row result = resultSet.one();
        System.out.println(String.format(
                "Users with ID 1: %s %s",
                result.getString("firstName"),
                result.getString("lastName")));

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
}



