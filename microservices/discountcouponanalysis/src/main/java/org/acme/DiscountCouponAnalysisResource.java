package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/discountcouponanalysis")
public class DiscountCouponAnalysisResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getAnalysis() {
        // Here you would call your analysislogic
        // and return the result. For now, we just return a placeholder message.
        return "Discount Coupon Analysis microservice is running.";
    }
}