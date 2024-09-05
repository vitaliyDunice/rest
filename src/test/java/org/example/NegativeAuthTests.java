package org.example;

import io.qameta.allure.Step;
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
import org.testng.asserts.SoftAssert;

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
        loginUser();
    }

    @Test
    @Feature("Авторизация пользователя")
    @Description("Проверка авторизации с некорректными учетными данными.")
    public void givenIncorrectCredentials_whenLogin_thenUnauthorized() {
        SoftAssert softAssert = new SoftAssert();
        String Email = "t.email@example.com";
        String Pass = "pass";

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.login)
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + Email + "\",\"password\":\"" + Pass + "\"}")
                .when()
                .post();

        softAssert.assertEquals(response.statusCode(), 401, "Status code 401");
        softAssert.assertAll();
    }

    @Test
    @Feature("Получение информации о пользователе")
    @Description("Проверка получения информации о пользователе без авторизации.")
    public void givenNoAuthorization_whenGetUserInfo_thenUnauthorized() {
        SoftAssert softAssert = new SoftAssert();

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.whoAmI)
                .when()
                .get();

        softAssert.assertEquals(response.statusCode(), 401, "Status code 401 ");
        softAssert.assertAll();
    }

    @Test
    @Feature("Обновление информации о пользователе")
    @Description("Проверка обновления информации о пользователе с невалидным токеном.")
    public void givenInvalidToken_whenUpdateUserInfo_thenUnauthorized() {
        SoftAssert softAssert = new SoftAssert();
        String news = "Updat";
        String Token = "Token123";

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateUser.replace("{id}", "validUserId"))
                .header("Authorization", "Bearer " + Token)
                .multiPart("firstName", news)
                .when()
                .patch();

        softAssert.assertEquals(response.statusCode(), 401, "Status code 401");
        softAssert.assertAll();
    }

    @Test
    @Feature("Создание поста")
    @Description("Проверка создания поста с отсутствующими обязательными полями.")
    public void givenMissingFields_whenCreatePost_thenUnauthorized() {
        String title = "Title";
        String text = "text";
        String filePath = "src/main/resources/sc.png";
        File file = new File(filePath);
        RequestSpecification request = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.createPost)
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("title", title)
                .multiPart("text", text)
                .multiPart("file", file, "image/png");
        Response response = request.when().post();
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(response.statusCode(), 400, "Status code 400");
        softAssert.assertAll();
    }

    @Test
    @Feature("Обновление поста")
    @Description("Проверка обновления поста с несуществующим идентификатором.")
    public void givenNonExistentPostId_whenUpdatePost_thenUnauthorized() {
        SoftAssert softAssert = new SoftAssert();
        String Title = "Title";
        String Text = "text";
        String nonPost = "nonPost";

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath("/posts/5")
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("title", Title)
                .multiPart("text", Text)
                .when()
                .patch();

        softAssert.assertEquals(response.statusCode(), 404, "Status code 404");
        softAssert.assertAll();
    }

    @Test
    @Feature("Удаление поста")
    @Description("Проверка удаления поста с несуществующим идентификатором.")
    public void givenNonExistentPostId_whenDeletePost_thenUnauthorized() {
        SoftAssert softAssert = new SoftAssert();
        String ExPost = "ExPost";

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath("/posts/5")
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .delete();

        softAssert.assertEquals(response.statusCode(), 404, "Status code 404");
        softAssert.assertAll();
    }

    @Test
    @Feature("Создание комментария")
    @Description("Проверка создания комментария без идентификатора поста.")
    public void givenMissingPostId_whenCreateComment_thenUnauthorized() {
        SoftAssert softAssert = new SoftAssert();
        String commen = "comment";
        JSONObject requestBody = new JSONObject();
        requestBody.put("text", commen);

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.comment)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body(requestBody.toString())
                .when()
                .post();

        softAssert.assertEquals(response.statusCode(), 400, "Status code 400");
        softAssert.assertAll();
    }

    @Test
    @Feature("Обновление комментария")
    @Description("Проверка обновления комментария с несуществующим идентификатором.")
    public void givenNonExistentCommentId_whenUpdateComment_thenUnauthorized() {
        SoftAssert softAssert = new SoftAssert();
        String update = "Update";
        String nonExis = "nonExis";

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateComment.replace("{id}", nonExis))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body("{\"text\":\"" + update + "\"}")
                .when()
                .patch();

        softAssert.assertEquals(response.statusCode(), 400, "Status code 400");
        softAssert.assertAll();
    }

    @Test
    @Feature("Удаление комментария")
    @Description("Проверка удаления комментария с несуществующим идентификатором.")
    public void givenNonExistentCommentId_whenDeleteComment_thenUnauthorized() {
        SoftAssert softAssert = new SoftAssert();
        String Com = "Com";
        String end = "sjdfbsdjkf";
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.deleteComment.replace("{id}", Com))
                .header("Authorization", "Bearer " + end)
                .when()
                .delete();

        softAssert.assertEquals(response.statusCode(), 401, "Status code 401");
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