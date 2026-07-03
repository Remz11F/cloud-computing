/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Orchestrator.data;

import com.azure.cosmos.*;
import com.azure.cosmos.ConsistencyLevel;

/**
 *
 * @author N1185866
 */
public class CosmosDBClient {
    
    // My Cosmos DB connection details from the Azure portal endpoint and key
    private static final String ENDPOINT = "https://mogun.documents.azure.com:443/";
    private static final String KEY = "iBy68HfnPpNqbDvrP4juajVjhBAvGcpkrXxkWrrz5na0GmbeJUo9XKLt24399q4NKwSAbGnljQH8ACDb37s0FQ==";

    // This is the database name in Cosmos
    private static final String DATABASE_NAME = "Coursework";

    private static CosmosClient client;

    // keeps a Cosmos client running so it doesnt reconnect every time
    public static CosmosClient connect() {
        if (client == null) {
            client = new CosmosClientBuilder()
                    .endpoint(ENDPOINT)
                    .key(KEY)
                    .consistencyLevel(ConsistencyLevel.EVENTUAL)
                    .buildClient();
        }
        return client;
    }

    // gets a container by name
    public static CosmosContainer getContainer(String containerName) {
        return connect()
                .getDatabase(DATABASE_NAME)
                .getContainer(containerName);
    }
}

