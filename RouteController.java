package Orchestrator.controllers;

import Orchestrator.data.CosmosDBClient;
import Orchestrator.models.ItemModel;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import Orchestrator.security.ApiKeyRequired;

/**
 *
 * Option A: Route Search (fastest duration first)
 *
 * Gets items from Cosmos by location, calls OSRM for each item,
 * then returns top 3 fastest routes.
 *
 * @author N1185866
 */
@Path("route")
@ApiKeyRequired
@Produces(MediaType.APPLICATION_JSON)
public class RouteController {

    private final CosmosContainer itemsContainer = CosmosDBClient.getContainer("items");

    // Default start point (Nottingham)
    private static final double DEFAULT_START_LAT = 52.9548;
    private static final double DEFAULT_START_LON = -1.1505;

    @GET
    public Response findFastestRoutes(
            @QueryParam("location") String location,
            @QueryParam("startLat") Double startLat,
            @QueryParam("startLon") Double startLon,
            @QueryParam("endLat") Double endLat,
            @QueryParam("endLon") Double endLon
    ) {

        // Simple check
        if (location == null || location.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"location query param is required\"}")
                    .build();
        }

        // Use defaults if user didn’t pass start coords
        double sLat = (startLat == null) ? DEFAULT_START_LAT : startLat;
        double sLon = (startLon == null) ? DEFAULT_START_LON : startLon;

        // Use end coords from URL if provided, otherwise use location mapping
        double eLat;
        double eLon;

        if (endLat != null && endLon != null) {
            eLat = endLat;
            eLon = endLon;
        } else {
            double[] dest = getCoordsForLocation(location);
            eLat = dest[0];
            eLon = dest[1];
        }

        // 1) Get all items from Cosmos by location
        List<ItemModel> items = queryItemsByLocation(location);

        // 2) Call OSRM per item and build route results
        List<RouteResult> results = new ArrayList<>();

        for (ItemModel item : items) {
            RouteResult r = buildRouteResult(item, sLat, sLon, eLat, eLon);
            if (r != null) {
                results.add(r);
            }
        }

        // 3) Sort by fastest duration
        results.sort(Comparator.comparingDouble(a -> a.duration));

        // 4) Keep only top 3 routes
        if (results.size() > 3) {
            results = results.subList(0, 3);
        }

        // 5) Add rank + readable distance/time
        for (int i = 0; i < results.size(); i++) {
            RouteResult r = results.get(i);

            r.rank = i + 1;

            // km + mins rounded to 2dp
            r.distanceKm = Math.round((r.distance / 1000.0) * 100.0) / 100.0;
            r.durationMins = Math.round((r.duration / 60.0) * 100.0) / 100.0;
        }

        // Build JSON response object
        RouteSearchResponse response = new RouteSearchResponse();
        response.location = location;
        response.startLat = sLat;
        response.startLon = sLon;
        response.endLat = eLat;
        response.endLon = eLon;
        response.results = results;

        return Response.ok(response).build();
    }

    // Query items by location
    private List<ItemModel> queryItemsByLocation(String location) {

        String sql = "SELECT * FROM c WHERE c.location = @location";
        SqlParameter param = new SqlParameter("@location", location);

        SqlQuerySpec querySpec = new SqlQuerySpec(sql, Arrays.asList(param));
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedIterable<ItemModel> cosmosResults =
                itemsContainer.queryItems(querySpec, options, ItemModel.class);

        List<ItemModel> items = new ArrayList<>();
        for (ItemModel i : cosmosResults) {
            items.add(i);
        }

        return items;
    }

    // Call OSRM and build one result
    private RouteResult buildRouteResult(ItemModel item,
                                        double startLat, double startLon,
                                        double endLat, double endLon) {
        try {
            // OSRM uses lon,lat order
            String osrmUrl =
                    "https://router.project-osrm.org/route/v1/driving/"
                            + startLon + "," + startLat
                            + ";" + endLon + "," + endLat
                            + "?overview=false";

            HttpURLConnection conn = (HttpURLConnection) new URL(osrmUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();

            ObjectMapper mapper = new ObjectMapper();

            try (InputStream body = is) {

                if (status != 200) {
                    return null;
                }

                // Parse JSON response quickly
                JsonNode root = mapper.readTree(body);

                JsonNode routes = root.get("routes");
                if (routes == null || !routes.isArray() || routes.size() == 0) {
                    return null;
                }

                JsonNode first = routes.get(0);
                double distance = first.get("distance").asDouble();
                double duration = first.get("duration").asDouble();

                RouteResult r = new RouteResult();
                r.itemId = item.itemId;
                r.name = item.name;
                r.location = item.location;
                r.distance = distance;
                r.duration = duration;

                return r;

            } finally {
                conn.disconnect();
            }

        } catch (Exception e) {
            return null;
        }
    }

    // Simple location  coordinates mapping
    private double[] getCoordsForLocation(String location) {

        String loc = location.trim().toLowerCase();

        if (loc.equals("nottingham")) {
            return new double[]{52.9548, -1.1505};
        }

        if (loc.equals("derby")) {
            return new double[]{52.9225, -1.4746};
        }

        if (loc.equals("liverpool")) {
            return new double[]{53.4084, -2.9916};
        }

        // fallback
        return new double[]{52.9548, -1.1505};
    }

    // JSON Response Classes 

    public static class RouteSearchResponse {
        public String location;
        public double startLat;
        public double startLon;
        public double endLat;
        public double endLon;
        public List<RouteResult> results;
    }

    public static class RouteResult {
        public int rank;

        public String itemId;
        public String name;
        public String location;

        public double distance;   // meters
        public double duration;   // seconds

        public double distanceKm; // km
        public double durationMins; // mins
    }
}
