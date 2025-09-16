package org.acme.vehiclerouting.service;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.Location;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverJob;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.xml.datatype.Duration;

import java.time.LocalDateTime;

@ApplicationScoped
public class VehicleRoutingService {

    @Inject
    SolverManager<VehicleRoutePlan, UUID> solverManager;

    private VehicleRoutePlan currentProblem;
    private SolverJob<VehicleRoutePlan, UUID> currentSolverJob;

    public VehicleRoutePlan getProblem() {
        if (currentProblem == null) {
            currentProblem = createSampleProblem();
        }
        return currentProblem;
    }

    public CompletableFuture<VehicleRoutePlan> solveAsync(VehicleRoutePlan problem) {
        UUID problemId = UUID.randomUUID();
        stopSolving();
        currentSolverJob = solverManager.solve(problemId, problem);
        try {
            return CompletableFuture.completedFuture(currentSolverJob.getFinalBestSolution());
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            CompletableFuture<VehicleRoutePlan> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    public void stopSolving() {
        if (currentSolverJob != null) {
            currentSolverJob.terminateEarly();
            currentSolverJob = null;
        }
    }

    private VehicleRoutePlan createSampleProblem() {
        List<Vehicle> vehicles = new ArrayList<>();
        List<Visit> visits = new ArrayList<>();

        // CORRECTED: Use Location objects and LocalDateTime
        Vehicle vehicle1 = new Vehicle("vehicle-1", "motorcycle", 
                                     new Location(23.374878, 85.335739),
                                     LocalDateTime.now().plusHours(1));
        vehicles.add(vehicle1);

        Vehicle vehicle2 = new Vehicle("vehicle-2", "scooter", 
                                     new Location(21.186438, 72.794115),
                                     LocalDateTime.now().plusHours(1));
        vehicles.add(vehicle2);

        Vehicle vehicle3 = new Vehicle("vehicle-3", "van", 
                                     new Location(18.55144, 73.804855),
                                     LocalDateTime.now().plusHours(1));
        vehicles.add(vehicle3);

        // CORRECTED: Use Location objects
        Visit visit1 = new Visit("1", "Sample Delivery 1", 
                               new Location(22.765049, 75.912471),
                               1, LocalDateTime.now().plusHours(2),
                               LocalDateTime.now().plusHours(6));
        visits.add(visit1);

        Visit visit2 = new Visit("2", "Sample Delivery 2", 
                               new Location(13.043041, 77.813237),
                               1, LocalDateTime.now().plusHours(3),
                               LocalDateTime.now().plusHours(7));
        visits.add(visit2);

        Visit visit3 = new Visit("3", "Sample Delivery 3", 
                               new Location(12.924264, 77.6884),
                               1, LocalDateTime.now().plusHours(2),
                               LocalDateTime.now().plusHours(8));
        visits.add(visit3);

        return new VehicleRoutePlan(visits, vehicles);
    }
}