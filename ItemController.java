/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Orchestrator.controllers;

import Orchestrator.data.CosmosDBClient;
import Orchestrator.models.ItemModel;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;
import Orchestrator.security.ApiKeyRequired;

/**
 *
 * @author N1185866
 */
@Path("items")
@ApiKeyRequired
@Produces(MediaType.APPLICATION_JSON)
public class ItemController {

    private final CosmosContainer itemsContainer = CosmosDBClient.getContainer("items");

    @GET
    public List<ItemModel> findItems(
            @QueryParam("location") String location,
            @QueryParam("name") String name
    ) {

        boolean hasLocation = location != null && !location.trim().isEmpty();
        boolean hasName = name != null && !name.trim().isEmpty();

        // If no filters are provided, return nothing for now
        if (!hasLocation && !hasName) {
            return new ArrayList<>();
        }

        // Start query and add filters depending on what the user sends
        StringBuilder sql = new StringBuilder("SELECT * FROM c WHERE 1=1");
        List<SqlParameter> params = new ArrayList<>();

        if (hasLocation) {
            sql.append(" AND c.location = @location");
            params.add(new SqlParameter("@location", location));
        }

        if (hasName) {
            // This allows keyword searching inside the item name 
            sql.append(" AND CONTAINS(c.name, @name, true)");
            params.add(new SqlParameter("@name", name));
        }

        SqlQuerySpec querySpec = new SqlQuerySpec(sql.toString(), params);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedIterable<ItemModel> results =
                itemsContainer.queryItems(querySpec, options, ItemModel.class);

        List<ItemModel> items = new ArrayList<>();
        for (ItemModel i : results) {
            items.add(i);
        }

        return items;
    }
}
