package org.acme.vehiclerouting.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;

/**
 * Utility class for loading vehicle routing problems from CSV data.
 * Handles the Amazon delivery dataset format with robust error handling.
 */
public class CSVDataLoader {

    private static final String CSV_FILE_PATH = "/data/amazon_delivery.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // CSV column indices based on the Amazon delivery dataset
    private static final int ORDER_ID_COL = 0;
    private static final int AGENT_AGE_COL = 1;
    private static final int AGENT_RATING_COL = 2;
    private static final int STORE_LAT_COL = 3;
    private static final int STORE_LON_COL = 4;
    private static final int DROP_LAT_COL = 5;
    private static final int DROP_LON_COL = 6;
    private static final int ORDER_DATE_COL = 7;
    private static final int ORDER_TIME_COL = 8;
    private static final int PICKUP_TIME_COL = 9;
    private static final int WEATHER_COL = 10;
    private static final int TRAFFIC_COL = 11;
    private static final int VEHICLE_COL = 12;
    private static final int AREA_COL = 13;
    private static final int DELIVERY_TIME_COL = 14;
    private static final int CATEGORY_COL = 15;

    public static class CSVLoadResult {
        private final VehicleRoutePlan problem;
        private final int totalRows;
        private final int validRows;
        private final int skippedRows;
        private final List<String> errors;

        public CSVLoadResult(VehicleRoutePlan problem, int totalRows, int validRows, int skippedRows, List<String> errors) {
            this.problem = problem;
            this.totalRows = totalRows;
            this.validRows = validRows;
            this.skippedRows = skippedRows;
            this.errors = errors;
        }

        public VehicleRoutePlan getProblem() { return problem; }
        public int getTotalRows() { return totalRows; }
        public int getValidRows() { return validRows; }
        public int getSkippedRows() { return skippedRows; }
        public List<String> getErrors() { return errors; }
    }

    /**
     * Load a vehicle routing problem from the CSV file with default parameters.
     */
    public static CSVLoadResult loadFromCSV() throws IOException {
        return loadFromCSV(100, true);
    }

    /**
     * Load a vehicle routing problem from the CSV file.
     * 
     * @param maxRows Maximum number of rows to process (for testing)
     * @param createOptimalVehicles Whether to create an optimal number of vehicles based on data
     * @return CSVLoadResult containing the problem and statistics
     */
    public static CSVLoadResult loadFromCSV(int maxRows, boolean createOptimalVehicles) throws IOException {
        List<Visit> visits = new ArrayList<>();
        Map<String, Location> depotLocations = new HashMap<>();
        List<Vehicle> vehicles = new ArrayList<>();
        AtomicLong visitIdSequence = new AtomicLong();
        List<String> errors = new ArrayList<>();

        int totalRows = 0;
        int validRows = 0;
        int skippedRows = 0;

        try (InputStream inputStream = CSVDataLoader.class.getResourceAsStream(CSV_FILE_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            if (inputStream == null) {
                throw new IOException("CSV file not found: " + CSV_FILE_PATH);
            }

            String headerLine = reader.readLine(); // Skip header
            if (headerLine == null) {
                throw new IOException("Empty CSV file");
            }

            String line;
            while ((line = reader.readLine()) != null && totalRows < maxRows) {
                totalRows++;

                try {
                    String[] data = parseCSVLine(line);

                    if (data.length < 16) {
                        skippedRows++;
                        errors.add("Row " + totalRows + ": Insufficient columns (" + data.length + " < 16)");
                        continue;
                    }

                    // Parse coordinates
                    double storeLat = parseCoordinate(data[STORE_LAT_COL]);
                    double storeLon = parseCoordinate(data[STORE_LON_COL]);
                    double dropLat = parseCoordinate(data[DROP_LAT_COL]);
                    double dropLon = parseCoordinate(data[DROP_LON_COL]);

                    if (!isValidCoordinate(storeLat, storeLon) || !isValidCoordinate(dropLat, dropLon)) {
                        skippedRows++;
                        errors.add("Row " + totalRows + ": Invalid coordinates");
                        continue;
                    }

                    // Store depot locations for vehicle creation
                    String depotKey = String.format("%.6f,%.6f", storeLat, storeLon);
                    depotLocations.put(depotKey, new Location(storeLat, storeLon));

                    // Parse date and time
                    LocalDate orderDate;
                    LocalTime pickupTime;
                    try {
                        orderDate = LocalDate.parse(data[ORDER_DATE_COL], DATE_FORMATTER);
                        pickupTime = LocalTime.parse(data[PICKUP_TIME_COL], TIME_FORMATTER);
                    } catch (DateTimeParseException e) {
                        skippedRows++;
                        errors.add("Row " + totalRows + ": Invalid date/time format");
                        continue;
                    }

                    // Create visit location and time window
                    Location visitLocation = new Location(dropLat, dropLon);
                    LocalDateTime serviceWindowStart = LocalDateTime.of(orderDate, pickupTime);
                    LocalDateTime serviceWindowEnd = serviceWindowStart.plusHours(2); // 2-hour window
                    // Create visit
                    Visit visit = new Visit(
                            String.valueOf(visitIdSequence.incrementAndGet()),
                            data[ORDER_ID_COL] + " (" + data[CATEGORY_COL] + ")", // Name with category
                            visitLocation,
                            1, // Standard demand
                            serviceWindowStart,
                            serviceWindowEnd
                    );

                    visits.add(visit);
                    validRows++;

                } catch (Exception e) {
                    skippedRows++;
                    errors.add("Row " + totalRows + ": " + e.getMessage());
                }
            }
        }

        // Create vehicles based on depot locations
        vehicles = createVehicles(depotLocations, visits.size(), createOptimalVehicles);

        // Calculate map boundaries
        Location southWest = calculateSouthWestCorner(visits, vehicles);
        Location northEast = calculateNorthEastCorner(visits, vehicles);

        // Create problem
        VehicleRoutePlan problem = new VehicleRoutePlan(
                "amazon-delivery-csv-problem",
                southWest,
                northEast,
                LocalDateTime.now().plusDays(1).withHour(6).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().plusDays(1).withHour(22).withMinute(0).withSecond(0).withNano(0),
                vehicles,
                visits
        );

        return new CSVLoadResult(problem, totalRows, validRows, skippedRows, errors);
    }

    /**
     * Parse a CSV line handling quotes and commas properly.
     */
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        boolean escapeNext = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (escapeNext) {
                field.append(c);
                escapeNext = false;
            } else if (c == '\\') {
                escapeNext = true;
            } else if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Double quote escape
                    field.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
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

    /**
     * Parse coordinate value with error handling.
     */
    private static double parseCoordinate(String value) {
        if (value == null || value.trim().isEmpty() || "0.0".equals(value.trim())) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /**
     * Validate coordinate values.
     */
    private static boolean isValidCoordinate(double lat, double lon) {
        return !Double.isNaN(lat) && !Double.isNaN(lon) && 
               lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180 &&
               !(Math.abs(lat) < 0.0001 && Math.abs(lon) < 0.0001); // Exclude null island
    }

    /**
     * Create vehicles based on depot locations and problem size.
     */
    private static List<Vehicle> createVehicles(Map<String, Location> depotLocations, int visitCount, boolean optimize) {
        List<Vehicle> vehicles = new ArrayList<>();

        if (depotLocations.isEmpty()) {
            // Create default depot if none found
            Location defaultDepot = new Location(12.9716, 77.5946); // Bangalore coordinates
            vehicles.add(new Vehicle("default-vehicle", "motorcycle", defaultDepot,
                    LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0)));
            return vehicles;
        }

        // Calculate optimal number of vehicles
        int vehicleCount;
        if (optimize) {
            vehicleCount = Math.max(1, Math.min(depotLocations.size(), (visitCount / 10) + 1));
        } else {
            vehicleCount = Math.min(3, depotLocations.size()); // Fixed number for testing
        }

        String[] vehicleTypes = {"motorcycle", "scooter", "van"};
        List<Location> depots = new ArrayList<>(depotLocations.values());

        for (int i = 0; i < vehicleCount; i++) {
            Location depot = depots.get(i % depots.size());
            String vehicleType = vehicleTypes[i % vehicleTypes.length];

            vehicles.add(new Vehicle(
                    "vehicle-" + (i + 1),
                    vehicleType,
                    depot,
                    LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0)
            ));
        }

        return vehicles;
    }

    /**
     * Calculate the south-west corner for map boundaries.
     */
    private static Location calculateSouthWestCorner(List<Visit> visits, List<Vehicle> vehicles) {
        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;

        for (Visit visit : visits) {
            minLat = Math.min(minLat, visit.getLocation().getLatitude());
            minLon = Math.min(minLon, visit.getLocation().getLongitude());
        }

        for (Vehicle vehicle : vehicles) {
            minLat = Math.min(minLat, vehicle.getHomeLocation().getLatitude());
            minLon = Math.min(minLon, vehicle.getHomeLocation().getLongitude());
        }

        // Add small buffer
        return new Location(minLat - 0.01, minLon - 0.01);
    }

    /**
     * Calculate the north-east corner for map boundaries.
     */
    private static Location calculateNorthEastCorner(List<Visit> visits, List<Vehicle> vehicles) {
        double maxLat = Double.MIN_VALUE;
        double maxLon = Double.MIN_VALUE;

        for (Visit visit : visits) {
            maxLat = Math.max(maxLat, visit.getLocation().getLatitude());
            maxLon = Math.max(maxLon, visit.getLocation().getLongitude());
        }

        for (Vehicle vehicle : vehicles) {
            maxLat = Math.max(maxLat, vehicle.getHomeLocation().getLatitude());
            maxLon = Math.max(maxLon, vehicle.getHomeLocation().getLongitude());
        }

        // Add small buffer
        return new Location(maxLat + 0.01, maxLon + 0.01);
    }

    /**
     * Create a test problem with a limited number of visits for quick testing.
     */
    public static CSVLoadResult createTestProblem() throws IOException {
        return loadFromCSV(20, false); // Load only 20 visits for quick testing
    }

    /**
     * Create a problem suitable for performance testing.
     */
    public static CSVLoadResult createPerformanceTestProblem() throws IOException {
        return loadFromCSV(200, true); // Load 200 visits with optimized vehicles
    }
}