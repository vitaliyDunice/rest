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

@Epic("Управление пользователями и новостями")
public class AuthTests {

    private EndPoints endPoints;
    private TestData testData;

    @BeforeMethod
    public void setup() {
        endPoints = new EndPoints();
        testData = new TestData();
        testData.testUserEmail = "5test.user@example.com";
        testData.testUserPassword = "password123";
        loginUser();
    }
    @Test
    @Feature("User Login")
    @Description("Verify user can successfully login with correct credentials")
    public void testUserLoginSuccess() {
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.login)
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + testData.testUserEmail + "\",\"password\":\"" + testData.testUserPassword + "\"}")
                .when()
                .post();
        Assert.assertEquals(response.statusCode(), 200, "Status code 200");
    }
    @Test
    @Feature("Авторизация пользователя")
    public void testGetUserInfo() {
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.whoAmI)
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .get();
        Assert.assertEquals(response.statusCode(), 200, "Status code 200");
    }
    @Test
    @Feature("Получение информации о пользователе")
    public void testUpdateUserInfo() {
        String newFirstName = "UpdatedFirstName";
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateUser.replace("{id}", testData.userId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("firstName", newFirstName)
                .when()
                .patch();

        Assert.assertEquals(response.statusCode(), 200, "Status code 200");
    }
    @Test
    @Feature("Создание поста")
    public void testCreatePost() {
        String title = "Test Post Title";
        String text = "This is a test post text";
        String filePath = "src/main/resources/sc.png";
        String[] tags = {"tag1", "tag2"};
        File file = new File(filePath);
        RequestSpecification request = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.createPost)
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("title", title)
                .multiPart("text", text)
                .multiPart("file", file, "image/png");
        for (String tag : tags) {
            request.multiPart("tags", tag);
        }
        Response response = request.when().post();
        Assert.assertEquals(response.statusCode(), 201, "Status code 201");
        testData.postId = response.jsonPath().getString("id");
        Assert.assertNotNull(testData.postId, "Post ID is null");
    }
    @Test
    @Feature("Обновление поста")
    public void testUpdatePost() {
        testCreatePost();
        String updatedTitle = "Updated Test Post Title";
        String updatedText = "This is an updated test post text";
        String updatedFilePath = "src/main/resources/s.png";
        String[] updatedTags = {"upTag1", "upTag2"};
        File updatedFile = new File(updatedFilePath);
        RequestSpecification request = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updatePost.replace("{id}", testData.postId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("title", updatedTitle)
                .multiPart("text", updatedText)
                .multiPart("file", updatedFile, "image/png");
        for (String tag : updatedTags) {
            request.multiPart("tags", tag);
        }
        Response response = request.when().patch();
        Assert.assertEquals(response.statusCode(), 200, "Status code 200 ");
    }
    @Test
    @Feature("Удаление поста")
    public void testDeletePost() {
        testCreatePost();
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.deletePost.replace("{id}", testData.postId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .delete();
        Assert.assertEquals(response.statusCode(), 200, "Status code 200 ");
    }
    @Test
    @Feature("Создание комментария")
    public void testCreateComment() {
        testCreatePost();
        String commentContent = "comment";
        JSONObject requestBody = new JSONObject();
        requestBody.put("postId", testData.postId);
        requestBody.put("text", commentContent);
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.comment)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body(requestBody.toString())
                .when()
                .post();
        Assert.assertEquals(response.statusCode(), 201, "Status code 201");
        testData.commentId = response.jsonPath().getString("id");
        Assert.assertNotNull(testData.commentId, "Comment ID is null");
        Assert.assertEquals(response.jsonPath().getString("text"), commentContent, "Comment");
    }
    @Test
    @Feature("Обновление комментария")
    public void testUpdateComment() {
        testCreateComment();
        String updatedContent = "Acomment";
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateComment.replace("{id}", testData.commentId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body("{\"text\":\"" + updatedContent + "\"}")
                .when()
                .patch();
        Assert.assertEquals(response.statusCode(), 200, "Status code 200");
        Assert.assertEquals(response.jsonPath().getString("text"), updatedContent, "AComment");
    }
    @Test
    @Feature("Удаление комментария")
    public void testDeleteComment() {
        testCreateComment();
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.deleteComment.replace("{id}", testData.commentId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .delete();
        Assert.assertEquals(response.statusCode(), 200, "Status code 200");
    }
    private void loginUser() {
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.login)
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + testData.testUserEmail + "\",\"password\":\"" + testData.testUserPassword + "\"}")
                .when()
                .post();
        Assert.assertEquals(response.statusCode(), 200, "Status code 200");
        testData.accessToken = response.jsonPath().getString("accessToken");
        testData.userId = response.jsonPath().getString("user.id");
    }
}