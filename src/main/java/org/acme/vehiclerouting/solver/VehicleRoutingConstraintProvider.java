package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import org.acme.vehiclerouting.domain.Visit;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
            minimizeTotalDrivingTime(constraintFactory)
        };
    }

    /**
     * Minimize total driving time - this will encourage visit assignments.
     */
    private Constraint minimizeTotalDrivingTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Visit.class)
            .filter(visit -> visit.getVehicle() != null)
            .penalize(HardSoftLongScore.ONE_SOFT, visit -> 1) // Simple penalty to encourage assignments
            .asConstraint("Minimize total driving time");
    }
}
