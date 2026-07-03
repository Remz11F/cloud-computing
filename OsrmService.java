/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/WebServices/GenericResource.java to edit this template
 */
package Orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service (Orchestrator)
 *
 * Calls OSRM, maps JSON to POJOs, returns a simplified JSON summary.
 *
 * @author N1185866
 */
@Path("osrm")
public class OsrmService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
     public String getJson(
            @QueryParam("startLat") Double startLat,
            @QueryParam("startLon") Double startLon,
            @QueryParam("endLat") Double endLat,
            @QueryParam("endLon") Double endLon
    ) {

        try {
            // If nothing was passed in, use default coordinates (so it still runs)
            if (startLat == null || startLon == null || endLat == null || endLon == null) {
                startLat = 52.9548;
                startLon = -1.1505;
                endLat = 53.0080;
                endLon = -1.4689;
            }

            // OSRM expects coordinates like: lon,lat;lon,lat
            String osrmUrl =
                "https://router.project-osrm.org/route/v1/driving/"
                + startLon + "," + startLat + ";"
                + endLon + "," + endLat
                + "?overview=false";

            HttpURLConnection conn = (HttpURLConnection) new URL(osrmUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
            ObjectMapper mapper = new ObjectMapper();

            try (InputStream body = is) {

                if (status != 200) {
                    return "{\"error\":\"OSRM returned " + status + "\"}";
                }

                // JSON -> POJOs
                OsrmResponse osrmResponse = mapper.readValue(body, OsrmResponse.class);

                // Extract needed info
                RouteSummary summary = new RouteSummary();
                summary.code = osrmResponse.code;

                if (osrmResponse.routes != null && !osrmResponse.routes.isEmpty()) {
                    Route first = osrmResponse.routes.get(0);
                    summary.distance = first.distance;
                    summary.duration = first.duration;
                }

                // POJO -> JSON
                return mapper.writeValueAsString(summary);

            } finally {
                conn.disconnect();
            }

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.trim().isEmpty()) msg = "Unknown error";
            msg = msg.replace("\"", "'");
            return "{\"error\":\"" + msg + "\"}";
        }
    }
}