
package org.acme.vehiclerouting.service;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverJob;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class VehicleRoutingService {

    @Inject
    SolverManager<VehicleRoutePlan, UUID> solverManager;

    @Inject
    VehicleRoutingDataService dataService;

    private VehicleRoutePlan currentProblem;
    private SolverJob<VehicleRoutePlan, UUID> currentSolverJob;

    public VehicleRoutePlan getProblem() {
    if (currentProblem == null) {
        // currentProblem = dataService.createSafeProblem();
    }
    return currentProblem;
}
    public CompletableFuture<VehicleRoutePlan> solveAsync(VehicleRoutePlan problem) {
        UUID problemId = UUID.randomUUID();
        stopSolving();

        System.out.println("Starting to solve problem with " + 
            problem.getVehicles().size() + " vehicles and " + 
            problem.getVisits().size() + " visits");

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
            System.out.println("Stopping current solver job");
            currentSolverJob.terminateEarly();
            currentSolverJob = null;
        }
    }

    /**
     * Reset problem data to force reload
     */
    public void resetProblem() {
        currentProblem = null;
    }

    // /**
    //  * Switch to full Amazon dataset
    //  */
    // public VehicleRoutePlan loadFullDataset() {
    //     currentProblem = dataService.createAmazonDeliveryProblem();
    //     System.out.println("Loaded FULL Amazon dataset with " + 
    //         currentProblem.getVehicles().size() + " vehicles and " + 
    //         currentProblem.getVisits().size() + " visits");
    //     return currentProblem;
    // }
}