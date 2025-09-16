
package org.acme.vehiclerouting.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@PlanningEntity
public class Vehicle {

    private String id;
    private String style;  // motorcycle, scooter, van
    private Location homeLocation;
    private int capacity;
    private LocalDateTime departureTime;

    @PlanningListVariable
    private List<Visit> visits;

    // Solver-calculated fields - stored directly, not calculated
    private int totalDemand;
    private long totalDrivingTimeSeconds;
    private LocalDateTime arrivalTime;

    // Default constructor (required by Timefold)
    public Vehicle() {
        this.visits = new ArrayList<>();
        this.capacity = 10; // default
    }

    public Vehicle(String id) {
        this.id = id;
        this.visits = new ArrayList<>();
        this.capacity = 10; // default
    }

    public Vehicle(String id, String style) {
        this.id = id;
        this.style = style;
        this.visits = new ArrayList<>();
        this.capacity = 10; // default
    }

    public Vehicle(String id, String style, Location homeLocation, LocalDateTime departureTime) {
        this.id = id;
        this.homeLocation = homeLocation;
        this.visits = new ArrayList<>();
        this.capacity = 10; // default
    }

    // Constructor with basic fields
    public Vehicle(String id, String style, Location homeLocation) {
        this.id = id;
        this.style = style;
        this.homeLocation = homeLocation;
        this.visits = new ArrayList<>();
        this.capacity = 10; // default
    }

    // Full constructor
    public Vehicle(String id, String style, Location homeLocation, 
                   int capacity, LocalDateTime departureTime) {
        this.id = id;
        this.style = style;
        this.homeLocation = homeLocation;
        this.capacity = capacity;
        this.departureTime = departureTime;
        this.visits = new ArrayList<>();
    }

    // SAFE getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public Location getHomeLocation() { return homeLocation; }
    public void setHomeLocation(Location homeLocation) { 
        this.homeLocation = homeLocation; 
    }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { 
        this.departureTime = departureTime; 
    }

    public List<Visit> getVisits() { return visits; }
    public void setVisits(List<Visit> visits) { this.visits = visits; }

    // SAFE: Return stored values, don't calculate during JSON serialization
    public int getTotalDemand() { return totalDemand; }
    public void setTotalDemand(int totalDemand) { this.totalDemand = totalDemand; }

    public long getTotalDrivingTimeSeconds() { return totalDrivingTimeSeconds; }
    public void setTotalDrivingTimeSeconds(long totalDrivingTimeSeconds) { 
        this.totalDrivingTimeSeconds = totalDrivingTimeSeconds; 
    }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    /**
     * Helper method to calculate total demand safely (for internal use)
     */
    @JsonIgnore
    public int calculateTotalDemand() {
        if (visits != null) {
            return visits.stream().mapToInt(Visit::getDemand).sum();
        }
        return 0;
    }

    /**
     * Helper method to calculate total driving time safely (for internal use)
     */
    @JsonIgnore
    public long calculateTotalDrivingTime() {
        if (visits != null) {
            return visits.stream()
                .mapToLong(visit -> visit.getDrivingTimeSecondsFromPreviousStandstill())
                .sum();
        }
        return 0;
    }

    /**
     * Update calculated fields safely
     */
    public void updateCalculatedFields() {
        this.totalDemand = calculateTotalDemand();
        this.totalDrivingTimeSeconds = calculateTotalDrivingTime();
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", style='" + style + '\'' +
                ", homeLocation=" + homeLocation +
                ", capacity=" + capacity +
                ", visits=" + (visits != null ? visits.size() : 0) +
                '}';
    }
}