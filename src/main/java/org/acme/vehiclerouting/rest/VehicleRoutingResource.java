
package org.acme.vehiclerouting.rest;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.rest.exception.ErrorInfo;
import org.acme.vehiclerouting.rest.exception.VehicleRoutingSolverException;
import org.acme.vehiclerouting.service.VehicleRoutingDataService;
import org.acme.vehiclerouting.service.VehicleRoutingService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VehicleRoutingResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleRoutingResource.class);

    @Inject
    VehicleRoutingDataService dataService;

    @Inject
    VehicleRoutingService solvingService;

    private final SolverManager<VehicleRoutePlan, String> solverManager;
    private final SolutionManager<VehicleRoutePlan, HardSoftLongScore> solutionManager;

    private final ConcurrentMap<String, Job> jobIdToJob = new ConcurrentHashMap<>();

    public VehicleRoutingResource(){
        this.solverManager = null;
        this.solutionManager = null;
    }

    @Inject
    public VehicleRoutingResource(SolverManager<VehicleRoutePlan, String> solverManager,
                                    SolutionManager<VehicleRoutePlan, HardSoftLongScore> solutionManager) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
    }

    /**
     * GET vehicleRoute - Returns frontend-compatible data structure
     */
    @GET
    @Path("/vehicleRoute")
    public Response getVehicleRoute() {
        try {
            System.out.println("=== /vehicleRoute endpoint called (frontend-compatible) ===");

            VehicleRoutePlan problem = solvingService.getProblem();
            if (problem == null) {
                // problem = dataService.createAmazonDeliveryProblem();
                System.out.println("No problem found");
            }

            // Convert to frontend-compatible structure
            Map<String, Object> frontendData = convertToFrontendFormat(problem);

            System.out.println("Returning frontend-compatible data:");
            System.out.println("- Vehicles: " + frontendData.get("vehicleCount"));
            System.out.println("- Visits: " + frontendData.get("visitCount"));

            return Response.ok(frontendData).build();

        } catch (Exception e) {
            System.err.println("ERROR in /vehicleRoute: " + e.getMessage());
            e.printStackTrace();
            // return getHardcodedFrontendData();
            return null;
        }
    }

    /**
     * GET demo-data - Also returns frontend-compatible format
     */
    @GET
    @Path("/demo-data")
    public Response getDemoData() {
        try {
            System.out.println("=== /demo-data endpoint called (frontend-compatible) ===");

            VehicleRoutePlan problem = dataService.createAmazonDeliveryProblem();
            Map<String, Object> frontendData = convertToFrontendFormat(problem);

            return Response.ok(frontendData).build();

        } catch (Exception e) {
            System.err.println("ERROR in /demo-data: " + e.getMessage());
            e.printStackTrace();
            // return getHardcodedFrontendData();
            return null;
        }
    }

    /**
     * POST route-plans - Returns frontend-compatible solved data
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/route-plans")
    public String solve() {
        VehicleRoutePlan problem = solvingService.getProblem();
        String jobId = UUID.randomUUID().toString();
        jobIdToJob.put(jobId, Job.ofRoutePlan(problem));
        solverManager.solveBuilder()
                .withProblemId(jobId)
                .withProblemFinder(jobId_ -> jobIdToJob.get(jobId).routePlan)
                .withBestSolutionConsumer(solution -> jobIdToJob.put(jobId, Job.ofRoutePlan(solution)))
                .withExceptionHandler((jobId_, exception) -> {
                    jobIdToJob.put(jobId, Job.ofException(exception));
                    LOGGER.error("Failed solving jobId ({}).", jobId, exception);
                })
                .run();
        return jobId;
    }

    /*  try {
            System.out.println("=== /route-plans endpoint called ===");

            VehicleRoutePlan problem = solvingService.getProblem();
            if (problem == null) {
                problem = dataService.createAmazonDeliveryProblem();
            }

            problem.setSolverStatus(SolverStatus.SOLVING_ACTIVE);
            CompletableFuture<VehicleRoutePlan> solutionFuture = solvingService.solveAsync(problem);
            VehicleRoutePlan solution = solutionFuture.get(60, TimeUnit.SECONDS);
            solution.setSolverStatus(SolverStatus.NOT_SOLVING);

            Map<String, Object> frontendData = convertToFrontendFormat(solution);

            System.out.println("Solve completed. Score: " + solution.getScore());
            return Response.ok(frontendData).build();

        } catch (Exception e) {
            System.err.println("ERROR in /solve: " + e.getMessage());
            e.printStackTrace();
            // return getHardcodedFrontendData();
            return null;
        }
 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/route-plans/{jobId}")
    public VehicleRoutePlan getRoutePlan(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        VehicleRoutePlan routePlan = getRoutePlanAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        String scoreExplanation = solutionManager.explain(routePlan).getSummary();
        routePlan.setSolverStatus(solverStatus);
        routePlan.setScoreExplanation(scoreExplanation);
        return routePlan;
    }

    @Operation(
            summary = "Get the route plan status and score for a given job ID.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The route plan status and the best score so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = VehicleRoutePlan.class))),
            @APIResponse(responseCode = "404", description = "No route plan found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @APIResponse(responseCode = "500", description = "Exception during solving a route plan.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/route-plans/{jobId}/status")
    public VehicleRoutePlan getStatus(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        VehicleRoutePlan routePlan = getRoutePlanAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        return new VehicleRoutePlan(routePlan.getName(), routePlan.getScore(), solverStatus);
    }

    private VehicleRoutePlan getRoutePlanAndCheckForExceptions(String jobId) {
        Job job = jobIdToJob.get(jobId);
        if (job == null) {
            throw new VehicleRoutingSolverException(jobId, Response.Status.NOT_FOUND, "No route plan found.");
        }
        if (job.exception != null) {
            throw new VehicleRoutingSolverException(jobId, job.exception);
        }
        return job.routePlan;
    }

    @Operation(
            summary = "Terminate solving for a given job ID. Returns the best solution of the route plan so far, as it might still be running or not even started.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The best solution of the route plan so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = VehicleRoutePlan.class))),
            @APIResponse(responseCode = "404", description = "No route plan found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @APIResponse(responseCode = "500", description = "Exception during solving a route plan.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/route-plans/{jobId}")
    public VehicleRoutePlan terminateSolving(
            @Parameter(description = "The job ID returned by the POST method.") @PathParam("jobId") String jobId) {
        solverManager.terminateEarly(jobId);
        return getRoutePlan(jobId);
    }

    @Operation(summary = "Submit a route plan to analyze its score.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200",
                    description = "Resulting score analysis, optionally without constraint matches.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ScoreAnalysis.class)))})
   

    /**
     * Convert VehicleRoutePlan to frontend-compatible format
     */
    private Map<String, Object> convertToFrontendFormat(VehicleRoutePlan plan) {
        Map<String, Object> data = new HashMap<>();

        // Convert vehicles
        List<Map<String, Object>> frontendVehicles = new ArrayList<>();
        for (Vehicle vehicle : plan.getVehicles()) {
            if (vehicle != null && vehicle.getHomeLocation() != null) {
                Map<String, Object> vehicleData = new HashMap<>();
                vehicleData.put("id", vehicle.getId());
                vehicleData.put("style", vehicle.getStyle());
                vehicleData.put("capacity", vehicle.getCapacity());
                vehicleData.put("totalDemand", vehicle.getTotalDemand());
                vehicleData.put("totalDrivingTimeSeconds", vehicle.getTotalDrivingTimeSeconds());

                // Frontend expects "location" property with [lat, lon] array
                double[] location = {
                    vehicle.getHomeLocation().getLatitude(),
                    vehicle.getHomeLocation().getLongitude()
                };
                vehicleData.put("location", location);

                // Also include homeLocation for compatibility
                vehicleData.put("homeLocation", location);

                // Add visit assignments
                List<Map<String, Object>> assignedVisits = new ArrayList<>();
                if (vehicle.getVisits() != null) {
                    for (Visit visit : vehicle.getVisits()) {
                        if (visit != null) {
                            assignedVisits.add(convertVisitToMap(visit));
                        }
                    }
                }
                vehicleData.put("visits", assignedVisits);

                frontendVehicles.add(vehicleData);
            }
        }

        // Convert visits
        List<Map<String, Object>> frontendVisits = new ArrayList<>();
        for (Visit visit : plan.getVisits()) {
            if (visit != null && visit.getLocation() != null) {
                frontendVisits.add(convertVisitToMap(visit));
            }
        }

        // Assemble frontend data
        data.put("vehicles", frontendVehicles);
        data.put("visits", frontendVisits);
        data.put("score", plan.getScore() != null ? plan.getScore().toString() : "0hard/0soft");
        data.put("solverStatus", plan.getSolverStatus() != null ? plan.getSolverStatus().toString() : "NOT_SOLVING");
        data.put("name", plan.getName());
        data.put("totalDrivingTimeSeconds", plan.getTotalDrivingTimeSeconds());

        // Add bounds
        if (plan.getSouthWestCorner() != null && plan.getNorthEastCorner() != null) {
            data.put("southWestCorner", new double[]{
                plan.getSouthWestCorner().getLatitude(),
                plan.getSouthWestCorner().getLongitude()
            });
            data.put("northEastCorner", new double[]{
                plan.getNorthEastCorner().getLatitude(),
                plan.getNorthEastCorner().getLongitude()
            });
        }

        // Debug info
        data.put("vehicleCount", frontendVehicles.size());
        data.put("visitCount", frontendVisits.size());

        return data;
    }

    private Map<String, Object> convertVisitToMap(Visit visit) {
        Map<String, Object> visitData = new HashMap<>();
        visitData.put("id", visit.getId());
        visitData.put("name", visit.getName());
        visitData.put("demand", visit.getDemand());

        // CRITICAL: Frontend expects "location" property with [lat, lon] array
        if (visit.getLocation() != null) {
            double[] location = {
                visit.getLocation().getLatitude(),
                visit.getLocation().getLongitude()
            };
            visitData.put("location", location);
        }

        // Add time windows if available
        if (visit.getMinStartTime() != null) {
            visitData.put("minStartTime", visit.getMinStartTime().toString());
        }
        if (visit.getMaxEndTime() != null) {
            visitData.put("maxEndTime", visit.getMaxEndTime().toString());
        }
        if (visit.getArrivalTime() != null) {
            visitData.put("arrivalTime", visit.getArrivalTime().toString());
        }
        if (visit.getDepartureTime() != null) {
            visitData.put("departureTime", visit.getDepartureTime().toString());
        }

        visitData.put("serviceDuration", visit.getServiceDuration());

        return visitData;
    }

    /**
     * Hardcoded fallback data in frontend-compatible format
     */
    /* private Response getHardcodedFrontendData() {
        System.out.println("Using hardcoded frontend-compatible fallback data");

        Map<String, Object> data = new HashMap<>();

        // Create frontend-compatible vehicles
        List<Map<String, Object>> vehicles = new ArrayList<>();

        Map<String, Object> vehicle1 = new HashMap<>();
        vehicle1.put("id", "vehicle-1");
        vehicle1.put("style", "motorcycle");
        vehicle1.put("capacity", 5);
        vehicle1.put("location", new double[]{19.076, 72.8777}); // Mumbai
        vehicle1.put("homeLocation", new double[]{19.076, 72.8777});
        vehicle1.put("visits", new ArrayList<>());
        vehicles.add(vehicle1);

        Map<String, Object> vehicle2 = new HashMap<>();
        vehicle2.put("id", "vehicle-2");
        vehicle2.put("style", "van");
        vehicle2.put("capacity", 10);
        vehicle2.put("location", new double[]{18.5204, 73.8567}); // Pune
        vehicle2.put("homeLocation", new double[]{18.5204, 73.8567});
        vehicle2.put("visits", new ArrayList<>());
        vehicles.add(vehicle2);

        // Create frontend-compatible visits
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

        data.put("vehicles", vehicles);
        data.put("visits", visits);
        data.put("score", "0hard/0soft");
        data.put("solverStatus", "NOT_SOLVING");
        data.put("vehicleCount", vehicles.size());
        data.put("visitCount", visits.size());

        return Response.ok(data).build();
    } */

    @GET
    @Path("/route-plans//health")
    public Response health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Vehicle Routing (Frontend-Compatible)");
        health.put("timestamp", System.currentTimeMillis());
        return Response.ok(health).build();
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/route-plans/analyze")
    public ScoreAnalysis<HardSoftLongScore> analyze(VehicleRoutePlan problem,
                                                    @QueryParam("fetchPolicy") ScoreAnalysisFetchPolicy fetchPolicy) {
        return fetchPolicy == null ? solutionManager.analyze(problem) : solutionManager.analyze(problem, fetchPolicy);
    }

    private record Job(VehicleRoutePlan routePlan, Throwable exception) {

        static Job ofRoutePlan(VehicleRoutePlan routePlan) {
            return new Job(routePlan, null);
        }

        static Job ofException(Throwable exception) {
            return new Job(null, exception);
        }

    }
}