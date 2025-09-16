
package org.acme.vehiclerouting.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VehicleRoutingResource {

    /**
     * BULLETPROOF demo-data endpoint - no dependencies, no serialization issues
     */
    @GET
    @Path("/demo-data")
    public Response getDemoData() {
        try {
            System.out.println("=== /demo-data endpoint called ===");

            // Create simple data structure that definitely serializes
            Map<String, Object> demoData = new HashMap<>();

            // Create vehicles list
            List<Map<String, Object>> vehicles = new ArrayList<>();

            Map<String, Object> vehicle1 = new HashMap<>();
            vehicle1.put("id", "vehicle-1");
            vehicle1.put("style", "motorcycle");
            vehicle1.put("homeLocation", new double[]{19.076, 72.8777});
            vehicle1.put("capacity", 5);
            vehicles.add(vehicle1);

            Map<String, Object> vehicle2 = new HashMap<>();
            vehicle2.put("id", "vehicle-2");
            vehicle2.put("style", "van");
            vehicle2.put("homeLocation", new double[]{18.5204, 73.8567});
            vehicle2.put("capacity", 10);
            vehicles.add(vehicle2);

            // Create visits list
            List<Map<String, Object>> visits = new ArrayList<>();

            Map<String, Object> visit1 = new HashMap<>();
            visit1.put("id", "visit-1");
            visit1.put("name", "Mumbai Delivery");
            visit1.put("location", new double[]{19.0896, 72.8656});
            visit1.put("demand", 1);
            visits.add(visit1);

            Map<String, Object> visit2 = new HashMap<>();
            visit2.put("id", "visit-2");
            visit2.put("name", "Pune Delivery");
            visit2.put("location", new double[]{18.5289, 73.8732});
            visit2.put("demand", 1);
            visits.add(visit2);

            // Assemble final response
            demoData.put("vehicles", vehicles);
            demoData.put("visits", visits);
            demoData.put("score", "0hard/0soft");
            demoData.put("status", "demo-data-loaded");

            System.out.println("Demo data created successfully:");
            System.out.println("- Vehicles: " + vehicles.size());
            System.out.println("- Visits: " + visits.size());

            return Response.ok(demoData).build();

        } catch (Exception e) {
            System.err.println("ERROR in /demo-data: " + e.getMessage());
            e.printStackTrace();

            // Return error details as JSON
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            error.put("endpoint", "/demo-data");

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
        }
    }

    /**
     * Alternative vehicleRoute endpoint that definitely works
     */
    @GET
    @Path("/vehicleRoute")
    public Response getVehicleRoute() {
        try {
            System.out.println("=== /vehicleRoute endpoint called ===");

            // Redirect to demo-data for now
            return getDemoData();

        } catch (Exception e) {
            System.err.println("ERROR in /vehicleRoute: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to load vehicle route data");
            error.put("details", e.getMessage());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
        }
    }

    /**
     * Simple solve endpoint
     */
    @POST
    @Path("/solve")
    public Response solve() {
        try {
            System.out.println("=== /solve endpoint called ===");

            // Return the same demo data for now
            Response demoResponse = getDemoData();

            System.out.println("Solve completed (demo mode)");
            return demoResponse;

        } catch (Exception e) {
            System.err.println("ERROR in /solve: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Solve failed");
            error.put("details", e.getMessage());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
        }
    }

    /**
     * Health check
     */
    @GET
    @Path("/health")
    public Response health() {
        System.out.println("=== Health check called ===");

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Vehicle Routing");
        health.put("timestamp", System.currentTimeMillis());

        return Response.ok(health).build();
    }

    /**
     * Debug endpoint
     */
    @GET
    @Path("/debug")
    public Response debug() {
        System.out.println("=== Debug endpoint called ===");

        Map<String, Object> debug = new HashMap<>();
        debug.put("message", "Debug endpoint working");
        debug.put("java_version", System.getProperty("java.version"));
        debug.put("working_directory", System.getProperty("user.dir"));

        return Response.ok(debug).build();
    }
}