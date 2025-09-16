// package org.acme.vehiclerouting.domain.unused;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

import java.time.LocalDateTime;


/**
 * Updates the estimated arrival time when an order is assigned to an agent.
 * This is a shadow variable listener that automatically calculates delivery timing.
 */
public class DeliveryTimeUpdatingVariableListener implements VariableListener<DeliveryRoutePlan, DeliveryOrder> {
    
    @Override
    public void beforeEntityAdded(ScoreDirector<DeliveryRoutePlan> scoreDirector, DeliveryOrder deliveryOrder) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<DeliveryRoutePlan> scoreDirector, DeliveryOrder deliveryOrder) {
        updateEstimatedArrivalTime(scoreDirector, deliveryOrder);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<DeliveryRoutePlan> scoreDirector, DeliveryOrder deliveryOrder) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector<DeliveryRoutePlan> scoreDirector, DeliveryOrder deliveryOrder) {
        updateEstimatedArrivalTime(scoreDirector, deliveryOrder);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<DeliveryRoutePlan> scoreDirector, DeliveryOrder deliveryOrder) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<DeliveryRoutePlan> scoreDirector, DeliveryOrder deliveryOrder) {
        // Do nothing
    }

    private void updateEstimatedArrivalTime(ScoreDirector<DeliveryRoutePlan> scoreDirector, DeliveryOrder deliveryOrder) {
        DeliveryAgent agent = deliveryOrder.getAssignedAgent();
        LocalDateTime estimatedArrival = null;

        if (agent != null) {
            // Start from the pickup time as base
            LocalDateTime baseTime = deliveryOrder.getPickupDateTime();

            // Add travel time from store to customer
            long travelTimeSeconds = deliveryOrder.getTotalTravelTimeSeconds();
            estimatedArrival = baseTime.plusSeconds(travelTimeSeconds);

            // Apply traffic and weather modifiers
            estimatedArrival = applyDeliveryModifiers(estimatedArrival, deliveryOrder);
        }

        scoreDirector.beforeVariableChanged(deliveryOrder, "estimatedArrivalTime");
        deliveryOrder.setEstimatedArrivalTime(estimatedArrival);
        scoreDirector.afterVariableChanged(deliveryOrder, "estimatedArrivalTime");
    }

    /**
     * Apply traffic and weather modifiers to the estimated arrival time.
     */
    private LocalDateTime applyDeliveryModifiers(LocalDateTime baseTime, DeliveryOrder order) {
        double modifier = 1.0;

        // Traffic modifier
        switch (order.getTraffic().toLowerCase().trim()) {
            case "high":
                modifier *= 1.5; // 50% longer in high traffic
                break;
            case "medium":
                modifier *= 1.2; // 20% longer in medium traffic
                break;
            case "low":
                modifier *= 1.0; // No change in low traffic
                break;
            default:
                modifier *= 1.1; // Default 10% buffer
        }

        // Weather modifier
        switch (order.getWeather().toLowerCase().trim()) {
            case "rainy":
            case "stormy":
                modifier *= 1.3; // 30% longer in bad weather
                break;
            case "cloudy":
                modifier *= 1.1; // 10% longer in cloudy weather
                break;
            case "sunny":
                modifier *= 1.0; // No change in good weather
                break;
            default:
                modifier *= 1.05; // Default 5% buffer
        }

        // Area modifier
        switch (order.getArea().toLowerCase().trim()) {
            case "urban":
                modifier *= 1.1; // Urban areas have more complexity
                break;
            case "suburban":
                modifier *= 1.0; // Baseline
                break;
            case "rural":
                modifier *= 1.2; // Rural areas take longer
                break;
            default:
                modifier *= 1.0;
        }

        // Apply the total modifier to the base delivery time
        long additionalMinutes = Math.round(order.getEstimatedDeliveryTimeMinutes() * (modifier - 1.0));
        return baseTime.plusMinutes(additionalMinutes);
    }
}