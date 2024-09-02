/*package org.example;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AuthUtils {
    private final EndPoints endPoints = new EndPoints();

    public String getAuthToken() {
        // Параметры для авторизации
        String email = "5test.user@example.com";
        String password = "password123";

        Response response = given()
                .baseUri(endPoints.baseUrl)
                .contentType("application/json")
                .body("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}")
                .when()
                .post(endPoints.login)
                .then()
                .statusCode(200)
                .extract()
                .response();

        return response.jsonPath().getString("token");
    }
}
*/