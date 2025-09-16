// package org.acme.vehiclerouting.domain.unused;

// import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalTime;
import java.util.List;

/**
 * Represents a delivery agent (driver) with their vehicle and capabilities.
 * This is derived from the agent information in the CSV (Agent_Age, Agent_Rating, Vehicle).
 * 
 * Planning Entity - Timefold will assign DeliveryOrders to this agent.
 */
// @PlanningEntity
public class DeliveryAgent {

    private String agentId;
    private int age;
    private double rating;
    private String vehicleType; // motorcycle, car, truck, etc.
    private Location homeLocation; // Starting location for the agent
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
    private int maxOrdersPerShift;
    private String workingArea; // Urban, Suburban, etc.

    // Shadow variable - orders assigned to this agent
    @InverseRelationShadowVariable(sourceVariableName = "assignedAgent")
    private List<DeliveryOrder> assignedOrders;

    // No-arg constructor for Timefold
    public DeliveryAgent() {}

    public DeliveryAgent(String agentId, int age, double rating, String vehicleType,
                        Location homeLocation, LocalTime shiftStart, LocalTime shiftEnd,
                        int maxOrdersPerShift, String workingArea) {
        this.agentId = agentId;
        this.age = age;
        this.rating = rating;
        this.vehicleType = vehicleType;
        this.homeLocation = homeLocation;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.maxOrdersPerShift = maxOrdersPerShift;
        this.workingArea = workingArea;
    }

    // Getters and setters
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Location getHomeLocation() { return homeLocation; }
    public void setHomeLocation(Location homeLocation) { this.homeLocation = homeLocation; }

    public LocalTime getShiftStart() { return shiftStart; }
    public void setShiftStart(LocalTime shiftStart) { this.shiftStart = shiftStart; }

    public LocalTime getShiftEnd() { return shiftEnd; }
    public void setShiftEnd(LocalTime shiftEnd) { this.shiftEnd = shiftEnd; }

    public int getMaxOrdersPerShift() { return maxOrdersPerShift; }
    public void setMaxOrdersPerShift(int maxOrdersPerShift) { this.maxOrdersPerShift = maxOrdersPerShift; }

    public String getWorkingArea() { return workingArea; }
    public void setWorkingArea(String workingArea) { this.workingArea = workingArea; }

    public List<DeliveryOrder> getAssignedOrders() { return assignedOrders; }
    public void setAssignedOrders(List<DeliveryOrder> assignedOrders) { this.assignedOrders = assignedOrders; }

    /**
     * Get the total number of orders assigned to this agent.
     */
    @JsonIgnore
    public int getOrderCount() {
        return assignedOrders != null ? assignedOrders.size() : 0;
    }

    /**
     * Check if the agent can take more orders.
     */
    @JsonIgnore
    public boolean canTakeMoreOrders() {
        return getOrderCount() < maxOrdersPerShift;
    }

    /**
     * Calculate the total travel time for all assigned orders.
     */
    @JsonIgnore
    public long getTotalTravelTimeSeconds() {
        if (assignedOrders == null || assignedOrders.isEmpty()) {
            return 0L;
        }

        long totalTime = 0L;
        Location currentLocation = homeLocation;

        // Travel to first order's store
        if (!assignedOrders.isEmpty()) {
            DeliveryOrder firstOrder = assignedOrders.get(0);
            totalTime += currentLocation.getDrivingTimeTo(firstOrder.getStoreLocation());
        }

        // Travel between orders
        for (DeliveryOrder order : assignedOrders) {
            // Store to customer
            totalTime += order.getTotalTravelTimeSeconds();
            currentLocation = order.getDropLocation();
        }

        // Return home after last delivery
        if (!assignedOrders.isEmpty()) {
            totalTime += currentLocation.getDrivingTimeTo(homeLocation);
        }

        return totalTime;
    }

    /**
     * Calculate the total service time for all orders (pickup + delivery time).
     */
    @JsonIgnore
    public long getTotalServiceTimeSeconds() {
        if (assignedOrders == null || assignedOrders.isEmpty()) {
            return 0L;
        }

        return assignedOrders.stream()
                .mapToLong(order -> order.getEstimatedDeliveryTimeMinutes() * 60L)
                .sum();
    }

    /**
     * Calculate the agent's efficiency score based on rating and vehicle type.
     */
    @JsonIgnore
    public double getEfficiencyScore() {
        double baseScore = rating * 20; // Rating is out of 5, so max 100

        // Adjust based on vehicle type efficiency
        switch (vehicleType.toLowerCase()) {
            case "motorcycle":
                baseScore *= 1.2; // Motorcycles are more efficient in urban areas
                break;
            case "car":
                baseScore *= 1.0;
                break;
            case "truck":
                baseScore *= 0.8; // Trucks are less efficient
                break;
            default:
                baseScore *= 1.0;
        }

        return Math.min(baseScore, 100.0); // Cap at 100
    }

    /**
     * Check if the agent is available during the specified time window.
     */
    @JsonIgnore
    public boolean isAvailableDuring(LocalTime startTime, LocalTime endTime) {
        return !startTime.isBefore(shiftStart) && !endTime.isAfter(shiftEnd);
    }

    /**
     * Check if the agent can handle deliveries in the specified area.
     */
    @JsonIgnore
    public boolean canWorkInArea(String area) {
        return workingArea.equals("Any") || workingArea.equals(area);
    }

    @Override
    public String toString() {
        return String.format("Agent[%s: %s, rating=%.1f, vehicle=%s, orders=%d/%d]", 
                           agentId, workingArea, rating, vehicleType,
                           getOrderCount(), maxOrdersPerShift);
    }
}