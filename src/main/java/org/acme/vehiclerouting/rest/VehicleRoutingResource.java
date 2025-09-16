
package org.acme.vehiclerouting.rest;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.service.VehicleRoutingService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VehicleRoutingResource {

    @Inject
    VehicleRoutingService vehicleRoutingService;

    /**
     * GET endpoint to get the current problem data
     */
    @GET
    @Path("/vehicleRoute")
    public VehicleRoutePlan getVehicleRoute() {
        return vehicleRoutingService.getProblem();
    }

    /**
     * POST endpoint to solve the vehicle routing problem
     * This is the missing /solve endpoint causing your 404 error
     */
    @POST
    @Path("/solve")
    public Response solve() {
        try {
            // Get the problem data
            VehicleRoutePlan problem = vehicleRoutingService.getProblem();

            if (problem == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No problem data available")
                    .build();
            }

            // Solve the problem asynchronously
            CompletableFuture<VehicleRoutePlan> solutionFuture = 
                vehicleRoutingService.solveAsync(problem);

            // Wait for the solution (with timeout)
            VehicleRoutePlan solution = solutionFuture.get();

            return Response.ok(solution).build();

        } catch (ExecutionException | InterruptedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Solving failed: " + e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unexpected error: " + e.getMessage())
                .build();
        }
    }

    /**
     * POST endpoint to stop solving
     */
    @POST
    @Path("/stopSolving")
    public Response stopSolving() {
        try {
            vehicleRoutingService.stopSolving();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Stop solving failed: " + e.getMessage())
                .build();
        }
    }

    /**
     * GET endpoint for health check
     */
    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("Vehicle Routing Service is running").build();
    }
}