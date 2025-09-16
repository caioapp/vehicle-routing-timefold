package org.acme.vehiclerouting.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

@PlanningSolution
public class VehicleRoutePlan {

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Visit> visits;

    @PlanningEntityCollectionProperty  
    private List<Vehicle> vehicles;

    @PlanningScore
    private HardSoftLongScore score;

    private String name;
    private Location southWestCorner;
    private Location northEastCorner;
    private String startDateTime;
    private String endDateTime;

    // CHANGED: Use SolverStatus enum instead of String
    private SolverStatus solverStatus;
    private String scoreExplanation;
    private long totalDrivingTimeSeconds;

    // Default constructor for Timefold
    public VehicleRoutePlan() {
        this.visits = new ArrayList<>();
        this.vehicles = new ArrayList<>();
        // Set default corners for India
        this.southWestCorner = new Location(10.033064, 72.784115);
        this.northEastCorner = new Location(30.929584, 88.395507);
        this.solverStatus = SolverStatus.NOT_SOLVING;
    }

    public VehicleRoutePlan(List<Visit> visits, List<Vehicle> vehicles) {
        this.visits = visits != null ? visits : new ArrayList<>();
        this.vehicles = vehicles != null ? vehicles : new ArrayList<>();
        // Set default corners
        this.southWestCorner = new Location(10.033064, 72.784115);
        this.northEastCorner = new Location(30.929584, 88.395507);
        this.solverStatus = SolverStatus.NOT_SOLVING;
    }

    // Constructor that template expects
    public VehicleRoutePlan(String name, Location southWest, Location northEast, 
                           LocalDateTime windowStart, LocalDateTime windowEnd, 
                           List<Vehicle> vehicles, List<Visit> visits) {
        this.name = name;
        this.southWestCorner = southWest;
        this.northEastCorner = northEast;
        this.startDateTime = windowStart != null ? windowStart.toString() : null;
        this.endDateTime = windowEnd != null ? windowEnd.toString() : null;
        this.vehicles = vehicles != null ? vehicles : new ArrayList<>();
        this.visits = visits != null ? visits : new ArrayList<>();
        this.solverStatus = SolverStatus.NOT_SOLVING;
    }

    // Getters and setters
    public List<Visit> getVisits() { return visits; }
    public void setVisits(List<Visit> visits) { this.visits = visits; }

    public List<Vehicle> getVehicles() { return vehicles; }
    public void setVehicles(List<Vehicle> vehicles) { this.vehicles = vehicles; }

    public HardSoftLongScore getScore() { return score; }
    public void setScore(HardSoftLongScore score) { this.score = score; }

    // FIXED: Use SolverStatus enum instead of String
    public SolverStatus getSolverStatus() { return solverStatus; }
    public void setSolverStatus(SolverStatus solverStatus) { this.solverStatus = solverStatus; }

    // ADDED: String getter/setter for backward compatibility
    public String getSolverStatusAsString() { 
        return solverStatus != null ? solverStatus.toString() : "NOT_SOLVING"; 
    }
    public void setSolverStatusFromString(String status) {
        try {
            this.solverStatus = SolverStatus.valueOf(status);
        } catch (Exception e) {
            this.solverStatus = SolverStatus.NOT_SOLVING;
        }
    }

    public String getScoreExplanation() { return scoreExplanation; }
    public void setScoreExplanation(String scoreExplanation) { this.scoreExplanation = scoreExplanation; }

    public long getTotalDrivingTimeSeconds() { return totalDrivingTimeSeconds; }
    public void setTotalDrivingTimeSeconds(long totalDrivingTimeSeconds) { 
        this.totalDrivingTimeSeconds = totalDrivingTimeSeconds; 
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Location getSouthWestCorner() { return southWestCorner; }
    public void setSouthWestCorner(Location southWestCorner) { 
        this.southWestCorner = southWestCorner; 
    }

    public Location getNorthEastCorner() { return northEastCorner; } 
    public void setNorthEastCorner(Location northEastCorner) { 
        this.northEastCorner = northEastCorner; 
    }

    // Convenience methods for double array access (for JSON compatibility)
    public double[] getSouthWestCornerAsArray() {
        return southWestCorner != null ? 
            new double[]{southWestCorner.getLatitude(), southWestCorner.getLongitude()} : 
            new double[]{10.033064, 72.784115};
    }

    public double[] getNorthEastCornerAsArray() {
        return northEastCorner != null ? 
            new double[]{northEastCorner.getLatitude(), northEastCorner.getLongitude()} : 
            new double[]{30.929584, 88.395507};
    }

    public String getStartDateTime() { return startDateTime; }
    public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }

    public String getEndDateTime() { return endDateTime; }
    public void setEndDateTime(String endDateTime) { this.endDateTime = endDateTime; }

    @Override
    public String toString() {
        return "VehicleRoutePlan{" +
                "name='" + name + '\'' +
                ", vehicles=" + (vehicles != null ? vehicles.size() : 0) +
                ", visits=" + (visits != null ? visits.size() : 0) +
                ", score=" + score +
                ", solverStatus=" + solverStatus +
                '}';
    }
}