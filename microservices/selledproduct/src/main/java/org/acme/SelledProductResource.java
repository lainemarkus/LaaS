package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/selledproduct")
public class SelledProductResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getAnalytics() {
        // Here you would call your analytics logic
        // and return the result. For now, we just return a placeholder message.
        return "Selled Product microservice is running.";
    }
}