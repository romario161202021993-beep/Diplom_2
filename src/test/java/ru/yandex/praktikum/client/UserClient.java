package ru.yandex.praktikum.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ru.yandex.praktikum.model.User;

import static io.restassured.RestAssured.given;

public class UserClient extends BaseClient {
    private static final String REGISTER_PATH = "/auth/register";
    private static final String LOGIN_PATH = "/auth/login";
    private static final String USER_PATH = "/auth/user";

    @Step("Регистрация нового пользователя: {user.email}")
    public Response register(User user) {
        return given()
                .spec(getBaseSpec())
                .body(user)
                .when()
                .post(REGISTER_PATH);
    }

    @Step("Авторизация пользователя: {user.email}")
    public Response login(User user) {
        return given()
                .spec(getBaseSpec())
                .body(user)
                .when()
                .post(LOGIN_PATH);
    }

    @Step("Удаление пользователя с токеном")
    public Response delete(String accessToken) {
        return given()
                .spec(getBaseSpec())
                .header("Authorization", accessToken)
                .when()
                .delete(USER_PATH);
    }
}
