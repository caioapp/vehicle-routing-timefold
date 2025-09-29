
package org.acme.vehiclerouting.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@JsonIdentityInfo(scope = Vehicle.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@PlanningEntity
public class Vehicle implements LocationAware {

    private String id;
    private String style;
    private Location homeLocation;
    private int capacity;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureTime;
    
    @JsonIdentityReference(alwaysAsId = true)
    @PlanningListVariable
    private List<Visit> visits;
    
    // Constructor ensuring departureTime is set
    public Vehicle(String id, String style, Location homeLocation, int capacity, LocalDateTime departureTime) {
        this.id = id;
        this.style = style;
        this.homeLocation = homeLocation;
        this.capacity = capacity;
        this.departureTime = departureTime; 
        this.visits = new ArrayList<>();
    }


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
        switch (style) {
            case "van":
                this.capacity = 20;
                break;
            case "motorcycle":
                this.capacity = 8;
                break;
            case "scooter":
                this.capacity = 4;
                break;          
            default:
                this.capacity = 10;
                break;
        }
    }

    public Vehicle(String id, String style, Location homeLocation, LocalDateTime departureTime) {
        this.id = id;
        this.homeLocation = homeLocation;
        this.style = style;
        this.departureTime = departureTime;
        this.visits = new ArrayList<>();
        switch (style) {
            case "van":
                this.capacity = 20;
                break;
            case "motorcycle":
                this.capacity = 8;
                break;
            case "scooter":
                this.capacity = 4;
                break;          
            default:
                this.capacity = 10;
                break;
        }
    }

    // Constructor with basic fields
    public Vehicle(String id, String style, Location homeLocation) {
        this.id = id;
        this.style = style;
        this.homeLocation = homeLocation;
        this.visits = new ArrayList<>();
        switch (style) {
            case "van":
                this.capacity = 20;
                break;
            case "motorcycle":
                this.capacity = 8;
                break;
            case "scooter":
                this.capacity = 4;
                break;          
            default:
                this.capacity = 10;
                break;
        }
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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getTotalDemand() {
        int totalDemand = 0;
        for (Visit visit : visits) {
            totalDemand += visit.getDemand();
        }
        return totalDemand;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public long getTotalDrivingTimeSeconds() {
        if (visits.isEmpty()) {
            return 0;
        }

        long totalDrivingTime = 0;
        Location previousLocation = homeLocation;

        for (Visit visit : visits) {
            totalDrivingTime += previousLocation.getDrivingTimeTo(visit.getLocation());
            previousLocation = visit.getLocation();
        }
        totalDrivingTime += previousLocation.getDrivingTimeTo(homeLocation);

        return totalDrivingTime;
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

    @JsonIgnore
    @Override
    public Location getLocation() {
        return homeLocation;
    }
}