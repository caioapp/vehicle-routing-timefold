
package org.acme.vehiclerouting.service;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.util.CSVDataLoader;
import ai.timefold.solver.core.api.solver.SolverStatus;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class VehicleRoutingDataService {

    public VehicleRoutePlan loadFromCSV() {
        VehicleRoutePlan plan = null;
        try {
            CSVDataLoader.CSVLoadResult result = CSVDataLoader.loadFromCSV();
            plan = result.getProblem();
            System.out.println("Loaded problem from CSV with " + 
                plan.getVehicles().size() + " vehicles and " + 
                plan.getVisits().size() + " visits");

            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return plan;

    }

    /**
     * Creates the Amazon delivery problem
     */
    public VehicleRoutePlan createAmazonDeliveryProblem() {
        List<Vehicle> vehicles = createAmazonVehicleFleet();
        List<Visit> visits = createAmazonDeliveryVisits();

        VehicleRoutePlan plan = new VehicleRoutePlan(visits, vehicles);

        plan.setName("amazon-delivery-india-problem");
        plan.setSouthWestCorner(new Location(10.033064, 72.784115));
        plan.setNorthEastCorner(new Location(30.929584, 88.395507));
        plan.setStartDateTime("2022-02-11T06:00:00");
        plan.setEndDateTime("2022-04-06T22:00:00");
        plan.setSolverStatus(SolverStatus.NOT_SOLVING);
        plan.setScoreExplanation("Initial Amazon delivery problem");
        System.out.println("Created Amazon delivery problem: " + plan.toString());

        return plan;
    }

    /**
     * ADDED: Creates a minimal test problem for debugging
     */
    public VehicleRoutePlan createMinimalTestProblem() {
        List<Vehicle> vehicles = new ArrayList<>();
        List<Visit> visits = new ArrayList<>();

        LocalDateTime departureTime = LocalDateTime.of(2022, 9, 17, 8, 0);

        // Only 2 vehicles
        vehicles.add(createVehicle("vehicle-1", "van", 
            new Location(19.0760, 72.8777), 5, departureTime));
        vehicles.add(createVehicle("vehicle-2", "motorcycle", 
            new Location(18.5204, 73.8567), 3, departureTime));

        // Only 4 visits
        visits.add(createVisit("1", "Mumbai Delivery", 
            new Location(19.0896, 72.8656),
            "2022-09-17T10:00:00", "2022-09-17T12:00:00", 1800));
        visits.add(createVisit("2", "Pune Delivery", 
            new Location(18.5289, 73.8732),
            "2022-09-17T14:00:00", "2022-09-17T16:00:00", 1800));
        visits.add(createVisit("3", "Nashik Delivery", 
            new Location(19.9975, 73.7898),
            "2022-09-17T11:00:00", "2022-09-17T13:00:00", 1800));
        visits.add(createVisit("4", "Thane Delivery", 
            new Location(19.2183, 72.9781),
            "2022-09-17T15:00:00", "2022-09-17T17:00:00", 1800));

        VehicleRoutePlan plan = new VehicleRoutePlan(visits, vehicles);

        plan.setName("minimal-test-problem");
        plan.setSouthWestCorner(new Location(18.0, 72.0));
        plan.setNorthEastCorner(new Location(20.0, 74.0));
        plan.setStartDateTime("2022-09-17T08:00:00");
        plan.setEndDateTime("2022-09-17T18:00:00");
        plan.setSolverStatus(SolverStatus.NOT_SOLVING);
        plan.setScoreExplanation("Minimal test problem");
        System.out.println("Created minimal test problem: " + plan.toString());

        return plan;
    }

    private List<Vehicle> createAmazonVehicleFleet() {
        List<Vehicle> vehicles = new ArrayList<>();


        

        System.out.println("Created Amazon fleet with " + vehicles.size() + " vehicles");
        return vehicles;
    }

    private Vehicle createVehicle(String id, String style, Location location, 
                                 int capacity, LocalDateTime departureTime) {
        Vehicle vehicle = new Vehicle(id, style, location, capacity, departureTime);
        return vehicle;
    }

    private List<Visit> createAmazonDeliveryVisits() {
        List<Visit> visits = new ArrayList<>();

        // Reduce to 15 visits for better solver performance
        visits.add(createVisit("1", "Mumbai Delivery", 
            new Location(19.0896, 72.8656),
            "2022-09-17T10:00:00", "2022-09-17T12:00:00", 1800));
        visits.add(createVisit("2", "Pune Delivery", 
            new Location(18.5289, 73.8732),
            "2022-09-17T14:00:00", "2022-09-17T16:00:00", 1800));
        visits.add(createVisit("3", "Nashik Delivery", 
            new Location(19.9975, 73.7898),
            "2022-09-17T11:00:00", "2022-09-17T13:00:00", 1800));
        visits.add(createVisit("4", "Ahmedabad Delivery", 
            new Location(23.0225, 72.5714),
            "2022-09-17T09:00:00", "2022-09-17T11:00:00", 1800));
        visits.add(createVisit("5", "Bangalore Delivery", 
            new Location(12.9716, 77.5946),
            "2022-09-17T15:00:00", "2022-09-17T17:00:00", 1800));
        visits.add(createVisit("6", "Chennai Delivery", 
            new Location(13.0827, 80.2707),
            "2022-09-17T13:00:00", "2022-09-17T15:00:00", 1800));
        visits.add(createVisit("7", "Hyderabad Delivery", 
            new Location(17.3850, 78.4867),
            "2022-09-17T12:00:00", "2022-09-17T14:00:00", 1800));
        visits.add(createVisit("8", "Kolkata Delivery", 
            new Location(22.5726, 88.3639),
            "2022-09-17T16:00:00", "2022-09-17T18:00:00", 1800));
        visits.add(createVisit("9", "Jaipur Delivery", 
            new Location(26.9124, 75.7873),
            "2022-09-17T08:00:00", "2022-09-17T10:00:00", 1800));
        visits.add(createVisit("10", "Kochi Delivery", 
            new Location(9.9312, 76.2673),
            "2022-09-17T17:00:00", "2022-09-17T19:00:00", 1800));
        visits.add(createVisit("11", "Indore Delivery", 
            new Location(22.7196, 75.8577),
            "2022-09-17T10:30:00", "2022-09-17T12:30:00", 1800));
        visits.add(createVisit("12", "Bhopal Delivery", 
            new Location(23.2599, 77.4126),
            "2022-09-17T14:30:00", "2022-09-17T16:30:00", 1800));
        visits.add(createVisit("13", "Coimbatore Delivery", 
            new Location(11.0168, 76.9558),
            "2022-09-17T09:30:00", "2022-09-17T11:30:00", 1800));
        visits.add(createVisit("14", "Mysore Delivery", 
            new Location(12.2958, 76.6394),
            "2022-09-17T15:30:00", "2022-09-17T17:30:00", 1800));
        visits.add(createVisit("15", "Goa Delivery", 
            new Location(15.2993, 74.1240),
            "2022-09-17T13:30:00", "2022-09-17T15:30:00", 1800));

        System.out.println("Created " + visits.size() + " Amazon delivery visits");
        return visits;
    }

    // Loads from CSV file in resources
    public VehicleRoutePlan createSafeProblem() {
        
        VehicleRoutePlan plan = loadFromCSV();
        System.out.println("Created safe Amazon delivery problem: " + plan.toString());

        return plan;
    }

    private Visit createVisit(String id, String name, Location location,
                            String minStartTime, String maxEndTime, long serviceDuration) {
        Visit visit = new Visit(id, name, location);
        visit.setDemand(1);
        visit.setMinStartTime(parseDateTime(minStartTime));
        visit.setMaxEndTime(parseDateTime(maxEndTime));

        visit.setArrivalTime(null);
        visit.setDepartureTime(null);
        visit.setStartServiceTime(null);

        return visit;
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}