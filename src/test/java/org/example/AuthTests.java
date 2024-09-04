package org.example;

import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

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
    @Feature("Авторизация пользователя")
    @Description("Тест успешной авторизации пользователя с валидными данными.")
    public void givenValidCredentials_whenLogin_thenSuccess() {
        SoftAssert softAssert = new SoftAssert();

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.login)
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + testData.testUserEmail + "\",\"password\":\"" + testData.testUserPassword + "\"}")
                .when()
                .post();

        softAssert.assertEquals(response.statusCode(), 200, "Status code 200");
        softAssert.assertNotNull(response.jsonPath().getString("accessToken"), "Access Token is null");
        softAssert.assertNotNull(response.jsonPath().getString("user.id"), "User ID is null");

        softAssert.assertAll();
    }

    @Test
    @Feature("Получение информации о пользователе")
    @Description("Тест получения информации о пользователе с валидным токеном.")
    public void givenValidToken_whenGetUserInfo_thenUserInfoReturned() {
        SoftAssert softAssert = new SoftAssert();

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.whoAmI)
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .get();

        softAssert.assertEquals(response.statusCode(), 200, "Status code 200");
        softAssert.assertNotNull(response.jsonPath().getString("id"), "User ID is null");
        softAssert.assertNotNull(response.jsonPath().getString("email"), "User email is null");

        softAssert.assertAll();
    }

    @Test
    @Feature("Изменение пользователя")
    @Description("Тест изменения информации о пользователе с валидным токеном и данными.")
    public void givenValidData_whenUpdateUserInfo_thenUserInfoUpdated() {
        SoftAssert softAssert = new SoftAssert();
        String Name = "UpdatedFirstName";

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateUser.replace("{id}", testData.userId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("firstName", Name)
                .when()
                .patch();

        softAssert.assertEquals(response.statusCode(), 200, "Status code 200");
        softAssert.assertEquals(response.jsonPath().getString("firstName"), Name, "First name did not update correctly");

        softAssert.assertAll();
    }

    @Test
    @Feature("Создание поста")
    @Description("Тест создания нового поста с изображением и тегами.")
    public void givenValidData_whenCreatePost_thenPostCreated() {
        String title = "Title";
        String text = "text";
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
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(response.statusCode(), 201, "Status code 201");
        testData.postId = response.jsonPath().getString("id");
        softAssert.assertNotNull(testData.postId, "Post ID is null");
        softAssert.assertEquals(response.jsonPath().getString("title"), title, "Title mismatch");
        softAssert.assertEquals(response.jsonPath().getString("text"), text, "Text mismatch");

        softAssert.assertAll();
    }

    @Test
    @Feature("Обновление поста")
    @Description("Тест обновления существующего поста с новыми данными.")
    public void givenValidData_whenUpdatePost_thenPostUpdated() {
        givenValidData_whenCreatePost_thenPostCreated();
        SoftAssert softAssert = new SoftAssert();
        String Title = "Updated Test Post Title";
        String Text = "This is an updated test post text";
        String FilePath = "src/main/resources/s.png";
        String[] Tags = {"upTag1", "upTag2"};
        File updatedFile = new File(FilePath);
        RequestSpecification request = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updatePost.replace("{id}", testData.postId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("title", Title)
                .multiPart("text", Text)
                .multiPart("file", updatedFile, "image/png");
        for (String tag : Tags) {
            request.multiPart("tags", tag);
        }
        Response response = request.when().patch();
        softAssert.assertEquals(response.statusCode(), 200, "Status code 200 ");
        softAssert.assertEquals(response.jsonPath().getString("title"), Title, "Updated title mismatch");
        softAssert.assertEquals(response.jsonPath().getString("text"), Text, "Updated text mismatch");

        softAssert.assertAll();
    }
    @Test
    @Feature("Удаление поста")
    @Description("Тест удаления существующего поста.")
    public void givenValidPostId_whenDeletePost_thenPostDeleted() {
        givenValidData_whenCreatePost_thenPostCreated();
        SoftAssert softAssert = new SoftAssert();

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.deletePost.replace("{id}", testData.postId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .delete();

        softAssert.assertEquals(response.statusCode(), 200, "Status code 200 ");
        softAssert.assertAll();
    }

    @Test
    @Feature("Создание комментария")
    @Description("Тест создания комментария к посту.")
    public void givenValidData_whenCreateComment_thenCommentCreated() {
        givenValidData_whenCreatePost_thenPostCreated();
        SoftAssert softAssert = new SoftAssert();
        String comment = "comment";

        JSONObject requestBody = new JSONObject();
        requestBody.put("postId", testData.postId);
        requestBody.put("text", comment);

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.comment)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body(requestBody.toString())
                .when()
                .post();

        softAssert.assertEquals(response.statusCode(), 201, "Status code 201");
        testData.commentId = response.jsonPath().getString("id");
        softAssert.assertNotNull(testData.commentId, "Comment ID is null");
        softAssert.assertEquals(response.jsonPath().getString("text"), comment, "Comment text mismatch");

        softAssert.assertAll();
    }

    @Test
    @Feature("Обновление комментария")
    @Description("Тест обновления комментария к посту.")
    public void givenValidData_whenUpdateComment_thenCommentUpdated() {
        givenValidData_whenCreateComment_thenCommentCreated();
        SoftAssert softAssert = new SoftAssert();
        String updated = "Updated";

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateComment.replace("{id}", testData.commentId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body("{\"text\":\"" + updated + "\"}")
                .when()
                .patch();

        softAssert.assertEquals(response.statusCode(), 200, "Status code 200");
        softAssert.assertEquals(response.jsonPath().getString("text"), updated, "Updated comment text mismatch");

        softAssert.assertAll();
    }

    @Test
    @Feature("Удаление комментария")
    @Description("Тест удаления комментария к посту.")
    public void givenValidCommentId_whenDeleteComment_thenCommentDeleted() {
        givenValidData_whenCreateComment_thenCommentCreated();
        SoftAssert softAssert = new SoftAssert();

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.deleteComment.replace("{id}", testData.commentId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .delete();

        softAssert.assertEquals(response.statusCode(), 200, "Status code 200");
        softAssert.assertAll();
    }

    @Step("Login user and obtain access token")
    private void loginUser() {
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.login)
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + testData.testUserEmail + "\",\"password\":\"" + testData.testUserPassword + "\"}")
                .when()
                .post();

        Assert.assertEquals(response.statusCode(), 200, "User login failed");
        testData.accessToken = response.jsonPath().getString("accessToken");
        testData.userId = response.jsonPath().getString("user.id");
    }
}
