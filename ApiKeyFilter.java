/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Orchestrator.security;

import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
/**
 *
 * @author N1185866
 */
@Provider
@ApiKeyRequired
@Priority(Priorities.AUTHENTICATION)

public class ApiKeyFilter implements ContainerRequestFilter {

    private static final String HEADER_NAME = "X-API-KEY";
    private static final String EXPECTED_KEY = "ntusec-2026"; // choose any value

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String key = requestContext.getHeaderString(HEADER_NAME);

        if (key == null || !EXPECTED_KEY.equals(key)) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Missing/invalid API key")
                        .build()
            );
        }
    }
}