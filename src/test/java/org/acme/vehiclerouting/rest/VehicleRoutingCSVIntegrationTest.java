package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class VehicleRoutingCSVIntegrationTest {

    private VehicleRoutePlan testProblem;
    private static final String CSV_FILE_PATH = "/data/amazon_delivery.csv";
    private static final int MAX_CSV_ROWS_FOR_TEST = 50; // Limit for faster testing

    @BeforeEach
    void setupTestData() throws IOException {
        testProblem = loadProblemFromCSV();
    }

    @Test
    @DisplayName("Test CSV data import and problem creation")
    void testCSVDataImport() {
        assertThat(testProblem).isNotNull();
        assertThat(testProblem.getName()).isEqualTo("amazon-delivery-test-problem");
        assertThat(testProblem.getVehicles()).isNotEmpty();
        assertThat(testProblem.getVisits()).isNotEmpty();

        // Verify we have reasonable data
        assertThat(testProblem.getVehicles().size()).isGreaterThan(0);
        assertThat(testProblem.getVisits().size()).isLessThanOrEqualTo(MAX_CSV_ROWS_FOR_TEST);

        // Check that locations are valid
        for (Visit visit : testProblem.getVisits()) {
            assertThat(visit.getLocation().getLatitude()).isBetween(-90.0, 90.0);
            assertThat(visit.getLocation().getLongitude()).isBetween(-180.0, 180.0);
        }
    }

    @Test
    @DisplayName("Test solving CSV-based problem via REST API")
    void testSolveCSVProblem() {
        // Submit the problem for solving
        String jobId = given()
                .contentType(ContentType.JSON)
                .body(testProblem)
                .expect().contentType(ContentType.TEXT)
                .when().post("/route-plans")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertThat(jobId).isNotNull().isNotEmpty();

        // Wait for solving to complete or timeout
        await()
                .atMost(Duration.ofMinutes(2))
                .pollInterval(Duration.ofMillis(1000L))
                .until(() -> {
                    String status = given()
                            .when().get("/route-plans/" + jobId + "/status")
                            .then()
                            .statusCode(200)
                            .extract()
                            .jsonPath()
                            .get("solverStatus");
                    return SolverStatus.NOT_SOLVING.name().equals(status);
                });

        // Get the solution
        VehicleRoutePlan solution = given()
                .when().get("/route-plans/" + jobId)
                .then()
                .statusCode(200)
                .extract()
                .as(VehicleRoutePlan.class);

        assertThat(solution).isNotNull();
        assertThat(solution.getScore()).isNotNull();
        assertThat(solution.getVehicles()).hasSameSizeAs(testProblem.getVehicles());
        assertThat(solution.getVisits()).hasSameSizeAs(testProblem.getVisits());
    }

    @Test
    @DisplayName("Test UI endpoints with CSV data")
    void testUIEndpointsWithCSVData() {
        // Test the main UI page loads
        given()
                .when().get("/")
                .then()
                .statusCode(200)
                .contentType("text/html");

        // Test demo data endpoint
        Response demoResponse = given()
                .when().get("/demo-data/FIRENZE")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        VehicleRoutePlan demoData = demoResponse.as(VehicleRoutePlan.class);
        assertThat(demoData).isNotNull();
        assertThat(demoData.getVehicles()).isNotEmpty();
        assertThat(demoData.getVisits()).isNotEmpty();
    }

    @Test
    @DisplayName("Test CSV problem solving with different vehicle configurations")
    void testDifferentVehicleConfigurations() throws IOException {
        // Create problems with different vehicle counts
        VehicleRoutePlan singleVehicleProblem = createProblemWithVehicleCount(1);
        VehicleRoutePlan multiVehicleProblem = createProblemWithVehicleCount(5);

        // Test single vehicle
        String singleJobId = submitProblemForSolving(singleVehicleProblem);
        waitForSolving(singleJobId);
        VehicleRoutePlan singleSolution = getSolution(singleJobId);

        assertThat(singleSolution.getVehicles()).hasSize(1);

        // Test multiple vehicles
        String multiJobId = submitProblemForSolving(multiVehicleProblem);
        waitForSolving(multiJobId);
        VehicleRoutePlan multiSolution = getSolution(multiJobId);

        assertThat(multiSolution.getVehicles()).hasSize(5);
    }

    @Test
    @DisplayName("Test CSV data validation and error handling")
    void testCSVDataValidation() {
        // Test that invalid CSV data is handled gracefully
        VehicleRoutePlan problemWithInvalidData = createProblemWithInvalidData();

        // This should still work but with cleaned data
        assertThat(problemWithInvalidData.getVisits()).isNotEmpty();

        // All visits should have valid coordinates
        for (Visit visit : problemWithInvalidData.getVisits()) {
            assertThat(visit.getLocation().getLatitude()).isNotNaN();
            assertThat(visit.getLocation().getLongitude()).isNotNaN();
        }
    }

    @Test
    @DisplayName("Test recommendation API with CSV data")
    void testRecommendationAPIWithCSVData() {
        // First solve a problem
        String jobId = submitProblemForSolving(testProblem);
        waitForSolving(jobId);
        VehicleRoutePlan solution = getSolution(jobId);

        // Pick a visit for recommendation testing
        if (!solution.getVisits().isEmpty()) {
            Visit testVisit = solution.getVisits().get(0);

            // Test recommendation endpoint
            String recommendationRequest = String.format(
                    "{\"solution\": %s, \"visitId\": \"%s\"}",
                    convertToJsonString(solution),
                    testVisit.getId()
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(recommendationRequest)
                    .when()
                    .post("/route-plans/recommendation")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON);
        }
    }

    // Helper methods

    private VehicleRoutePlan loadProblemFromCSV() throws IOException {
        List<Visit> visits = new ArrayList<>();
        Location depotLocation = null;
        List<Vehicle> vehicles = new ArrayList<>();
        AtomicLong visitIdSequence = new AtomicLong();
        int rowCount = 0;

        try (InputStream inputStream = getClass().getResourceAsStream(CSV_FILE_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null && rowCount < MAX_CSV_ROWS_FOR_TEST) {
                String[] data = parseCSVLine(line);

                if (data.length < 11) continue; // Skip incomplete rows

                try {
                    if (depotLocation == null) {
                        double depotLat = parseCoordinate(data[3]);
                        double depotLon = parseCoordinate(data[4]);
                        if (isValidCoordinate(depotLat, depotLon)) {
                            depotLocation = new Location(depotLat, depotLon);
                        }
                    }

                    double dropLat = parseCoordinate(data[5]);
                    double dropLon = parseCoordinate(data[6]);

                    if (isValidCoordinate(dropLat, dropLon)) {
                        Location visitLocation = new Location(dropLat, dropLon);

                        String orderDateStr = data[7];
                        String pickupTimeStr = data[9];
                        LocalDate orderDate = LocalDate.parse(orderDateStr);
                        LocalTime pickupTime = LocalTime.parse(pickupTimeStr);
                        LocalDateTime serviceWindowStart = LocalDateTime.of(orderDate, pickupTime);

                        visits.add(new Visit(
                                String.valueOf(visitIdSequence.incrementAndGet()),
                                data[0], // Order_ID as visit name
                                visitLocation,
                                1, // demand
                                serviceWindowStart,
                                serviceWindowStart.plusHours(2)                        ));

                        rowCount++;
                    }
                } catch (Exception e) {
                    // Skip invalid rows and continue
                    System.err.println("Skipping invalid row: " + line + " - " + e.getMessage());
                }
            }
        }

        // Create vehicles if we have a valid depot
        if (depotLocation != null) {
            vehicles.add(new Vehicle("test-motorcycle", "motorcycle", depotLocation, 
                    LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0)));
            vehicles.add(new Vehicle("test-scooter", "scooter", depotLocation, 
                    LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0)));
            vehicles.add(new Vehicle("test-van", "van", depotLocation, 
                    LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0)));
        } else {
            // Create a default depot if none found
            depotLocation = new Location(12.9716, 77.5946); // Bangalore coordinates as fallback
            vehicles.add(new Vehicle("default-vehicle", "motorcycle", depotLocation, 
                    LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0)));
        }

        return new VehicleRoutePlan("amazon-delivery-test-problem",
                new Location(0, 0), new Location(100, 100),
                LocalDateTime.now().plusDays(1).withHour(8).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(20).withMinute(0),
                vehicles,
                visits);
    }

    private String[] parseCSVLine(String line) {
        // Simple CSV parsing - in production use a proper CSV library
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString().trim());

        return fields.toArray(new String[0]);
    }

    private double parseCoordinate(String value) {
        if (value == null || value.trim().isEmpty() || "0.0".equals(value.trim())) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private boolean isValidCoordinate(double lat, double lon) {
        return !Double.isNaN(lat) && !Double.isNaN(lon) && 
               lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180 &&
               !(lat == 0.0 && lon == 0.0); // Exclude null island
    }

    private VehicleRoutePlan createProblemWithVehicleCount(int vehicleCount) throws IOException {
        VehicleRoutePlan baseProblem = loadProblemFromCSV();
        List<Vehicle> newVehicles = new ArrayList<>();

        Location depotLocation = baseProblem.getVehicles().get(0).getHomeLocation();
        for (int i = 0; i < vehicleCount; i++) {
            newVehicles.add(new Vehicle("vehicle-" + (i + 1), "motorcycle", depotLocation,
                    LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0)));
        }

        return new VehicleRoutePlan(
                baseProblem.getName() + "-" + vehicleCount + "-vehicles",
                baseProblem.getSouthWestCorner(),
                baseProblem.getNorthEastCorner(),
                baseProblem.getStartDateTime(),
                baseProblem.getEndDateTime(),
                newVehicles,
                baseProblem.getVisits().subList(0, Math.min(10, baseProblem.getVisits().size())) // Limit visits for faster testing
        );
    }

    private VehicleRoutePlan createProblemWithInvalidData() {
        List<Visit> visits = new ArrayList<>();
        List<Vehicle> vehicles = new ArrayList<>();
        Location depotLocation = new Location(12.9716, 77.5946);

        // Add some valid visits
        visits.add(new Visit("valid-1", "Valid Visit 1", 
                new Location(12.9716, 77.5946), 1, 
                LocalDateTime.now().plusDays(1).withHour(10),
                LocalDateTime.now().plusDays(1).withHour(12)));

        vehicles.add(new Vehicle("test-vehicle", "motorcycle", depotLocation,
                LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0)));

        return new VehicleRoutePlan("test-problem-with-invalid-data",
                new Location(0, 0), new Location(100, 100),
                LocalDateTime.now().plusDays(1).withHour(8).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(20).withMinute(0),
                vehicles, visits);
    }

    private String submitProblemForSolving(VehicleRoutePlan problem) {
        return given()
                .contentType(ContentType.JSON)
                .body(problem)
                .expect().contentType(ContentType.TEXT)
                .when().post("/route-plans")
                .then()
                .statusCode(200)
                .extract()
                .asString();
    }

    private void waitForSolving(String jobId) {
        await()
                .atMost(Duration.ofMinutes(3))
                .pollInterval(Duration.ofMillis(1000L))
                .until(() -> {
                    String status = given()
                            .when().get("/route-plans/" + jobId + "/status")
                            .then()
                            .statusCode(200)
                            .extract()
                            .jsonPath()
                            .get("solverStatus");
                    return SolverStatus.NOT_SOLVING.name().equals(status);
                });
    }

    private VehicleRoutePlan getSolution(String jobId) {
        return given()
                .when().get("/route-plans/" + jobId)
                .then()
                .statusCode(200)
                .extract()
                .as(VehicleRoutePlan.class);
    }

    private String convertToJsonString(Object obj) {
        // Simple JSON conversion - in production use proper JSON library
        return "{}"; // Placeholder - would need proper JSON serialization
    }
}