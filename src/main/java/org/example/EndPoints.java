package org.example;

public class EndPoints {
    public String baseUrl = "https://api.news.academy.dunice.net";
    public String signup = "/auth/signup";//регистрация post
    public String login = "/auth/login";//авторизация post
    public String whoAmI = "/auth/whoami";//проверить токен и полцчить пользователя get
    public String userInfo =  "/users/{id}"; //получить информацию о пользователи get
    public String updateUser =  "/users/{id}"; //изменить данные пользователя patch
    public String userList =  "/users"; //получить список пользователей get
    public String createPost =  "/posts"; //создать новость  post
    public String getPost =  "/posts"; //получить список новостей get
    public String updatePost =  "/posts/{id}"; //обновить определенную новость patch
    public String deletePost = "/posts/{id}"; //удалить новость delete
    public String comment = "/comments";//добавить коментарий post
    public String updateComment = "/comments/{id}";//изменить коментарий patch
    public String deleteComment = "/comments/{id}";//удалить коментарий delete
}
