
package org.acme.vehiclerouting.service;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.Location;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

@ApplicationScoped
public class VehicleRoutingDataService {

    /**
     * Creates a safe test problem that won't cause JSON serialization errors
     */
    public VehicleRoutePlan createSafeProblem() {
        List<Vehicle> vehicles = createSafeVehicles();
        List<Visit> visits = createSafeVisits();

        VehicleRoutePlan plan = new VehicleRoutePlan(visits, vehicles);
        plan.setSolverStatus("NOT_SOLVING");
        plan.setScoreExplanation("Initial solution");

        return plan;
    }

    private List<Vehicle> createSafeVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();

        LocalDateTime departureTime = LocalDateTime.now().plusHours(1);

        Vehicle vehicle1 = new Vehicle("vehicle-1", "motorcycle", 
            new Location(19.076, 72.8777));
        vehicle1.setCapacity(8);
        vehicle1.setDepartureTime(departureTime);
        vehicle1.setTotalDemand(0); // Initialize to safe values
        vehicle1.setTotalDrivingTimeSeconds(0);
        vehicles.add(vehicle1);

        Vehicle vehicle2 = new Vehicle("vehicle-2", "van", 
            new Location(18.5204, 73.8567));
        vehicle2.setCapacity(15);
        vehicle2.setDepartureTime(departureTime);
        vehicle2.setTotalDemand(0);
        vehicle2.setTotalDrivingTimeSeconds(0);
        vehicles.add(vehicle2);

        return vehicles;
    }

    private List<Visit> createSafeVisits() {
        List<Visit> visits = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        Visit visit1 = new Visit("1", "Mumbai Central Delivery", 
            new Location(19.0896, 72.8656));
        visit1.setDemand(1);
        visit1.setMinStartTime(now.plusHours(2));
        visit1.setMaxEndTime(now.plusHours(6));
        visit1.setServiceDuration(3600); // 1 hour
        // Don't set calculated fields - leave them null for now
        visits.add(visit1);

        Visit visit2 = new Visit("2", "Pune Station Delivery", 
            new Location(18.5289, 73.8732));
        visit2.setDemand(1);
        visit2.setMinStartTime(now.plusHours(3));
        visit2.setMaxEndTime(now.plusHours(7));
        visit2.setServiceDuration(1800); // 30 minutes
        // Don't set calculated fields - leave them null for now
        visits.add(visit2);

        Visit visit3 = new Visit("3", "Nashik Delivery", 
            new Location(19.9975, 73.7898));
        visit3.setDemand(1);
        visit3.setMinStartTime(now.plusHours(4));
        visit3.setMaxEndTime(now.plusHours(8));
        visit3.setServiceDuration(2700); // 45 minutes
        visits.add(visit3);

        Visit visit4 = new Visit("4", "Aurangabad Delivery", 
            new Location(19.8762, 75.3433));
        visit4.setDemand(1);
        visit4.setMinStartTime(now.plusHours(2));
        visit4.setMaxEndTime(now.plusHours(9));
        visit4.setServiceDuration(3000); // 50 minutes
        visits.add(visit4);

        return visits;
    }
}