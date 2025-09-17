
package org.acme.vehiclerouting.solver;

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

            // Soft constraints (simplified to avoid infinite loops)
            minimizeTotalVehicles(constraintFactory),
            minimizeUnassignedVisits(constraintFactory)
        };
    }

    // CRITICAL: Simple vehicle capacity constraint (no complex calculations)
    Constraint vehicleCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Vehicle.class)
            .filter(vehicle -> vehicle.getVisits() != null && 
                             vehicle.getVisits().size() > vehicle.getCapacity())
            .penalize(HardSoftLongScore.ONE_HARD,
                vehicle -> vehicle.getVisits().size() - vehicle.getCapacity())
            .asConstraint("Vehicle capacity exceeded");
    }

    // SIMPLE: Minimize number of vehicles used
    Constraint minimizeTotalVehicles(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Vehicle.class)
            .filter(vehicle -> vehicle.getVisits() != null && !vehicle.getVisits().isEmpty())
            .penalize(HardSoftLongScore.ONE_SOFT, vehicle -> 1)
            .asConstraint("Minimize vehicles used");
    }

    // SIMPLE: Penalize unassigned visits
    Constraint minimizeUnassignedVisits(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Visit.class)
            .filter(visit -> visit.getVehicle() == null)
            .penalize(HardSoftLongScore.of(0, 1000), visit -> 1)
            .asConstraint("Minimize unassigned visits");
    }
}