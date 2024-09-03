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
        String filePath = "src/main/resources/sc.png"; // Проверьте корректность пути
        String[] tags = {"tag1", "tag2"};

        // Проверка существования файла
        File file = new File(filePath);
        Assert.assertTrue(file.exists(), "Файл не существует по указанному пути: " + filePath);

        // Создаем запрос
        RequestSpecification request = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.createPost)
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("title", title)
                .multiPart("text", text)
                .multiPart("file", file, "image/png"); // Явно указываем MIME-тип файла

        // Отправляем каждый тег как отдельный multipart
        for (String tag : tags) {
            request.multiPart("tags", tag);
        }

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
    @Feature("Update Post")
    @Description("Verify user can update a post")
    public void testUpdatePost() {
        // Создаем пост перед обновлением
        testCreatePost(); // Убедимся, что пост существует

        // Параметры для обновления поста
        String updatedTitle = "Updated Test Post Title";
        String updatedText = "This is an updated test post text";
        String updatedFilePath = "src/main/resources/s.png"; // Проверьте корректность пути к новому файлу
        String[] updatedTags = {"updatedTag1", "updatedTag2"};

        // Проверка существования нового файла
        File updatedFile = new File(updatedFilePath);
        Assert.assertTrue(updatedFile.exists(), "Файл не существует по указанному пути: " + updatedFilePath);

        // Создаем запрос для обновления поста
        RequestSpecification request = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updatePost.replace("{id}", testData.postId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .multiPart("title", updatedTitle)
                .multiPart("text", updatedText)
                .multiPart("file", updatedFile, "image/png"); // Явно указываем MIME-тип файла

        // Отправляем каждый тег как отдельный multipart
        for (String tag : updatedTags) {
            request.multiPart("tags", tag);
        }

        // Выполняем запрос
        Response response = request.when().patch();

        // Выводим тело ответа для отладки
        System.out.println("Response Body: " + response.getBody().asString());

        // Проверяем статус код
        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200 for post update");

        // Проверяем, что пост был обновлен и возвращен в ответе
        Assert.assertEquals(response.jsonPath().getString("title"), updatedTitle, "Updated title mismatch");
        Assert.assertEquals(response.jsonPath().getString("text"), updatedText, "Updated text mismatch");

    }


    @Test
    @Feature("Delete Post")
    @Description("Verify user can delete a post")
    public void testDeletePost() {
        testCreatePost();
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.deletePost.replace("{id}", testData.postId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .delete();
        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200 for post deletion");

    }

    @Test
    @Feature("Create Comment")
    @Description("Verify user can add a comment to a post")
    public void testCreateComment() {
        // Создаем пост перед созданием комментария
        testCreatePost(); // Убедимся, что пост существует

        // Параметры для комментария
        String commentContent = "This is a test comment";

        // Создаем тело запроса в формате JSON
        JSONObject requestBody = new JSONObject();
        requestBody.put("postId", testData.postId);
        requestBody.put("text", commentContent);

        // Создаем запрос для добавления комментария
        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.comment)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body(requestBody.toString())
                .when()
                .post();

        // Выводим тело ответа для отладки
        System.out.println("Create Comment Response Body: " + response.getBody().asString());

        // Проверяем статус код
        Assert.assertEquals(response.statusCode(), 201, "Status code is not 201");

        // Проверяем, что комментарий был создан и возвращен в ответе
        testData.commentId = response.jsonPath().getString("id");
        Assert.assertNotNull(testData.commentId, "Comment ID is null");
        Assert.assertEquals(response.jsonPath().getString("text"), commentContent, "Comment content mismatch");
    }


    @Test
    @Feature("Update Comment")
    @Description("Verify user can update a comment")
    public void testUpdateComment() {
        testCreateComment(); // Убедимся, что комментарий существует
        String updatedContent = "This is an updated comment";

        // Проверяем, что commentId установлен правильно
        Assert.assertNotNull(testData.commentId, "Comment ID is null");

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.updateComment.replace("{id}", testData.commentId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + testData.accessToken)
                .body("{\"text\":\"" + updatedContent + "\"}")
                .when()
                .patch();

        // Выводим тело ответа для отладки
        System.out.println("Update Comment Response Body: " + response.getBody().asString());

        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");
        Assert.assertEquals(response.jsonPath().getString("text"), updatedContent, "Comment content mismatch");
    }


    @Test
    @Feature("Delete Comment")
    @Description("Verify user can delete a comment")
    public void testDeleteComment() {
        testCreateComment(); // Убедимся, что комментарий существует

        // Проверяем, что commentId установлен правильно
        Assert.assertNotNull(testData.commentId, "Comment ID is null");

        Response response = RestAssured.given()
                .baseUri(endPoints.baseUrl)
                .basePath(endPoints.deleteComment.replace("{id}", testData.commentId))
                .header("Authorization", "Bearer " + testData.accessToken)
                .when()
                .delete();

        // Выводим тело ответа для отладки
        System.out.println("Delete Comment Response Body: " + response.getBody().asString());

        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");
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
