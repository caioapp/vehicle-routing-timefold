
package org.acme.vehiclerouting.service;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.Location;
import ai.timefold.solver.core.api.solver.SolverStatus;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class VehicleRoutingDataService {

    /**
     * Creates the Amazon delivery problem with SolverStatus enum
     */
    public VehicleRoutePlan createAmazonDeliveryProblem() {
        List<Vehicle> vehicles = createAmazonVehicleFleet();
        List<Visit> visits = createAmazonDeliveryVisits();

        VehicleRoutePlan plan = new VehicleRoutePlan(visits, vehicles);

        // Set all the fields with proper types
        plan.setName("amazon-delivery-india-problem");
        plan.setSouthWestCorner(new Location(10.033064, 72.784115));
        plan.setNorthEastCorner(new Location(30.929584, 88.395507));
        plan.setStartDateTime("2025-09-17T06:00:00");
        plan.setEndDateTime("2025-09-17T22:00:00");
        plan.setSolverStatus(SolverStatus.NOT_SOLVING); // Use enum instead of string
        plan.setScoreExplanation("Initial Amazon delivery problem");
        plan.setTotalDrivingTimeSeconds(0);

        System.out.println("Created Amazon delivery problem: " + plan.toString());

        return plan;
    }

    /**
     * Create the complete 10-vehicle Amazon fleet
     */
    private List<Vehicle> createAmazonVehicleFleet() {
        List<Vehicle> vehicles = new ArrayList<>();

        LocalDateTime departureTime = LocalDateTime.of(2025, 9, 17, 8, 0);

        // 4 Motorcycles (capacity 8 each)
        vehicles.add(createVehicle("vehicle-1", "motorcycle", 
            new Location(23.374878, 85.335739), 8, departureTime));
        vehicles.add(createVehicle("vehicle-4", "motorcycle", 
            new Location(17.451976, 78.385883), 8, departureTime));
        vehicles.add(createVehicle("vehicle-7", "motorcycle", 
            new Location(12.934365, 77.616155), 8, departureTime));
        vehicles.add(createVehicle("vehicle-10", "motorcycle", 
            new Location(19.176269, 72.836721), 8, departureTime));

        // 3 Scooters (capacity 12 each)
        vehicles.add(createVehicle("vehicle-2", "scooter", 
            new Location(21.186438, 72.794115), 12, departureTime));
        vehicles.add(createVehicle("vehicle-5", "scooter", 
            new Location(11.003669, 76.976494), 12, departureTime));
        vehicles.add(createVehicle("vehicle-8", "scooter", 
            new Location(17.431477, 78.40035), 12, departureTime));

        // 3 Vans (capacity 18 each)
        vehicles.add(createVehicle("vehicle-3", "van", 
            new Location(18.55144, 73.804855), 18, departureTime));
        vehicles.add(createVehicle("vehicle-6", "van", 
            new Location(12.316967, 76.603067), 18, departureTime));
        vehicles.add(createVehicle("vehicle-9", "van", 
            new Location(18.56245, 73.916619), 18, departureTime));

        System.out.println("Created Amazon fleet with " + vehicles.size() + " vehicles");
        return vehicles;
    }

    private Vehicle createVehicle(String id, String style, Location location, 
                                 int capacity, LocalDateTime departureTime) {
        Vehicle vehicle = new Vehicle(id, style, location, capacity, departureTime);
        vehicle.setTotalDemand(0);
        vehicle.setTotalDrivingTimeSeconds(0);
        return vehicle;
    }

    /**
     * Create Amazon delivery visits
     */
    private List<Visit> createAmazonDeliveryVisits() {
        List<Visit> visits = new ArrayList<>();

        // Sample Amazon deliveries across India
        visits.add(createVisit("1", "ialx566343618 (Clothing)", 
            new Location(22.765049, 75.912471),
            "2025-09-17T11:45:00", "2025-09-17T13:45:00", 7200));

        visits.add(createVisit("2", "akqg208421122 (Electronics)", 
            new Location(13.043041, 77.813237),
            "2025-09-17T19:50:00", "2025-09-17T21:50:00", 9900));

        visits.add(createVisit("3", "njpu434582536 (Sports)", 
            new Location(12.924264, 77.6884),
            "2025-09-17T08:45:00", "2025-09-17T10:45:00", 7800));

        visits.add(createVisit("4", "rjto796129700 (Cosmetics)", 
            new Location(11.053669, 77.026494),
            "2025-09-17T18:10:00", "2025-09-17T20:10:00", 6300));

        visits.add(createVisit("5", "zguw716275638 (Toys)", 
            new Location(13.012793, 80.289982),
            "2025-09-17T13:45:00", "2025-09-17T15:45:00", 9000));

        visits.add(createVisit("6", "Amazon Prime Books", 
            new Location(28.6139, 77.2090), // Delhi
            "2025-09-17T10:00:00", "2025-09-17T12:00:00", 5400));

        visits.add(createVisit("7", "Home Appliances", 
            new Location(19.0760, 72.8777), // Mumbai
            "2025-09-17T14:30:00", "2025-09-17T16:30:00", 8100));

        visits.add(createVisit("8", "Fashion Delivery", 
            new Location(22.5726, 88.3639), // Kolkata
            "2025-09-17T09:15:00", "2025-09-17T11:15:00", 6600));

        visits.add(createVisit("9", "Tech Gadgets", 
            new Location(26.9124, 75.7873), // Jaipur
            "2025-09-17T15:45:00", "2025-09-17T17:45:00", 7200));

        visits.add(createVisit("10", "Baby Products", 
            new Location(23.0225, 72.5714), // Ahmedabad
            "2025-09-17T12:20:00", "2025-09-17T14:20:00", 5700));

        System.out.println("Created " + visits.size() + " Amazon delivery visits");
        return visits;
    }

    private Visit createVisit(String id, String name, Location location,
                            String minStartTime, String maxEndTime, long serviceDuration) {
        Visit visit = new Visit(id, name, location);
        visit.setDemand(1);
        visit.setMinStartTime(parseDateTime(minStartTime));
        visit.setMaxEndTime(parseDateTime(maxEndTime));
        visit.setServiceDuration(serviceDuration);

        // Set safe initial values
        visit.setArrivalTime(null);
        visit.setDepartureTime(null);
        visit.setStartServiceTime(null);
        visit.setDrivingTimeSecondsFromPreviousStandstill(0);

        return visit;
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}