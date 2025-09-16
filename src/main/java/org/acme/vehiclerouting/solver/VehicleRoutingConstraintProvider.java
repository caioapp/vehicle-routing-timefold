package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                vehicleCapacity(constraintFactory),
                timeWindows(constraintFactory),
                limitVisitsPerVehicle(constraintFactory),

                // Soft constraints  
                minimizeTravelTime(constraintFactory),
                penalizeUnusedVehicles(constraintFactory),
                balanceWorkload(constraintFactory)
        };
    }

    // CRITICAL: Hard constraint for vehicle capacity
    Constraint vehicleCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Vehicle.class)
                .filter(vehicle -> vehicle.getTotalDemand() > vehicle.getCapacity())
                .penalize(HardSoftLongScore.ONE_HARD,
                        vehicle -> vehicle.getTotalDemand() - vehicle.getCapacity())
                .asConstraint("Vehicle capacity exceeded");
    }

    // CRITICAL: Hard constraint for time windows
    Constraint timeWindows(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Visit.class)
                .filter(visit -> visit.getArrivalTime() != null && 
                        (visit.getArrivalTime().isBefore(visit.getMinStartTime()) ||
                         visit.getArrivalTime().isAfter(visit.getMaxEndTime())))
                .penalize(HardSoftLongScore.ONE_HARD, visit -> 1000)
                .asConstraint("Time window violation");
    }

    // SOLUTION: Force distribution across vehicles
    Constraint limitVisitsPerVehicle(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Vehicle.class)
                .filter(vehicle -> vehicle.getVisits().size() > 12) // Max 12 visits per vehicle
                .penalize(HardSoftLongScore.ONE_HARD,
                        vehicle -> (vehicle.getVisits().size() - 12) * 100)
                .asConstraint("Too many visits per vehicle");
    }

    // SOLUTION: Penalize unused vehicles to force distribution
    Constraint penalizeUnusedVehicles(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Vehicle.class)
                .filter(vehicle -> vehicle.getVisits().isEmpty())
                .penalize(HardSoftLongScore.ONE_SOFT, vehicle -> 2000) // Heavy penalty for unused vehicles
                .asConstraint("Unused vehicle penalty");
    }

    // SOLUTION: Balance workload across vehicles
    Constraint balanceWorkload(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Vehicle.class)
                .filter(vehicle -> !vehicle.getVisits().isEmpty())
                .penalize(HardSoftLongScore.ONE_SOFT,
                        vehicle -> (int) vehicle.getTotalDrivingTimeSeconds() / 3600) // Penalize long routes
                .asConstraint("Balance workload");
    }

    // Existing: Minimize total travel time
    Constraint minimizeTravelTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Vehicle.class)
                .penalize(HardSoftLongScore.ONE_SOFT,
                        vehicle -> (int) vehicle.getTotalDrivingTimeSeconds())
                .asConstraint("Minimize total travel time");
    }
}