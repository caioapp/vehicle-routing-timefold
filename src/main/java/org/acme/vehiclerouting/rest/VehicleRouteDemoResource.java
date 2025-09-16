package org.acme.vehiclerouting.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

@Tag(name = "Vehicle Routing", description = "Vehicle Routing service.")
@Path("route")
public class VehicleRouteDemoResource {

    private final SolverManager<VehicleRoutePlan, String> solverManager;
    private final Map<String, VehicleRoutePlan> solutions = new HashMap<>();

    public VehicleRouteDemoResource(SolverManager<VehicleRoutePlan, String> solverManager) {
        this.solverManager = solverManager;
    }

    @Operation(summary = "Solve the vehicle routing problem.")
    @APIResponses(value = {
            @APIResponse(responseCode = "202", description = "Solving started."),
            @APIResponse(responseCode = "400", description = "Problem not found.")
    })
    @POST
    @Path("solve/{problemId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.TEXT_PLAIN)
    public String solve(@Parameter(description = "The problem ID.") @PathParam("problemId") String problemId) {
        solverManager.solveBuilder();//problemId, solutions::get, solutions::put);
        return problemId;
    }

    @Operation(summary = "Get the vehicle routing solution.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The best solution found so far."),
            @APIResponse(responseCode = "404", description = "Solution not found.")
    })
    @GET
    @Path("solution/{problemId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public VehicleRoutePlan solution(@Parameter(description = "The problem ID.") @PathParam("problemId") String problemId) {
        return solutions.get(problemId);
    }

    @Operation(summary = "Get the solver status.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The solver status."),
    })
    @GET
    @Path("status/{problemId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String status(@Parameter(description = "The problem ID.") @PathParam("problemId") String problemId) {
        SolverStatus solverStatus = solverManager.getSolverStatus(problemId);
        return solverStatus.toString();
    }

    @Operation(summary = "Generate a vehicle routing problem from the amazon_delivery.csv file.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The generated problem."),
            @APIResponse(responseCode = "500", description = "Failed to read the CSV file.")
    })
    @GET
    @Path("problem")
    @Produces({ MediaType.APPLICATION_JSON })
    public VehicleRoutePlan problem() {
        try {
            VehicleRoutePlan problem = CsvVehicleRoutePlanProvider.readProblem();
            solutions.put(problem.getName(), problem);
            return problem;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the CSV file.", e);
        }
    }
}

class CsvVehicleRoutePlanProvider {

    private static final String CSV_FILE_PATH = "/data/amazon_delivery.csv";

    public static VehicleRoutePlan readProblem() throws IOException {
        List<Visit> visits = new ArrayList<>();
        Location depotLocation = null;
        List<Vehicle> vehicles = new ArrayList<>();
        AtomicLong visitIdSequence = new AtomicLong();
        // Random random = new Random(37);

        try (InputStream inputStream = CsvVehicleRoutePlanProvider.class.getResourceAsStream(CSV_FILE_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                if (depotLocation == null) {
                    double depotLat = Double.parseDouble(data[3]);
                    double depotLon = Double.parseDouble(data[4]);
                    depotLocation = new Location(depotLat, depotLon);
                }

                double dropLat = Double.parseDouble(data[5]);
                double dropLon = Double.parseDouble(data[6]);
                Location visitLocation = new Location(dropLat, dropLon);
                
                String orderDateStr = data[7];
                String pickupTimeStr = data[9];
                LocalDate orderDate = LocalDate.parse(orderDateStr);
                LocalTime pickupTime = LocalTime.parse(pickupTimeStr);
                LocalDateTime serviceWindowStart = LocalDateTime.of(orderDate, pickupTime);
                
                visits.add(new Visit(
                        String.valueOf(visitIdSequence.incrementAndGet()),
                        data[0], // Order_ID as visit name
                        visitLocation,
                        1, // demand
                        serviceWindowStart,
                        serviceWindowStart.plusHours(2), // 2 hour service window
                        Duration.ofMinutes(15) // 15 minute service duration
                ));
            }
        }

        // Create vehicles
        if (depotLocation != null) {
            vehicles.add(new Vehicle("1", "motorcycle", depotLocation, tomorrowAt(LocalTime.of(8,0))));
            vehicles.add(new Vehicle("2", "scooter", depotLocation, tomorrowAt(LocalTime.of(8,0))));
            vehicles.add(new Vehicle("3", "van", depotLocation, tomorrowAt(LocalTime.of(8,0))));
        }

        return new VehicleRoutePlan("amazon-delivery-problem",
                new Location(0, 0), new Location(100, 100),
                tomorrowAt(LocalTime.of(8, 0)),
                tomorrowAt(LocalTime.MIDNIGHT).plusDays(1L),
                vehicles,
                visits);
    }
    
    private static LocalDateTime tomorrowAt(LocalTime time) {
        return LocalDateTime.of(LocalDate.now().plusDays(1L), time);
    }
}