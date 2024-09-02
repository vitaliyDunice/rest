/*package org.example;

import io.restassured.response.Response;
import org.example.AuthUtils;
import org.example.EndPoints;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

public class PostTests {
    private final EndPoints endPoints = new EndPoints();
    private final AuthUtils authUtils = new AuthUtils();

    @Test(description = "Позитивный тест: Создание новости")
    public void testCreatePost() {
        String token = authUtils.getAuthToken();

        Response response = given()
                .baseUri(endPoints.baseUrl)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"title\":\"Test News\", \"content\":\"This is a test news content\"}")
                .when()
                .post(endPoints.createPost)
                .then()
                .statusCode(201)
                .extract()
                .response();

        assertEquals(response.getStatusCode(), 201, "Статус код неверный");
        assertEquals(response.jsonPath().getBoolean("success"), true, "Флаг успеха неверный");
    }

    @Test(description = "Позитивный тест: Получение всех новостей")
    public void testGetAllPosts() {
        String token = authUtils.getAuthToken();

        Response response = given()
                .baseUri(endPoints.baseUrl)
                .header("Authorization", "Bearer " + token)
                .when()
                .get(endPoints.getPost)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertEquals(response.getStatusCode(), 200, "Статус код неверный");
        assertEquals(response.jsonPath().getBoolean("success"), true, "Флаг успеха неверный");
    }

    @Test(description = "Позитивный тест: Изменение новости")
    public void testUpdatePost() {
        String token = authUtils.getAuthToken();
        String postId = "1"; // замените на реальный ID новости

        Response response = given()
                .baseUri(endPoints.baseUrl)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"title\":\"Updated News\", \"content\":\"This is an updated news content\"}")
                .when()
                .patch(endPoints.updatePost.replace("{id}", postId))
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertEquals(response.getStatusCode(), 200, "Статус код неверный");
        assertEquals(response.jsonPath().getBoolean("success"), true, "Флаг успеха неверный");
    }

    @Test(description = "Позитивный тест: Удаление новости")
    public void testDeletePost() {
        String token = authUtils.getAuthToken();
        String postId = "1"; // замените на реальный ID новости

        Response response = given()
                .baseUri(endPoints.baseUrl)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(endPoints.deletePost.replace("{id}", postId))
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertEquals(response.getStatusCode(), 200, "Статус код неверный");
        assertEquals(response.jsonPath().getBoolean("success"), true, "Флаг успеха неверный");
    }
}*/
