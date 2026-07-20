package qa.beneapp.stepdefinitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * Step definitions for the @public API scenario in PublicSmoke.feature.
 * Hits jsonplaceholder.typicode.com (a stable public fake REST API) so this
 * scenario passes without the bene-app API running.
 */
public class PublicApiSteps {

    private Response response;

    @When("I call the public posts API for post id {int}")
    public void iCallThePublicPostsApiForPostId(int id) {
        response = RestAssured.given()
                .baseUri("https://jsonplaceholder.typicode.com")
                .filter(new AllureRestAssured())
                .get("/posts/" + id);
    }

    @Then("the public API response status should be {int}")
    public void thePublicApiResponseStatusShouldBe(int expected) {
        assertEquals("Unexpected HTTP status", expected, response.getStatusCode());
    }

    @Then("the public API response should contain a non-empty title")
    public void thePublicApiResponseShouldContainANonEmptyTitle() {
        String title = response.jsonPath().getString("title");
        assertFalse("Expected a non-empty title", title == null || title.trim().isEmpty());
    }
}
