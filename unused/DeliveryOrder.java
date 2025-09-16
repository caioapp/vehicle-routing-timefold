// package org.acme.vehiclerouting.domain.unused;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a delivery order that needs to be fulfilled.
 * This corresponds to a row in the CSV file.
 * 
 * Planning Entity - Timefold will assign this to a DeliveryAgent.
 */
@PlanningEntity
public class DeliveryOrder {

    // Order details from CSV
    private String orderId;
    private Location storeLocation;
    private Location dropLocation;
    private LocalDate orderDate;
    private LocalTime orderTime;
    private LocalTime pickupTime;
    private String weather;
    private String traffic;
    private String vehicleType; // motorcycle, car, etc.
    private String area; // Urban, Suburban, etc.
    private int estimatedDeliveryTimeMinutes;
    private String category; // Clothing, Food, etc.

    // Planning variables - will be assigned by Timefold
    @PlanningVariable(valueRangeProviderRefs = "agentRange", nullable = true)
    private DeliveryAgent assignedAgent;

    // Shadow variables - calculated automatically
    @ShadowVariable(variableListenerClass = DeliveryTimeUpdatingVariableListener.class, 
                   sourceVariableName = "assignedAgent")
    private LocalDateTime estimatedArrivalTime;

    // No-arg constructor for Timefold
    public DeliveryOrder() {}

    public DeliveryOrder(String orderId, Location storeLocation, Location dropLocation,
                        LocalDate orderDate, LocalTime orderTime, LocalTime pickupTime,
                        String weather, String traffic, String vehicleType, String area,
                        int estimatedDeliveryTimeMinutes, String category) {
        this.orderId = orderId;
        this.storeLocation = storeLocation;
        this.dropLocation = dropLocation;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
        this.pickupTime = pickupTime;
        this.weather = weather;
        this.traffic = traffic;
        this.vehicleType = vehicleType;
        this.area = area;
        this.estimatedDeliveryTimeMinutes = estimatedDeliveryTimeMinutes;
        this.category = category;
    }

    // Getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Location getStoreLocation() { return storeLocation; }
    public void setStoreLocation(Location storeLocation) { this.storeLocation = storeLocation; }

    public Location getDropLocation() { return dropLocation; }
    public void setDropLocation(Location dropLocation) { this.dropLocation = dropLocation; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalTime orderTime) { this.orderTime = orderTime; }

    public LocalTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalTime pickupTime) { this.pickupTime = pickupTime; }

    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }

    public String getTraffic() { return traffic; }
    public void setTraffic(String traffic) { this.traffic = traffic; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public int getEstimatedDeliveryTimeMinutes() { return estimatedDeliveryTimeMinutes; }
    public void setEstimatedDeliveryTimeMinutes(int estimatedDeliveryTimeMinutes) { 
        this.estimatedDeliveryTimeMinutes = estimatedDeliveryTimeMinutes; 
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public DeliveryAgent getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(DeliveryAgent assignedAgent) { this.assignedAgent = assignedAgent; }

    public LocalDateTime getEstimatedArrivalTime() { return estimatedArrivalTime; }
    public void setEstimatedArrivalTime(LocalDateTime estimatedArrivalTime) { 
        this.estimatedArrivalTime = estimatedArrivalTime; 
    }

    /**
     * Calculate the total travel distance for this order (store -> customer).
     */
    @JsonIgnore
    public long getTotalTravelTimeSeconds() {
        return storeLocation.getDrivingTimeTo(dropLocation);
    }

    /**
     * Get the combined order timestamp.
     */
    @JsonIgnore
    public LocalDateTime getOrderDateTime() {
        return LocalDateTime.of(orderDate, orderTime);
    }

    /**
     * Get the combined pickup timestamp.
     */
    @JsonIgnore
    public LocalDateTime getPickupDateTime() {
        return LocalDateTime.of(orderDate, pickupTime);
    }

    /**
     * Check if this order is compatible with the agent's vehicle type.
     */
    @JsonIgnore
    public boolean isCompatibleWithVehicle(String agentVehicleType) {
        // Basic compatibility - could be expanded with business rules
        return agentVehicleType.equals(this.vehicleType) || 
               agentVehicleType.equals("any") ||
               this.vehicleType.equals("any");
    }

    /**
     * Get urgency score based on pickup time and current time.
     */
    @JsonIgnore
    public int getUrgencyScore() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pickup = getPickupDateTime();

        if (pickup.isBefore(now)) {
            return 100; // Very urgent - already late
        }

        long minutesUntilPickup = java.time.Duration.between(now, pickup).toMinutes();

        if (minutesUntilPickup <= 30) {
            return 80; // High urgency
        } else if (minutesUntilPickup <= 60) {
            return 60; // Medium urgency
        } else if (minutesUntilPickup <= 120) {
            return 40; // Low urgency
        } else {
            return 20; // Very low urgency
        }
    }

    @Override
    public String toString() {
        return String.format("Order[%s: %s -> %s, pickup=%s, agent=%s]", 
                           orderId, storeLocation, dropLocation, pickupTime,
                           assignedAgent != null ? assignedAgent.getAgentId() : "unassigned");
    }
}