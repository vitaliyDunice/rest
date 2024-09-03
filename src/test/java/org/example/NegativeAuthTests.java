package org.example;

import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

@Epic("Негативное тестирование")
public class NegativeAuthTests {

    private EndPoints endPoints;
    private TestData testData;

    @BeforeMethod
    public void setup() {
        endPoints = new EndPoints();
        testData = new TestData();
        testData.testUserEmail = "5test.user@example.com";
        testData.testUserPassword = "password123";
    }

    @Test
    @Feature("Авторизация пользователя")
    public void testUserLoginFailureWithIncorrectCredentials() {
        String incorrectEmail = "incorrect.email@example.com";
        String incorrectPassword = "wrongpassword";

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.login)
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + incorrectEmail + "\",\"password\":\"" + incorrectPassword + "\"}")
                .when()
                .post();
        Assert.assertEquals(response.statusCode(), 401, "Status code 401");
    }
    @Test
    @Feature("Получение информации о пользователе")
    public void testGetUserInfoWithoutAuthorization() {
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.whoAmI)
                .when()
                .get();
        Assert.assertEquals(response.statusCode(), 401, "Status code 401");
    }

    @Test
    @Feature("Обновление информации о пользователе")
    public void testUpdateUserInfoWithInvalidToken() {
        String newFirstName = "UpdatedFirstName";
        String invalidToken = "invalidToken123";
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateUser.replace("{id}", "validUserId"))
                .header("Authorization", "Bearer " + invalidToken)
                .multiPart("firstName", newFirstName)
                .when()
                .patch();
        Assert.assertEquals(response.statusCode(), 401, "Status code 401");
    }
    @Test
    @Feature("Создание поста")
    public void testCreatePostWithMissingFields() {
        File file = new File("src/main/resources/sc.png");
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.createPost)
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("file", file, "image/png")
                .when()
                .post();
        Assert.assertEquals(response.statusCode(), 401, "Status code 401");
    }
    @Test
    @Feature("Обновление поста")
    public void testUpdatePostWithNonExistentId() {
        String updatedTitle = "Updated Test Post Title";
        String updatedText = "This is an updated test post text";
        String nonExistentPostId = "nonExistentPostId";
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updatePost.replace("{id}", nonExistentPostId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("title", updatedTitle)
                .multiPart("text", updatedText)
                .when()
                .patch();
        Assert.assertEquals(response.statusCode(), 401, "Status code 401");
    }
    @Test
    @Feature("Удаление поста")
    public void testDeletePostWithNonExistentId() {
        String nonExistentPostId = "nonExistentPostId";
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.deletePost.replace("{id}", nonExistentPostId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .delete();
        Assert.assertEquals(response.statusCode(), 401, "Status code 401");
    }
    @Test
    @Feature("Создание комментария")
    public void testCreateCommentWithMissingPostId() {
        String commentContent = "comment";
        JSONObject requestBody = new JSONObject();
        requestBody.put("text", commentContent);
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.comment)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body(requestBody.toString())
                .when()
                .post();
        Assert.assertEquals(response.statusCode(), 401, "Status code 401");
    }
    @Test
    @Feature("Обновление комментария")
    public void testUpdateCommentWithNonExistentId() {
        String updatedContent = "Updated comment";
        String nonExistentCommentId = "nonExistentCommentId";
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateComment.replace("{id}", nonExistentCommentId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body("{\"text\":\"" + updatedContent + "\"}")
                .when()
                .patch();
        Assert.assertEquals(response.statusCode(), 401, "Status code 401 ");
    }
    @Test
    @Feature("Удаление комментария")
    public void testDeleteCommentWithNonExistentId() {
        String nonExistentCommentId = "nonExistentCommentId";
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.deleteComment.replace("{id}", nonExistentCommentId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .delete();
        Assert.assertEquals(response.statusCode(), 401, "Status code 401");
    }
}
