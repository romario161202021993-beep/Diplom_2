package ru.yandex.praktikum.tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.praktikum.client.UserClient;
import ru.yandex.praktikum.model.User;
import ru.yandex.praktikum.model.UserGenerator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UserRegistrationTest {

    private UserClient userClient;
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Проверка успешной регистрации нового уникального пользователя")
    public void createUniqueUserSuccessTest() {
        user = UserGenerator.getRandomUser();

        Response response = userClient.register(user);

        // ОР: статус 200, success = true, возвращается accessToken и данные пользователя
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(user.getEmail().toLowerCase()))
                .body("user.name", equalTo(user.getName()));

        accessToken = response.jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    @Description("Проверка ошибки 403 при попытке повторной регистрации существующего пользователя")
    public void createDuplicateUserFailTest() {
        user = UserGenerator.getRandomUser();

        // Регистрируем пользователя первый раз
        Response firstResponse = userClient.register(user);
        accessToken = firstResponse.jsonPath().getString("accessToken");

        // Пытаемся зарегистрировать того же пользователя повторно
        Response secondResponse = userClient.register(user);

        // ОР: статус 403, success = false, message = "User already exists"
        secondResponse.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без заполнения поля email")
    @Description("Проверка ошибки 403 при отсутствии обязательного поля email")
    public void createUserWithoutEmailFailTest() {
        user = UserGenerator.getUserWithoutEmail();

        Response response = userClient.register(user);

        // ОР: статус 403, success = false, сообщение об обязательных полях
        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без заполнения поля password")
    @Description("Проверка ошибки 403 при отсутствии обязательного поля password")
    public void createUserWithoutPasswordFailTest() {
        user = UserGenerator.getUserWithoutPassword();

        Response response = userClient.register(user);

        // ОР: статус 403, success = false
        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без заполнения поля name")
    @Description("Проверка ошибки 403 при отсутствии обязательного поля name")
    public void createUserWithoutNameFailTest() {
        user = UserGenerator.getUserWithoutName();

        Response response = userClient.register(user);

        // ОР: статус 403, success = false
        response.then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        // Удаляем созданного пользователя, если он был успешно создан
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }
}
