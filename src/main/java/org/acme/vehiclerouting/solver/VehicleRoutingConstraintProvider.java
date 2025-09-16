package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
// import org.acme.vehiclerouting.domain.DeliveryAgent;
// import org.acme.vehiclerouting.domain.DeliveryOrder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Enhanced constraint provider for the delivery routing problem with improved performance.
 * Defines both hard constraints (must be satisfied) and soft constraints (optimization goals).
 */
public class VehicleRoutingConstraintProvider implements ConstraintProvider {
    
    // Constraint identifiers
    public static final String AGENT_CAPACITY = "Agent capacity limit";
    public static final String VEHICLE_COMPATIBILITY = "Vehicle compatibility";
    public static final String AREA_COMPATIBILITY = "Area compatibility";
    public static final String SHIFT_TIME_COMPLIANCE = "Shift time compliance";
    public static final String MINIMIZE_TRAVEL_TIME = "Minimize travel time";
    public static final String BALANCE_WORKLOAD = "Balance workload";
    public static final String PRIORITIZE_HIGH_RATING_AGENTS = "Prioritize high-rating agents";
    public static final String MINIMIZE_LATE_DELIVERIES = "Minimize late deliveries";

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
            // Hard constraints (must not be violated)
            // agentCapacity(constraintFactory),
            // vehicleCompatibility(constraintFactory),
            // areaCompatibility(constraintFactory),
            // shiftTimeCompliance(constraintFactory),
            
            // Soft constraints (optimization goals)
            // minimizeTravelTime(constraintFactory),
            // balanceWorkload(constraintFactory),
            // prioritizeHighRatingAgents(constraintFactory),
            // minimizeLateDeliveries(constraintFactory)
        };
    }

    // ==============================================================
    // HARD CONSTRAINTS
    // ==============================================================

    /**
     * Hard constraint: Agent cannot be assigned more orders than their maximum capacity.
     * This prevents overloading agents beyond their operational limits.
     */
    // private Constraint agentCapacity(ConstraintFactory constraintFactory) {
    //     return constraintFactory.forEach(DeliveryAgent.class)
    //             .filter(agent -> agent.getOrderCount() > agent.getMaxOrdersPerShift())
    //             .penalize(HardSoftLongScore.ONE_HARD,
    //                     agent -> (int) (agent.getOrderCount() - agent.getMaxOrdersPerShift()))
    //             .asConstraint(AGENT_CAPACITY);
    // }

    /**
     * Hard constraint: Orders must be assigned to agents with compatible vehicle types.
     * Ensures that delivery requirements match agent capabilities.
     */
    // private Constraint vehicleCompatibility(ConstraintFactory constraintFactory) {
    //     return constraintFactory.forEach(DeliveryOrder.class)
    //             .filter(order -> order.getAssignedAgent() != null)
    //             .filter(order -> !order.isCompatibleWithVehicle(order.getAssignedAgent().getVehicleType()))
    //             .penalize(HardSoftLongScore.ONE_HARD)
    //             .asConstraint(VEHICLE_COMPATIBILITY);
    // }

    /**
     * Hard constraint: Orders must be assigned to agents who can work in the delivery area.
     * Ensures agents only work in their designated geographical regions.
     */
    // private Constraint areaCompatibility(ConstraintFactory constraintFactory) {
    //     return constraintFactory.forEach(DeliveryOrder.class)
    //             .filter(order -> order.getAssignedAgent() != null)
    //             .filter(order -> !order.getAssignedAgent().canWorkInArea(order.getArea()))
    //             .penalize(HardSoftLongScore.ONE_HARD)
    //             .asConstraint(AREA_COMPATIBILITY);
    // }

    /**
     * Hard constraint: All deliveries must be completed within agent shift times.
     * Ensures no deliveries are scheduled outside working hours.
     */
    // private Constraint shiftTimeCompliance(ConstraintFactory constraintFactory) {
    //     return constraintFactory.forEach(DeliveryOrder.class)
    //             .filter(order -> order.getAssignedAgent() != null)
    //             .filter(order -> order.getEstimatedArrivalTime() != null)
    //             .filter(order -> !isDeliveryWithinShiftTime(order))
    //             .penalize(HardSoftLongScore.ONE_HARD,
    //                     order -> (int) calculateShiftViolationMinutes(order))
    //             .asConstraint(SHIFT_TIME_COMPLIANCE);
    // }

    // ==============================================================
    // SOFT CONSTRAINTS (OPTIMIZATION GOALS)
    // ==============================================================

    /**
     * Soft constraint: Minimize total travel time across all agents.
     * Reduces overall operational costs and delivery times.
     */
    // private Constraint minimizeTravelTime(ConstraintFactory constraintFactory) {
    //     return constraintFactory.forEach(DeliveryOrder.class)
    //             .filter(order -> order.getAssignedAgent() != null)
    //             .penalize(HardSoftLongScore.ONE_SOFT,
    //                     order -> (int) order.getTotalTravelTimeSeconds() / 60) // Convert to minutes
    //             .asConstraint(MINIMIZE_TRAVEL_TIME);
    // }

    /**
     * Soft constraint: Balance workload distribution across agents.
     * Prevents some agents from being overloaded while others are underutilized.
     */
    // private Constraint balanceWorkload(ConstraintFactory constraintFactory) {
    //     return constraintFactory.forEach(DeliveryAgent.class)
    //             .penalize(HardSoftLongScore.ONE_SOFT,
    //                     agent -> (int) calculateWorkloadImbalancePenalty(agent))
    //             .asConstraint(BALANCE_WORKLOAD);
    // }

    /**
     * Soft constraint: Prioritize assigning orders to higher-rated agents.
     * Improves service quality by utilizing the best available agents.
     */
    // private Constraint prioritizeHighRatingAgents(ConstraintFactory constraintFactory) {
    //     return constraintFactory.forEach(DeliveryOrder.class)
    //             .filter(order -> order.getAssignedAgent() != null)
    //             .reward(HardSoftLongScore.ONE_SOFT,
    //                     order -> (int) Math.round(order.getAssignedAgent().getRating() * 20)) // Rating * 20 for scaling
    //             .asConstraint(PRIORITIZE_HIGH_RATING_AGENTS);
    // }

    /**
     * Soft constraint: Minimize late deliveries and prioritize urgent orders.
     * Improves customer satisfaction by handling time-sensitive deliveries first.
     */
    // private Constraint minimizeLateDeliveries(ConstraintFactory constraintFactory) {
    //     return constraintFactory.forEach(DeliveryOrder.class)
    //             .filter(order -> order.getAssignedAgent() != null)
    //             .penalize(HardSoftLongScore.ONE_SOFT,
    //                     order -> (int) calculateUrgencyPenalty(order))
    //             .asConstraint(MINIMIZE_LATE_DELIVERIES);
    // }

    // ==============================================================
    // HELPER METHODS FOR CONSTRAINT CALCULATIONS
    // ==============================================================

    /**
     * Check if a delivery can be completed within the agent's shift time.
     */
    // private boolean isDeliveryWithinShiftTime(DeliveryOrder order) {
    //     DeliveryAgent agent = order.getAssignedAgent();
    //     LocalTime pickupTime = order.getPickupTime();
    //     LocalTime estimatedArrivalTime = order.getEstimatedArrivalTime().toLocalTime();
        
    //     return agent.isAvailableDuring(pickupTime, estimatedArrivalTime);
    // }

    /**
     * Calculate how many minutes a delivery violates shift time constraints.
     */
    // private long calculateShiftViolationMinutes(DeliveryOrder order) {
    //     DeliveryAgent agent = order.getAssignedAgent();
    //     LocalTime estimatedArrival = order.getEstimatedArrivalTime().toLocalTime();
        
    //     if (estimatedArrival.isAfter(agent.getShiftEnd())) {
    //         return Duration.between(agent.getShiftEnd(), estimatedArrival).toMinutes();
    //     }
        
    //     if (order.getPickupTime().isBefore(agent.getShiftStart())) {
    //         return Duration.between(order.getPickupTime(), agent.getShiftStart()).toMinutes();
    //     }
        
    //     return 0L;
    // }

    /**
     * Calculate workload imbalance penalty for an agent.
     * Penalizes both under-utilization and over-utilization.
     */
    // private long calculateWorkloadImbalancePenalty(DeliveryAgent agent) {
    //     if (agent.getMaxOrdersPerShift() == 0) {
    //         return 0L; // Avoid division by zero
    //     }
        
    //     double utilization = (double) agent.getOrderCount() / agent.getMaxOrdersPerShift();
        
    //     // Ideal utilization range: 70-90%
    //     if (utilization < 0.3) {
    //         // Heavily penalize severe under-utilization (waste of resources)
    //         return Math.round((0.3 - utilization) * 1000);
    //     } else if (utilization < 0.7) {
    //         // Moderate penalty for under-utilization
    //         return Math.round((0.7 - utilization) * 300);
    //     } else if (utilization <= 0.9) {
    //         // Optimal range - no penalty
    //         return 0L;
    //     } else {
    //         // Penalty for over-utilization (potential quality issues)
    //         return Math.round((utilization - 0.9) * 800);
    //     }
    // }

    /**
     * Calculate urgency penalty based on pickup time and delivery expectations.
     * Higher penalty for orders that are becoming late or urgent.
     */
    // private long calculateUrgencyPenalty(DeliveryOrder order) {
    //     long basePenalty = order.getUrgencyScore();
        
    //     // Additional penalty if estimated delivery time exceeds expected time
    //     if (order.getEstimatedArrivalTime() != null) {
    //         LocalDateTime expectedCompletion = order.getPickupDateTime()
    //                 .plusMinutes(order.getEstimatedDeliveryTimeMinutes());
            
    //         if (order.getEstimatedArrivalTime().isAfter(expectedCompletion)) {
    //             long delayMinutes = Duration.between(expectedCompletion, 
    //                                                order.getEstimatedArrivalTime()).toMinutes();
    //             basePenalty += delayMinutes * 3; // 3x penalty for delays
    //         }
    //     }
        
    //     // Category-based urgency multipliers
    //     switch (order.getCategory().toLowerCase()) {
    //         case "food":
    //             basePenalty *= 2; // Food deliveries are more time-sensitive
    //             break;
    //         case "medical":
    //         case "pharmacy":
    //             basePenalty *= 3; // Medical deliveries are critical
    //             break;
    //         case "electronics":
    //             basePenalty *= 1.5; // Electronics are moderately time-sensitive
    //             break;
    //         default:
    //             // No additional multiplier for other categories
    //             break;
    //     }
        
    //     return basePenalty;
    // }

    // ==============================================================
    // ADVANCED CONSTRAINTS (OPTIONAL - UNCOMMENT IF NEEDED)
    // ==============================================================

    /**
     * Optional: Minimize agent travel distance between consecutive deliveries.
     * Uncomment and add to constraint array if route optimization is needed.
     */
    /*
    private Constraint minimizeAgentTravelDistance(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DeliveryOrder.class)
                .join(DeliveryOrder.class,
                        Joiners.equal(DeliveryOrder::getAssignedAgent),
                        Joiners.lessThan(DeliveryOrder::getPickupTime))
                .penalize(HardSoftLongScore.ONE_SOFT,
                        (order1, order2) -> calculateDistanceBetweenOrders(order1, order2))
                .asConstraint("Minimize travel distance between orders");
    }

    private long calculateDistanceBetweenOrders(DeliveryOrder first, DeliveryOrder second) {
        // Distance from first delivery's drop location to second delivery's store location
        return first.getDropLocation().getDrivingTimeTo(second.getStoreLocation()) / 60;
    }
    */

    /**
     * Optional: Prefer grouping orders by category for the same agent.
     * Uncomment and add to constraint array if category grouping is beneficial.
     */
    /*
    private Constraint groupOrdersByCategory(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DeliveryOrder.class)
                .join(DeliveryOrder.class,
                        Joiners.equal(DeliveryOrder::getAssignedAgent),
                        Joiners.equal(DeliveryOrder::getCategory))
                .filter((order1, order2) -> !order1.equals(order2))
                .reward(HardSoftLongScore.ONE_SOFT, (order1, order2) -> 5)
                .asConstraint("Group orders by category");
    }
    */

    /**
     * Optional: Weather-based vehicle assignment preference.
     * Uncomment and add to constraint array for weather-optimized assignments.
     */
    /*
    private Constraint weatherVehiclePreference(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(DeliveryOrder.class)
                .filter(order -> order.getAssignedAgent() != null)
                .reward(HardSoftLongScore.ONE_SOFT,
                        order -> calculateWeatherCompatibilityBonus(order))
                .asConstraint("Weather-vehicle compatibility preference");
    }

    private long calculateWeatherCompatibilityBonus(DeliveryOrder order) {
        String weather = order.getWeather().toLowerCase();
        String vehicle = order.getAssignedAgent().getVehicleType().toLowerCase();
        
        // Cars are better in bad weather
        if ((weather.contains("rain") || weather.contains("storm")) && vehicle.equals("car")) {
            return 10L;
        }
        
        // Motorcycles are better in good weather
        if (weather.contains("sunny") && vehicle.equals("motorcycle")) {
            return 5L;
        }
        
        return 0L;
    }
    */
}