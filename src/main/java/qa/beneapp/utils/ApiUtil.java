package qa.beneapp.utils;

import qa.beneapp.config.ConfigManager;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * ApiUtil - thin RestAssured wrapper for the bene-app REST API.
 *
 * Reads the base URL and bearer token from the active config, attaches the
 * Allure RestAssured filter (so every call appears in the Allure report), and
 * exposes simple get / post / put / delete helpers.
 *
 * Example:
 *   Response r = ApiUtil.get("/api/accounts");
 *   r.then().statusCode(200);
 */
public final class ApiUtil {

    private ApiUtil() {
    }

    /** Base request spec: base URI + JSON headers + auth token + Allure logging. */
    public static RequestSpecification request() {
        return RestAssured.given()
                .baseUri(ConfigManager.apiBaseUrl())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + ConfigManager.apiToken())
                .filter(new AllureRestAssured());
    }

    public static Response get(String path) {
        return request().get(path);
    }

    public static Response post(String path, String jsonBody) {
        return request().body(jsonBody).post(path);
    }

    public static Response put(String path, String jsonBody) {
        return request().body(jsonBody).put(path);
    }

    public static Response delete(String path) {
        return request().delete(path);
    }
}
