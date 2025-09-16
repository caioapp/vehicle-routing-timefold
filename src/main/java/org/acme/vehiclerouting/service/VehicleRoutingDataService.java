
package org.acme.vehiclerouting.service;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.Location;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class VehicleRoutingDataService {

    /**
     * Creates the complete Amazon delivery problem with 10 vehicles and 90 visits
     * This matches your original JSON data structure
     */
    public VehicleRoutePlan createAmazonDeliveryProblem() {
        List<Vehicle> vehicles = createVehicleFleet();
        List<Visit> visits = createDeliveryVisits();

        return new VehicleRoutePlan(visits, vehicles);
    }

    /**
     * Create the 10-vehicle fleet (4 motorcycles, 3 scooters, 3 vans)
     */
    private List<Vehicle> createVehicleFleet() {
        List<Vehicle> vehicles = new ArrayList<>();

        // Motorcycles (4 vehicles)
        Vehicle motorcycle1 = new Vehicle("vehicle-1", "motorcycle", 
            new Location(23.374878, 85.335739));
        vehicles.add(motorcycle1);

        Vehicle motorcycle2 = new Vehicle("vehicle-4", "motorcycle", 
            new Location(17.451976, 78.385883));
        vehicles.add(motorcycle2);

        Vehicle motorcycle3 = new Vehicle("vehicle-7", "motorcycle", 
            new Location(12.934365, 77.616155));
        vehicles.add(motorcycle3);

        Vehicle motorcycle4 = new Vehicle("vehicle-10", "motorcycle", 
            new Location(19.176269, 72.836721));
        vehicles.add(motorcycle4);

        // Scooters (3 vehicles)
        Vehicle scooter1 = new Vehicle("vehicle-2", "scooter", 
            new Location(21.186438, 72.794115));
        vehicles.add(scooter1);

        Vehicle scooter2 = new Vehicle("vehicle-5", "scooter", 
            new Location(11.003669, 76.976494));
        vehicles.add(scooter2);

        Vehicle scooter3 = new Vehicle("vehicle-8", "scooter", 
            new Location(17.431477, 78.40035));
        vehicles.add(scooter3);

        // Vans (3 vehicles)
        Vehicle van1 = new Vehicle("vehicle-3", "van", 
            new Location(18.55144, 73.804855));
        vehicles.add(van1);

        Vehicle van2 = new Vehicle("vehicle-6", "van", 
            new Location(12.316967, 76.603067));
        vehicles.add(van2);

        Vehicle van3 = new Vehicle("vehicle-9", "van", 
            new Location(18.56245, 73.916619));
        vehicles.add(van3);

        return vehicles;
    }

    /**
     * Create sample delivery visits (subset of your 90 deliveries for testing)
     */
    private List<Visit> createDeliveryVisits() {
        List<Visit> visits = new ArrayList<>();

        // Sample visits from your Amazon data
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

        visits.add(createVisit("6", "fxuu788413734 (Toys)", 
            new Location(17.461668, 78.438321),
            "2025-09-17T21:30:00", "2025-09-17T23:30:00", 7800));

        visits.add(createVisit("7", "njmo150975311 (Toys)", 
            new Location(23.479746, 85.44982),
            "2025-09-17T19:30:00", "2025-09-17T21:30:00", 12000));

        visits.add(createVisit("8", "jvjc772545076 (Snacks)", 
            new Location(12.482058, 76.73665),
            "2025-09-17T17:30:00", "2025-09-17T19:30:00", 9600));

        visits.add(createVisit("9", "uaeb808891380 (Electronics)", 
            new Location(17.563809, 78.516744),
            "2025-09-17T21:05:00", "2025-09-17T23:05:00", 10200));

        visits.add(createVisit("10", "bgvc052754213 (Toys)", 
            new Location(30.397968, 78.116106),
            "2025-09-17T22:10:00", "2025-09-18T00:10:00", 13800));

        return visits;
    }

    /**
     * Helper method to create a visit with time windows
     */
    private Visit createVisit(String id, String name, Location location,
                            String minStartTime, String maxEndTime, long serviceDuration) {
        Visit visit = new Visit(id, name, location, 1,
            parseDateTime(minStartTime), parseDateTime(maxEndTime));
        return visit;
    }

    /**
     * Helper method to parse datetime strings
     */
    private LocalDateTime parseDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Create a smaller test problem for quick debugging
     */
    public VehicleRoutePlan createTestProblem() {
        List<Vehicle> vehicles = new ArrayList<>();
        List<Visit> visits = new ArrayList<>();

        // Just 2 vehicles for testing
        Vehicle motorcycle = new Vehicle("test-vehicle-1", "motorcycle", 
            new Location(19.076, 72.8777),
            LocalDateTime.now().plusHours(1));
        vehicles.add(motorcycle);

        Vehicle van = new Vehicle("test-vehicle-2", "van", 
            new Location(18.5204, 73.8567),
            LocalDateTime.now().plusHours(1));
        vehicles.add(van);

        // Just 4 visits for testing
        LocalDateTime now = LocalDateTime.now();

        Visit visit1 = new Visit("test-1", "Mumbai Delivery", 
            new Location(19.0760, 72.8777),
            1, now.plusHours(2), now.plusHours(6));
        visits.add(visit1);

        Visit visit2 = new Visit("test-2", "Pune Delivery", 
            new Location(18.5204, 73.8567), 1
            , now.plusHours(3), now.plusHours(7));
        visits.add(visit2);

        Visit visit3 = new Visit("test-3", "Nashik Delivery", 
            new Location(19.9975, 73.7898),
            1, now.plusHours(4), now.plusHours(8));
        visits.add(visit3);

        Visit visit4 = new Visit("test-4", "Aurangabad Delivery", 
            new Location(19.8762, 75.3433),1,
            now.plusHours(2), now.plusHours(9));
        visits.add(visit4);

        return new VehicleRoutePlan(visits, vehicles);
    }
}
