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

public class UserLoginTest {

    private UserClient userClient;
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();

        // Создаём пользователя для тестов авторизации
        Response registerResponse = userClient.register(user);
        accessToken = registerResponse.jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Успешная авторизация существующего пользователя")
    @Description("Проверка успешного входа с корректными email и password")
    public void loginExistingUserSuccessTest() {
        Response response = userClient.login(user);

        // ОР: статус 200, success = true, возвращается токен и данные пользователя
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(user.getEmail().toLowerCase()))
                .body("user.name", equalTo(user.getName()));
    }

    @Test
    @DisplayName("Вход с неверным email")
    @Description("Проверка ошибки 401 при авторизации с некорректным email")
    public void loginWithWrongEmailFailTest() {
        User wrongUser = new User("wrong_" + user.getEmail(), user.getPassword(), user.getName());

        Response response = userClient.login(wrongUser);

        // ОР: статус 401, success = false, message об ошибке
        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Проверка ошибки 401 при авторизации с некорректным паролем")
    public void loginWithWrongPasswordFailTest() {
        User wrongUser = new User(user.getEmail(), "wrongpassword", user.getName());

        Response response = userClient.login(wrongUser);

        // ОР: статус 401, success = false
        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        // Удаляем тестового пользователя
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }
}
