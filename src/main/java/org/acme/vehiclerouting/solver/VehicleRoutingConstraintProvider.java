
package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    public static final String MINIMIZE_TRAVEL_TIME = "minimizeTravelTime";
    public static final String VEHICLE_CAPACITY = "vehicleCapacity";


    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            vehicleCapacity(constraintFactory),
            minimizeTotalVehicles(constraintFactory),
            minimizeUnassignedVisits(constraintFactory),
            minimizeTravelTime(constraintFactory)
        };
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

    protected Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .filter(vehicle -> vehicle.getTotalDemand() > vehicle.getCapacity())
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        vehicle -> vehicle.getTotalDemand() - vehicle.getCapacity())
                .asConstraint(VEHICLE_CAPACITY);
    }

    protected Constraint minimizeTravelTime(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        Vehicle::getTotalDrivingTimeSeconds)
                .asConstraint(MINIMIZE_TRAVEL_TIME);
    }
}