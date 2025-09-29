package org.acme.vehiclerouting.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Visit.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@PlanningEntity
public class Visit {
    private String id;
    private String name;
    private Location location;
    private int demand;
    private Duration serviceDuration;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime minStartTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") 
    private LocalDateTime maxEndTime;
    

    @JsonIdentityReference(alwaysAsId = true)
    @InverseRelationShadowVariable(sourceVariableName = "visits")
    private Vehicle vehicle;
    
    @JsonIdentityReference(alwaysAsId = true)
    @PreviousElementShadowVariable(sourceVariableName = "visits")
    private Visit previousVisit;
    
    // Shadow variables (auto-calculated)
    @ShadowVariable(supplierName = "arrivalTimeSupplier")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivalTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startServiceTime;
    
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
        this.serviceDuration = Duration.ofMinutes(30); // default 30 minutes
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
        this.serviceDuration = serviceDuration != null ? serviceDuration : Duration.ofMinutes(30);
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

    public Duration getServiceDuration() { return serviceDuration; }
    public void setServiceDuration(Duration serviceDuration) { 
        this.serviceDuration = serviceDuration; 
    }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { 
        this.arrivalTime = arrivalTime; 
    }

    /**
     * FIXED: Safe departure time getter - no calculations, just return stored value
     */
    public void setDepartureTime(LocalDateTime departureTime) { 
        this.departureTime = departureTime; 
    }

    public void setStartServiceTime(LocalDateTime startServiceTime) { 
        this.startServiceTime = startServiceTime; 
    }

    /**
     * Helper method to calculate departure time safely (for internal use)
     */
    @JsonIgnore
    public LocalDateTime calculateDepartureTime() {
        if (arrivalTime != null && serviceDuration.getSeconds() > 0) {
            return arrivalTime.plus(serviceDuration);
        }
        return departureTime; // fallback to stored value
    }

    /**
     * Helper method to set departure time based on arrival + service duration
     */
    public void updateDepartureTime() {
        if (arrivalTime != null && serviceDuration.getSeconds() > 0) {
            this.departureTime = arrivalTime.plus(serviceDuration);
        }
    }

   @SuppressWarnings("unused")
    @ShadowSources({"vehicle", "previousVisit.arrivalTime"})
    private LocalDateTime arrivalTimeSupplier() {
        if (previousVisit == null && vehicle == null) {
            return null;
        }
        LocalDateTime departureTime = previousVisit == null ? vehicle.getDepartureTime() : previousVisit.getDepartureTime();
        return departureTime != null ? departureTime.plusSeconds(getDrivingTimeSecondsFromPreviousStandstill()) : null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public LocalDateTime getDepartureTime() {
        if (arrivalTime == null) {
            return null;
        }
        return getStartServiceTime().plus(serviceDuration);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public LocalDateTime getStartServiceTime() {
        if (arrivalTime == null) {
            return null;
        }
        return arrivalTime.isBefore(minStartTime) ? minStartTime : arrivalTime;
    }

    @JsonIgnore
    public boolean isServiceFinishedAfterMaxEndTime() {
        return arrivalTime != null
                && arrivalTime.plus(serviceDuration).isAfter(maxEndTime);
    }

    @JsonIgnore
    public long getServiceFinishedDelayInMinutes() {
        if (arrivalTime == null) {
            return 0;
        }
        return roundDurationToNextOrEqualMinutes(Duration.between(maxEndTime, arrivalTime.plus(serviceDuration)));
    }

    private static long roundDurationToNextOrEqualMinutes(Duration duration) {
        var remainder = duration.minus(duration.truncatedTo(ChronoUnit.MINUTES));
        var minutes = duration.toMinutes();
        if (remainder.equals(Duration.ZERO)) {
            return minutes;
        }
        return minutes + 1;
    }

    @JsonIgnore
    public long getDrivingTimeSecondsFromPreviousStandstill() {
        if (vehicle == null) {
            throw new IllegalStateException(
                    "This method must not be called when the shadow variables are not initialized yet.");
        }
        if (previousVisit == null) {
            return vehicle.getHomeLocation().getDrivingTimeTo(location);
        }
        return previousVisit.getLocation().getDrivingTimeTo(location);
    }

    // Required by the web UI even before the solution has been initialized.
    @JsonProperty(value = "drivingTimeSecondsFromPreviousStandstill", access = JsonProperty.Access.READ_ONLY)
    public Long getDrivingTimeSecondsFromPreviousStandstillOrNull() {
        if (vehicle == null) {
            return null;
        }
        return getDrivingTimeSecondsFromPreviousStandstill();
    }

    @Override
    public String toString() {
        return id;
    }

}
