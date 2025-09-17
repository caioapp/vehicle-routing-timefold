package org.acme.vehiclerouting.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import java.time.Duration;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Visit {
    private String id;
    private String name;
    private Location location;
    private int demand;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime minStartTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") 
    private LocalDateTime maxEndTime;
    
    private long serviceDuration;
    
    // Main planning variables
    @PlanningVariable(valueRangeProviderRefs = "vehicleRange")
    private Vehicle vehicle;
    
    @PlanningVariable(valueRangeProviderRefs = "visitRange", nullable = true)
    private Visit previousVisit;
    
    // Shadow variables (auto-calculated)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivalTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startServiceTime;
    
    private long drivingTimeSecondsFromPreviousStandstill;
    
    // CRITICAL: Must have ALL getters and setters
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    
    public Visit getPreviousVisit() { return previousVisit; }

    public void setPreviousVisit(Visit previousVisit) {
        this.previousVisit = previousVisit;
    }


    // Default constructor
    public Visit() {
        this.demand = 1; // default
    }

    // Constructor with basic fields
    public Visit(String id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.demand = 1;
    }

    public Visit(String id, String name, Location location, int demand,
                 LocalDateTime minStartTime, LocalDateTime maxEndTime) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.demand = demand;
        this.minStartTime = minStartTime;
        this.maxEndTime = maxEndTime;
        this.serviceDuration = Duration.ofMinutes(30).getSeconds(); // default 30 minutes
    }

    public Visit(String id, String name, Location location, int demand,
                LocalDateTime minStartTime, LocalDateTime maxEndTime, Duration serviceDuration) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.demand = demand;
        this.minStartTime = minStartTime;
        this.maxEndTime = maxEndTime;
        // Convert Duration to seconds
        this.serviceDuration = serviceDuration != null ? serviceDuration.getSeconds() : Duration.ofMinutes(30).getSeconds();
    }

    // SAFE getters that don't do calculations
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public int getDemand() { return demand; }
    public void setDemand(int demand) { this.demand = demand; }

    public LocalDateTime getMinStartTime() { return minStartTime; }
    public void setMinStartTime(LocalDateTime minStartTime) { 
        this.minStartTime = minStartTime; 
    }

    public LocalDateTime getMaxEndTime() { return maxEndTime; }
    public void setMaxEndTime(LocalDateTime maxEndTime) { 
        this.maxEndTime = maxEndTime; 
    }

    public long getServiceDuration() { return serviceDuration; }
    public void setServiceDuration(long serviceDuration) { 
        this.serviceDuration = serviceDuration; 
    }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { 
        this.arrivalTime = arrivalTime; 
    }

    /**
     * FIXED: Safe departure time getter - no calculations, just return stored value
     */
    public LocalDateTime getDepartureTime() { 
        return departureTime;  // Don't calculate, just return stored value
    }
    public void setDepartureTime(LocalDateTime departureTime) { 
        this.departureTime = departureTime; 
    }

    public LocalDateTime getStartServiceTime() { return startServiceTime; }
    public void setStartServiceTime(LocalDateTime startServiceTime) { 
        this.startServiceTime = startServiceTime; 
    }

    public long getDrivingTimeSecondsFromPreviousStandstill() { 
        return drivingTimeSecondsFromPreviousStandstill; 
    }
    public void setDrivingTimeSecondsFromPreviousStandstill(long drivingTime) { 
        this.drivingTimeSecondsFromPreviousStandstill = drivingTime; 
    }

    /**
     * Helper method to calculate departure time safely (for internal use)
     */
    @JsonIgnore
    public LocalDateTime calculateDepartureTime() {
        if (arrivalTime != null && serviceDuration > 0) {
            return arrivalTime.plusSeconds(serviceDuration);
        }
        return departureTime; // fallback to stored value
    }

    /**
     * Helper method to set departure time based on arrival + service duration
     */
    public void updateDepartureTime() {
        if (arrivalTime != null && serviceDuration > 0) {
            this.departureTime = arrivalTime.plusSeconds(serviceDuration);
        }
    }

    @Override
    public String toString() {
        return "Visit{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", demand=" + demand +
                '}';
    }
}