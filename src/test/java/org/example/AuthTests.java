package org.example;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

@Epic("User and News Management")
public class AuthTests {

    private EndPoints endPoints;
    private TestData testData;

    @BeforeMethod
    public void setup() {
        endPoints = new EndPoints();
        testData = new TestData();
        testData.testUserEmail = "5test.user@example.com";
        testData.testUserPassword = "password123";
        loginUser();  // Авторизация перед каждым тестом
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

        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");
        Assert.assertNotNull(response.jsonPath().getString("accessToken"), "Access token is null");
        testData.accessToken = response.jsonPath().getString("accessToken");
    }

    @Test
    @Feature("Get User Information")
    @Description("Verify user can get their own information")
    public void testGetUserInfo() {
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.whoAmI)
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .get();

        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");
        Assert.assertEquals(response.jsonPath().getString("id"), testData.userId, "User ID mismatch");
        Assert.assertEquals(response.jsonPath().getString("email"), testData.testUserEmail, "Email mismatch");
    }

    @Test
    @Feature("Update User Information")
    @Description("Verify user can update their own information")
    public void testUpdateUserInfo() {
        String newFirstName = "UpdatedFirstName";
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateUser.replace("{id}", testData.userId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("firstName", newFirstName) // Обновляем только поле firstName
                .when()
                .patch();

        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");
    }

    @Test
    @Feature("Create Post")
    @Description("Verify user can create a post with all required fields")
    public void testCreatePost() {
        // Параметры для поста
        String title = "Test Post Title";
        String text = "This is a test post text";
        String filePath = "C:/Users/Виталий/Desktop/sc.png"; // Путь к вашему файлу
        String[] tags = {"tag1", "tag2"};

        // Создаем запрос
        RequestSpecification request = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.createPost)
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("file", new File(filePath)) // Отправляем файл
                .multiPart("title", title)
                .multiPart("text", text)
                .multiPart("tags", tags); // Отправляем теги как массив

        // Выполняем запрос
        Response response = request.when().post();

        // Выводим тело ответа для отладки
        System.out.println("Response Body: " + response.getBody().asString());

        // Проверяем статус код
        Assert.assertEquals(response.statusCode(), 201, "Status code is not 201");

        // Проверяем, что пост был создан и возвращен в ответе
        testData.postId = response.jsonPath().getString("id");
        Assert.assertNotNull(testData.postId, "Post ID is null");
    }




    @Test
    @Feature("Create Comment")
    @Description("Verify user can add a comment to a post")
    public void testCreateComment() {
        // Создаем пост перед созданием комментария
        testCreatePost(); // Убедимся, что пост существует

        String commentContent = "This is a test comment";

        // Создаем комментарий
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.comment)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body("{\"postId\":\"" + testData.postId + "\",\"content\":\"" + commentContent + "\"}")
                .when()
                .post();

        // Проверяем статус код
        Assert.assertEquals(response.statusCode(), 201, "Status code is not 201");

        // Проверяем, что комментарий был создан и возвращен в ответе
        testData.commentId = response.jsonPath().getString("id");
        Assert.assertNotNull(testData.commentId, "Comment ID is null");
    }

    @Test
    @Feature("Update Comment")
    @Description("Verify user can update a comment")
    public void testUpdateComment() {
        testCreateComment(); // Убедимся, что комментарий существует
        String updatedContent = "This is an updated comment";
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateComment.replace("{id}", testData.commentId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body("{\"content\":\"" + updatedContent + "\"}")
                .when()
                .patch();

        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");
        Assert.assertEquals(response.jsonPath().getString("content"), updatedContent, "Comment content mismatch");
    }

    @Test
    @Feature("Delete Comment")
    @Description("Verify user can delete a comment")
    public void testDeleteComment() {
        testCreateComment(); // Убедимся, что комментарий существует
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.deleteComment.replace("{id}", testData.commentId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .delete();

        Assert.assertEquals(response.statusCode(), 204, "Status code is not 204");
    }

    @AfterMethod
    public void tearDown() {
        // Очистка созданных данных, если это необходимо
        if (testData.postId != null) {
            RestAssured.given()
                    .baseUri(endPoints.baseUrl)
                    .basePath(endPoints.deletePost.replace("{id}", testData.postId))
                    .header("Authorization", "Bearer " + testData.accessToken)
                    .when()
                    .delete();
            testData.postId = null;
        }
        // Удаление пользователя в данном случае не нужно
    }

    private void loginUser() {
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.login)
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + testData.testUserEmail + "\",\"password\":\"" + testData.testUserPassword + "\"}")
                .when()
                .post();

        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");
        testData.accessToken = response.jsonPath().getString("accessToken");
        testData.userId = response.jsonPath().getString("user.id");
    }
}

