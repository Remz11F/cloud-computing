/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Orchestrator.cosmos;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;

/**
 *
 * @author N1185866
 * This file is just a quick test to check my Cosmos DB connection is working.
 * When I run it, it should connect and print out any matching items from my container.
 */
public class CosmosTest {

    // My Cosmos DB connection details from the Azure portal (endpoint + key)
    // Database + container names must match what I created in Cosmos DB
    private static final String ENDPOINT = "https://mogun.documents.azure.com:443/";
    private static final String KEY = "iBy68HfnPpNqbDvrP4juajVjhBAvGcpkrXxkWrrz5na0GmbeJUo9XKLt24399q4NKwSAbGnljQH8ACDb37s0FQ==";
    private static final String DATABASE_NAME = "Coursework";
    private static final String CONTAINER_NAME = "items";

    public static void main(String[] args) {

        // Build the Cosmos client and open a connection
        try (CosmosClient client = new CosmosClientBuilder()
                .endpoint(ENDPOINT)
                .key(KEY)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient()) {

            // Connect to the container I want to query
            CosmosContainer container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);

            // check what items exist for a specific location
            String location = "Nottingham";

            // SQL query to filter items by location
            String sql = "SELECT * FROM c WHERE c.location = @location";

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            // Pass the location value safely into the query
            SqlParameter param = new SqlParameter("@location", location);
            SqlQuerySpec querySpec = new SqlQuerySpec(sql, Arrays.asList(param));

            // Read results as JSON objects 
            CosmosPagedIterable<ObjectNode> items = container.queryItems(querySpec, options, ObjectNode.class);

            // Print simple output (name + location)
            System.out.println("The following items are available for rent at " + location + ":");
            for (ObjectNode item : items) {
                System.out.println(item.get("name").asText() + " - " + item.get("location").asText());
            }

            // Print the full JSON for each item
            System.out.println("\nThe following items, displayed as JSON, are available for rent at " + location + ":");
            for (ObjectNode item : items) {
                System.out.println(item.toString() + "\n");
            }

            // Close the client connection
            client.close();
        }
    }
}
