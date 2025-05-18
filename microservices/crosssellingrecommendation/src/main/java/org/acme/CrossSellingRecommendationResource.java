package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/crosssellingrecommendation")
public class CrossSellingRecommendationResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getRecommendation() {
        // Here you would call your recommendation logic
        // and return the result. For now, we just return a placeholder message.
    
        return "CrossSellingRecommendation microservice is running.";
    }
}