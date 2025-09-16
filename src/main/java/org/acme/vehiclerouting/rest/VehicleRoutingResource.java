package org.acme.vehiclerouting.rest;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.service.VehicleRoutingService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VehicleRoutingResource {

    @Inject
    VehicleRoutingService vehicleRoutingService;

    /**
     * GET current problem data - ENHANCED with better error handling
     */
    @GET
    @Path("/vehicleRoute")
    public Response getVehicleRoute() {
        try {
            VehicleRoutePlan problem = vehicleRoutingService.getProblem();

            if (problem == null || problem.getVehicles().isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT)
                    .entity("No problem data available")
                    .build();
            }

            return Response.ok(problem).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error loading problem data: " + e.getMessage())
                .build();
        }
    }

    /**
     * POST solve the problem - ENHANCED with timeout
     */
    @POST
    @Path("/solve")
    public Response solve() {
        try {
            VehicleRoutePlan problem = vehicleRoutingService.getProblem();

            if (problem == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No problem data to solve")
                    .build();
            }

            System.out.println("Starting solve for " + problem.getVehicles().size() + 
                " vehicles and " + problem.getVisits().size() + " visits");

            CompletableFuture<VehicleRoutePlan> solutionFuture = 
                vehicleRoutingService.solveAsync(problem);

            // Wait for solution with 60 second timeout
            VehicleRoutePlan solution = solutionFuture.get(60, TimeUnit.SECONDS);

            System.out.println("Solve completed with score: " + solution.getScore());

            return Response.ok(solution).build();

        } catch (TimeoutException e) {
            return Response.status(Response.Status.REQUEST_TIMEOUT)
                .entity("Solving timed out after 60 seconds")
                .build();
        } catch (ExecutionException | InterruptedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Solving failed: " + e.getMessage())
                .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unexpected error: " + e.getMessage())
                .build();
        }
    }

    @POST
    @Path("/stopSolving")
    public Response stopSolving() {
        vehicleRoutingService.stopSolving();
        return Response.ok("Solving stopped").build();
    }

    /**
     * NEW: Reset and reload problem data
     */
    @POST
    @Path("/resetData")
    public Response resetData() {
        vehicleRoutingService.resetProblem();
        VehicleRoutePlan newProblem = vehicleRoutingService.getProblem();
        return Response.ok(newProblem).build();
    }

    /**
     * NEW: Load full Amazon dataset
     */
    @POST
    @Path("/loadFullDataset")
    public Response loadFullDataset() {
        try {
            VehicleRoutePlan fullProblem = vehicleRoutingService.loadFullDataset();
            return Response.ok(fullProblem).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error loading full dataset: " + e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("Vehicle Routing Service is running").build();
    }
}