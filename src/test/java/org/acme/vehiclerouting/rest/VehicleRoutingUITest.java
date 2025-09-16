package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.time.Duration;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VehicleRoutingUITest {

    @Test
    @Order(1)
    @DisplayName("Test main UI page loads correctly")
    void testMainUIPageLoads() {
        Response response = given()
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .contentType("text/html")
                .body(containsString("Vehicle routing with capacity and time windows"))
                .body(containsString("Generate optimal route plan"))
                .body(containsString("app.js"))
                .extract()
                .response();

        // Verify key UI elements are present
        String htmlContent = response.getBody().asString();
        assert htmlContent.contains("id=\"map\"") : "Map container should be present";
        assert htmlContent.contains("id=\"solveButton\"") : "Solve button should be present";
        assert htmlContent.contains("REST API Guide") : "API guide should be present";
    }

    @Test
    @Order(2)
    @DisplayName("Test JavaScript assets load correctly")
    void testJavaScriptAssetsLoad() {
        // Test main application JS
        given()
                .when()
                .get("/app.js")
                .then()
                .statusCode(200)
                .contentType(containsString("javascript"));

        // Test recommended fit JS
        given()
                .when()
                .get("/recommended-fit.js")
                .then()
                .statusCode(200)
                .contentType(containsString("javascript"));

        // Test score analysis JS
        given()
                .when()
                .get("/score-analysis.js")
                .then()
                .statusCode(200)
                .contentType(containsString("javascript"));
    }

    @Test
    @Order(3)
    @DisplayName("Test demo data API endpoints for UI")
    void testDemoDataEndpoints() {
        // Test FIRENZE demo data
        given()
                .when()
                .get("/demo-data/FIRENZE")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", notNullValue())
                .body("vehicles", not(empty()))
                .body("visits", not(empty()))
                .body("southWestCorner", notNullValue())
                .body("northEastCorner", notNullValue());
    }

    @Test
    @Order(4)
    @DisplayName("Test CSV-based problem generation for UI")
    void testCSVProblemGeneration() {
        // Test the new CSV problem endpoint
        Response response = given()
                .when()
                .get("/route/problem")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("amazon-delivery-problem"))
                .body("vehicles", not(empty()))
                .body("visits", not(empty()))
                .extract()
                .response();

        // Verify the structure is suitable for UI consumption
        String problemJson = response.getBody().asString();
        assert problemJson.contains("southWestCorner") : "Should contain map boundaries";
        assert problemJson.contains("northEastCorner") : "Should contain map boundaries";
        assert problemJson.contains("startDateTime") : "Should contain time boundaries";
        assert problemJson.contains("endDateTime") : "Should contain time boundaries";
    }

    @Test
    @Order(5)
    @DisplayName("Test solving workflow with UI data format")
    void testSolvingWorkflowWithUIData() {
        // First, get demo data (simulating UI behavior)
        Response demoResponse = given()
                .when()
                .get("/demo-data/FIRENZE")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String problemData = demoResponse.getBody().asString();

        // Submit for solving (as the UI would do)
        Response solveResponse = given()
                .contentType(ContentType.JSON)
                .body(problemData)
                .when()
                .post("/route-plans")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .extract()
                .response();

        String jobId = solveResponse.getBody().asString();
        assert jobId != null && !jobId.trim().isEmpty() : "Job ID should be returned";

        // Check status (as UI polling would do)
        given()
                .when()
                .get("/route-plans/" + jobId + "/status")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("solverStatus", notNullValue())
                .body("name", notNullValue());

        // Get solution (as UI would do after solving)
        given()
                .when()
                .get("/route-plans/" + jobId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("vehicles", not(empty()))
                .body("visits", not(empty()))
                .body("score", notNullValue());
    }

    @Test
    @Order(6)
    @DisplayName("Test recommendation API for UI interactions")
    void testRecommendationAPIForUI() {
        // Get demo data
        Response demoResponse = given()
                .when()
                .get("/demo-data/FIRENZE")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Submit for solving
        String jobId = given()
                .contentType(ContentType.JSON)
                .body(demoResponse.getBody().asString())
                .when()
                .post("/route-plans")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        // Wait a bit for some solving to happen
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Get current solution
        Response solutionResponse = given()
                .when()
                .get("/route-plans/" + jobId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Test score analysis endpoint (used by UI)
        given()
                .contentType(ContentType.JSON)
                .body(solutionResponse.getBody().asString())
                .when()
                .put("/route-plans/analyze")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @Order(7)
    @DisplayName("Test error handling in UI endpoints")
    void testErrorHandlingInUIEndpoints() {
        // Test non-existent job ID
        given()
                .when()
                .get("/route-plans/non-existent-job-id")
                .then()
                .statusCode(404);

        // Test invalid problem data
        given()
                .contentType(ContentType.JSON)
                .body("{\"invalid\": \"data\"}")
                .when()
                .post("/route-plans")
                .then()
                .statusCode(anyOf(is(400), is(500))); // Should handle gracefully

        // Test malformed recommendation request
        given()
                .contentType(ContentType.JSON)
                .body("{\"malformed\": \"request\"}")
                .when()
                .post("/route-plans/recommendation")
                .then()
                .statusCode(anyOf(is(400), is(500))); // Should handle gracefully
    }

    @Test
    @Order(8)
    @DisplayName("Test CSV data integration with UI workflow")
    void testCSVDataIntegrationWithUIWorkflow() {
        // Test the full workflow: CSV → Problem → Solving → UI Display

        // 1. Generate problem from CSV
        Response csvProblemResponse = given()
                .when()
                .get("/route/problem")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        String csvProblemData = csvProblemResponse.getBody().asString();

        // 2. Submit CSV-based problem for solving
        String jobId = given()
                .contentType(ContentType.JSON)
                .body(csvProblemData)
                .when()
                .post("/route-plans")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .extract()
                .asString();

        // 3. Monitor progress (simulate UI polling)
        for (int i = 0; i < 10; i++) {
            Response statusResponse = given()
                    .when()
                    .get("/route-plans/" + jobId + "/status")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            String solverStatus = statusResponse.jsonPath().getString("solverStatus");
            if ("NOT_SOLVING".equals(solverStatus)) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // 4. Get final solution for UI display
        given()
                .when()
                .get("/route-plans/" + jobId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("amazon-delivery-problem"))
                .body("vehicles", not(empty()))
                .body("visits", not(empty()))
                .body("score", notNullValue())
                .body("totalDrivingTimeSeconds", notNullValue());

        // 5. Test termination (UI stop button functionality)
        given()
                .when()
                .delete("/route-plans/" + jobId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @Order(9)
    @DisplayName("Test concurrent UI requests")
    void testConcurrentUIRequests() {
        // Get demo data
        Response demoResponse = given()
                .when()
                .get("/demo-data/FIRENZE")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String problemData = demoResponse.getBody().asString();

        // Submit multiple concurrent requests (simulate multiple users)
        String jobId1 = given()
                .contentType(ContentType.JSON)
                .body(problemData)
                .when()
                .post("/route-plans")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        String jobId2 = given()
                .contentType(ContentType.JSON)
                .body(problemData)
                .when()
                .post("/route-plans")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        // Verify both jobs are running independently
        assert !jobId1.equals(jobId2) : "Job IDs should be unique";

        given()
                .when()
                .get("/route-plans/" + jobId1 + "/status")
                .then()
                .statusCode(200);

        given()
                .when()
                .get("/route-plans/" + jobId2 + "/status")
                .then()
                .statusCode(200);

        // Clean up
        given().when().delete("/route-plans/" + jobId1);
        given().when().delete("/route-plans/" + jobId2);
    }

    @Test
    @Order(10)
    @DisplayName("Test UI data formats and serialization")
    void testUIDataFormatsAndSerialization() {
        // Test that all data returned by APIs can be properly consumed by the UI

        // Test demo data format
        Response demoResponse = given()
                .when()
                .get("/demo-data/FIRENZE")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        // Verify JSON structure is complete for UI consumption
        demoResponse.then()
                .body("name", notNullValue())
                .body("southWestCorner.latitude", notNullValue())
                .body("southWestCorner.longitude", notNullValue())
                .body("northEastCorner.latitude", notNullValue())
                .body("northEastCorner.longitude", notNullValue())
                .body("startDateTime", notNullValue())
                .body("endDateTime", notNullValue())
                .body("vehicles[0].id", notNullValue())
                .body("vehicles[0].homeLocation.latitude", notNullValue())
                .body("vehicles[0].homeLocation.longitude", notNullValue())
                .body("visits[0].id", notNullValue())
                .body("visits[0].name", notNullValue())
                .body("visits[0].location.latitude", notNullValue())
                .body("visits[0].location.longitude", notNullValue());

        // Test CSV problem format
        given()
                .when()
                .get("/route/problem")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("vehicles[0].homeLocation.latitude", notNullValue())
                .body("vehicles[0].homeLocation.longitude", notNullValue())
                .body("visits[0].location.latitude", notNullValue())
                .body("visits[0].location.longitude", notNullValue());
    }
}