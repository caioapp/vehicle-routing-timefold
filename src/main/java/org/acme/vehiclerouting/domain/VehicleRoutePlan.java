
package org.acme.vehiclerouting.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

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
    private double[] southWestCorner;
    private double[] northEastCorner;
    private String startDateTime;
    private String endDateTime;

    // Additional fields for JSON compatibility
    private String solverStatus;
    private String scoreExplanation;
    private long totalDrivingTimeSeconds;

    // Default constructor for Timefold
    public VehicleRoutePlan() {
        this.visits = new ArrayList<>();
        this.vehicles = new ArrayList<>();
    }

    public VehicleRoutePlan(List<Visit> visits, List<Vehicle> vehicles) {
        this.visits = visits != null ? visits : new ArrayList<>();
        this.vehicles = vehicles != null ? vehicles : new ArrayList<>();
    }

    // Add the missing constructor
    public VehicleRoutePlan(String name, Location southWest, Location northEast, LocalDateTime windowStart, LocalDateTime windowEnd, List<Vehicle> vehicles, List<Visit> visits) {
        // Initialize fields as appropriate
        this.vehicles = vehicles;
        this.visits = visits;
    }

    // Getters and setters
    public List<Visit> getVisits() { return visits; }
    public void setVisits(List<Visit> visits) { this.visits = visits; }

    public List<Vehicle> getVehicles() { return vehicles; }
    public void setVehicles(List<Vehicle> vehicles) { this.vehicles = vehicles; }

    public HardSoftLongScore getScore() { return score; }
    public void setScore(HardSoftLongScore score) { this.score = score; }

    public String getSolverStatus() { return solverStatus; }
    public void setSolverStatus(String solverStatus) { this.solverStatus = solverStatus; }

    public String getScoreExplanation() { return scoreExplanation; }
    public void setScoreExplanation(String scoreExplanation) { this.scoreExplanation = scoreExplanation; }

    public long getTotalDrivingTimeSeconds() { return totalDrivingTimeSeconds; }
    public void setTotalDrivingTimeSeconds(long totalDrivingTimeSeconds) { 
        this.totalDrivingTimeSeconds = totalDrivingTimeSeconds; 
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double[] getSouthWestCorner() { return southWestCorner; }
    public void setSouthWestCorner(double[] southWestCorner) { this.southWestCorner = southWestCorner; }
    public double[] getNorthEastCorner() { return northEastCorner; } 
    public void setNorthEastCorner(double[] northEastCorner) { this.northEastCorner = northEastCorner; }
    public String getStartDateTime() { return startDateTime; }
    public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime;}

    @Override
    public String toString() {
        return "VehicleRoutePlan{" +
                "vehicles=" + (vehicles != null ? vehicles.size() : 0) +
                ", visits=" + (visits != null ? visits.size() : 0) +
                ", score=" + score +
                '}';
    }
}