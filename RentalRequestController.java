package Orchestrator.controllers;

import Orchestrator.data.CosmosDBClient;
import Orchestrator.models.ItemModel;
import Orchestrator.models.RequestEventModel;
import Orchestrator.models.RequestModel;
import Orchestrator.rabbitmq.RabbitMQPublisher;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import Orchestrator.security.ApiKeyRequired;

/**
 *
 * @author N1185866
 */
@Path("requests")
@ApiKeyRequired
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RentalRequestController {

    private final CosmosContainer requestsContainer = CosmosDBClient.getContainer("Requests");
    private final CosmosContainer itemsContainer = CosmosDBClient.getContainer("items");

    private final ObjectMapper mapper = new ObjectMapper();

    @GET
    public Response ping() {
        return Response.ok("Requests endpoint working").build();
    }

    @POST
    public Response createRequest(RequestModel request) {

        if (request == null || request.itemId == null || request.itemId.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing itemId in request body.")
                    .build();
        }

        if (request.userId == null || request.userId.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing userId in request body.")
                    .build();
        }

        request.id = UUID.randomUUID().toString();
        request.status = "pending";

        try {
            requestsContainer.createItem(request);

            publishEvent("request.created", request);

            return Response.status(Response.Status.CREATED).entity(request).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create request: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("{id}/approve")
    public Response approveRequest(@PathParam("id") String id) {

        if (id == null || id.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request id is required.")
                    .build();
        }

        try {
            RequestModel request = findRequestById(id);

            if (request == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No request found with id: " + id)
                        .build();
            }

            request.status = "approved";
            requestsContainer.upsertItem(request);

            ItemModel item = findItemByItemId(request.itemId);
            if (item != null) {
                item.available = false;
                itemsContainer.upsertItem(item);
            }

            publishEvent("request.approved", request);

            return Response.ok(request).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to approve request: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("{id}/cancel")
    public Response cancelRequest(@PathParam("id") String id) {

        if (id == null || id.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request id is required.")
                    .build();
        }

        try {
            RequestModel request = findRequestById(id);

            if (request == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No request found with id: " + id)
                        .build();
            }

            request.status = "cancelled";
            requestsContainer.upsertItem(request);

            ItemModel item = findItemByItemId(request.itemId);
            if (item != null) {
                item.available = true;
                itemsContainer.upsertItem(item);
            }

            publishEvent("request.cancelled", request);

            return Response.ok(request).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to cancel request: " + e.getMessage())
                    .build();
        }
    }

    private RequestModel findRequestById(String id) {

        String sql = "SELECT * FROM c WHERE c.id = @id";
        SqlParameter param = new SqlParameter("@id", id);

        SqlQuerySpec querySpec = new SqlQuerySpec(sql, Arrays.asList(param));
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedIterable<RequestModel> results =
                requestsContainer.queryItems(querySpec, options, RequestModel.class);

        for (RequestModel r : results) {
            return r;
        }
        return null;
    }

    private ItemModel findItemByItemId(String itemId) {

        String sql = "SELECT * FROM c WHERE c.itemId = @itemId";
        SqlParameter param = new SqlParameter("@itemId", itemId);

        SqlQuerySpec querySpec = new SqlQuerySpec(sql, Arrays.asList(param));
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedIterable<ItemModel> results =
                itemsContainer.queryItems(querySpec, options, ItemModel.class);

        for (ItemModel i : results) {
            return i;
        }
        return null;
    }

    private void publishEvent(String eventType, RequestModel request) {
        try {
            RequestEventModel event = new RequestEventModel();
            event.eventType = eventType;
            event.timestamp = Instant.now().toString();
            event.requestId = request.id;
            event.itemId = request.itemId;
            event.userId = request.userId;
            event.status = request.status;

            String json = mapper.writeValueAsString(event);
            RabbitMQPublisher.publish(eventType, json);

        } catch (Exception e) {
            System.out.println("Failed to publish event: " + e.getMessage());
        }
    }
}
