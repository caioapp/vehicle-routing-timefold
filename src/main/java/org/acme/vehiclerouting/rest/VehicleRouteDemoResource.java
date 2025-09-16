package org.acme.vehiclerouting.rest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.util.CSVDataLoader;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

@Tag(name = "Vehicle Routing Demo", description = "Vehicle routing demo service with CSV data support.")
@Path("demo-data")
public class VehicleRouteDemoResource {

    private final SolverManager<VehicleRoutePlan, String> solverManager;
    private final ConcurrentMap<String, VehicleRoutePlan> solutions = new ConcurrentHashMap<>();

    public VehicleRouteDemoResource(SolverManager<VehicleRoutePlan, String> solverManager) {
        this.solverManager = solverManager;
    }

    @Operation(summary = "Get demo data for the specified dataset.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Demo data generated successfully."),
        @APIResponse(responseCode = "404", description = "Dataset not found."),
        @APIResponse(responseCode = "500", description = "Error generating demo data.")
    })
    @GET
    @Path("{datasetName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDemoData(@Parameter(description = "The dataset name") @PathParam("datasetName") String datasetName) {
        try {
            VehicleRoutePlan problem;

            switch (datasetName.toUpperCase()) {
                case "FIRENZE":
                    problem = generateFirenzeData();
                    break;
                case "CSV":
                case "AMAZON":
                    problem = generateCSVData();
                    break;
                case "CSV_SMALL":
                    problem = generateSmallCSVData();
                    break;
                case "CSV_LARGE":
                    problem = generateLargeCSVData();
                    break;
                default:
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Dataset not found: " + datasetName)
                            .build();
            }

            solutions.put(problem.getName(), problem);
            return Response.ok(problem).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error generating demo data: " + e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "Get available demo datasets.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "List of available datasets.")
    })
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAvailableDatasets() {
        List<String> datasets = new ArrayList<>();
        datasets.add("FIRENZE");
        datasets.add("CSV");
        datasets.add("AMAZON");
        datasets.add("CSV_SMALL");
        datasets.add("CSV_LARGE");
        return datasets;
    }

    @Operation(summary = "Solve a demo problem directly.")
    @APIResponses(value = {
        @APIResponse(responseCode = "202", description = "Solving started."),
        @APIResponse(responseCode = "404", description = "Dataset not found.")
    })
    @POST
    @Path("solve/{datasetName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response solveDemoData(@Parameter(description = "The dataset name") @PathParam("datasetName") String datasetName) {
        try {
            VehicleRoutePlan problem;

            switch (datasetName.toUpperCase()) {
                case "FIRENZE":
                    problem = generateFirenzeData();
                    break;
                case "CSV":
                case "AMAZON":
                    problem = generateCSVData();
                    break;
                case "CSV_SMALL":
                    problem = generateSmallCSVData();
                    break;
                case "CSV_LARGE":
                    problem = generateLargeCSVData();
                    break;
                default:
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Dataset not found: " + datasetName)
                            .build();
            }

            String jobId = datasetName + "-" + System.currentTimeMillis();
            solutions.put(jobId, problem);

            solverManager.solveBuilder()
                    .withProblemId(jobId)
                    .withProblemFinder(id -> solutions.get(id))
                    .withBestSolutionConsumer(solution -> solutions.put(jobId, solution))
                    .run();

            return Response.status(Response.Status.ACCEPTED).entity(jobId).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error starting solver: " + e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "Get solution status for a demo problem.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Solution status."),
        @APIResponse(responseCode = "404", description = "Job not found.")
    })
    @GET
    @Path("status/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSolutionStatus(@Parameter(description = "The job ID") @PathParam("jobId") String jobId) {
        if (!solutions.containsKey(jobId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        SolverStatus status = solverManager.getSolverStatus(jobId);
        VehicleRoutePlan solution = solutions.get(jobId);

        return Response.ok(new SolutionStatus(
                jobId, 
                status.name(), 
                solution != null ? solution.getScore() : null,
                solution != null ? solution.getTotalDrivingTimeSeconds() : 0
        )).build();
    }

    @Operation(summary = "Get the solution for a demo problem.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Solution data."),
        @APIResponse(responseCode = "404", description = "Job not found.")
    })
    @GET
    @Path("solution/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSolution(@Parameter(description = "The job ID") @PathParam("jobId") String jobId) {
        VehicleRoutePlan solution = solutions.get(jobId);
        if (solution == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(solution).build();
    }

    // Helper methods for generating different types of demo data

    private VehicleRoutePlan generateFirenzeData() {
        // Generate the original Firenze demo data
        List<Vehicle> vehicles = new ArrayList<>();
        List<Visit> visits = new ArrayList<>();

        // Firenze coordinates (Florence, Italy)
        Location depot = new Location(43.7696, 11.2558);

        vehicles.add(new Vehicle("Vehicle-1", "van", depot, tomorrowAt(LocalTime.of(8, 0))));
        vehicles.add(new Vehicle("Vehicle-2", "truck", depot, tomorrowAt(LocalTime.of(8, 0))));

        // Add some visits around Florence
        visits.add(createVisit("1", "Duomo", new Location(43.7731, 11.2560), 1));
        visits.add(createVisit("2", "Ponte Vecchio", new Location(43.7679, 11.2530), 2));
        visits.add(createVisit("3", "Uffizi", new Location(43.7677, 11.2553), 1));
        visits.add(createVisit("4", "Palazzo Pitti", new Location(43.7650, 11.2500), 3));
        visits.add(createVisit("5", "Santa Croce", new Location(43.7687, 11.2625), 1));

        return new VehicleRoutePlan(
                "firenze-demo",
                new Location(43.7500, 11.2300),
                new Location(43.7800, 11.2800),
                tomorrowAt(LocalTime.of(8, 0)),
                tomorrowAt(LocalTime.of(18, 0)),
                vehicles,
                visits
        );
    }

    private VehicleRoutePlan generateCSVData() throws IOException {
        CSVDataLoader.CSVLoadResult result = CSVDataLoader.loadFromCSV(100, true);
        return result.getProblem();
    }

    private VehicleRoutePlan generateSmallCSVData() throws IOException {
        CSVDataLoader.CSVLoadResult result = CSVDataLoader.createTestProblem();
        VehicleRoutePlan problem = result.getProblem();

        // Rename for clarity
        return new VehicleRoutePlan(
                "amazon-delivery-small",
                problem.getSouthWestCorner(),
                problem.getNorthEastCorner(),
                problem.getStartDateTime(),
                problem.getEndDateTime(),
                problem.getVehicles(),
                problem.getVisits()
        );
    }

    private VehicleRoutePlan generateLargeCSVData() throws IOException {
        CSVDataLoader.CSVLoadResult result = CSVDataLoader.createPerformanceTestProblem();
        VehicleRoutePlan problem = result.getProblem();

        // Rename for clarity
        return new VehicleRoutePlan(
                "amazon-delivery-large",
                problem.getSouthWestCorner(),
                problem.getNorthEastCorner(),
                problem.getStartDateTime(),
                problem.getEndDateTime(),
                problem.getVehicles(),
                problem.getVisits()
        );
    }

    private Visit createVisit(String id, String name, Location location, int demand) {
        LocalDateTime tomorrow9AM = tomorrowAt(LocalTime.of(9, 0));
        return new Visit(
                id,
                name,
                location,
                demand,
                tomorrow9AM,
                tomorrow9AM.plusHours(8), // 8-hour window
                java.time.Duration.ofMinutes(30) // 30-minute service time
        );
    }

    private LocalDateTime tomorrowAt(LocalTime time) {
        return LocalDateTime.now().plusDays(1).withHour(time.getHour()).withMinute(time.getMinute()).withSecond(0).withNano(0);
    }

    // Inner class for solution status response
    public static class SolutionStatus {
        public final String jobId;
        public final String solverStatus;
        public final Object score;
        public final long totalDrivingTimeSeconds;

        public SolutionStatus(String jobId, String solverStatus, Object score, long totalDrivingTimeSeconds) {
            this.jobId = jobId;
            this.solverStatus = solverStatus;
            this.score = score;
            this.totalDrivingTimeSeconds = totalDrivingTimeSeconds;
        }
    }
}