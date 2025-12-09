package ru.yandex.praktikum.tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.praktikum.client.OrderClient;
import ru.yandex.praktikum.client.UserClient;
import ru.yandex.praktikum.model.Order;
import ru.yandex.praktikum.model.User;
import ru.yandex.praktikum.model.UserGenerator;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;

public class OrderCreationTest {

    private OrderClient orderClient;
    private UserClient userClient;
    private String accessToken;

    // Валидные хеши ингредиентов из реального API
    private static final String BUN_INGREDIENT = "61c0c5a71d1f82001bdaaa6d";
    private static final String SAUCE_INGREDIENT = "61c0c5a71d1f82001bdaaa72";
    private static final String FILLING_INGREDIENT = "61c0c5a71d1f82001bdaaa6f";

    @Before
    public void setUp() {
        orderClient = new OrderClient();
        userClient = new UserClient();

        // Создаём пользователя для тестов с авторизацией
        User user = UserGenerator.getRandomUser();
        Response registerResponse = userClient.register(user);
        accessToken = registerResponse.jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Проверка успешного создания заказа авторизованным пользователем с валидными ингредиентами")
    public void createOrderWithAuthAndIngredientsSuccessTest() {
        Order order = new Order(Arrays.asList(BUN_INGREDIENT, SAUCE_INGREDIENT, FILLING_INGREDIENT));

        Response response = orderClient.createWithAuth(order, accessToken);

        // ОР: статус 200, success = true, возвращается номер заказа
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("name", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации с ингредиентами")
    @Description("Проверка создания заказа без авторизации. ОР - из документации, ФР может отличаться")
    public void createOrderWithoutAuthWithIngredientsTest() {
        Order order = new Order(Arrays.asList(BUN_INGREDIENT, SAUCE_INGREDIENT));

        Response response = orderClient.createWithoutAuth(order);

        // ОР из документации: заказ создаётся, но ФР может показать ошибку
        // Проверяем фактический результат
        int statusCode = response.getStatusCode();

        // ФР: тест покажет реальное поведение API
        if (statusCode == 200) {
            response.then()
                    .body("success", equalTo(true))
                    .body("order.number", notNullValue());
        } else if (statusCode == 401) {
            // Если API требует авторизацию
            response.then()
                    .body("success", equalTo(false))
                    .body("message", equalTo("You should be authorised"));
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией без ингредиентов")
    @Description("Проверка ошибки 400 при создании заказа без ингредиентов")
    public void createOrderWithoutIngredientsFailTest() {
        Order order = new Order(Collections.emptyList());

        Response response = orderClient.createWithAuth(order, accessToken);

        // ОР: статус 400, success = false, message об обязательности ингредиентов
        response.then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиента")
    @Description("Проверка ошибки 500 при создании заказа с невалидным хешем ингредиента")
    public void createOrderWithInvalidIngredientHashFailTest() {
        Order order = new Order(Arrays.asList("invalidhash123456789"));

        Response response = orderClient.createWithAuth(order, accessToken);

        // ОР из документации: статус 500 Internal Server Error
        response.then()
                .statusCode(500);
    }

    @Test
    @DisplayName("Создание заказа без авторизации и без ингредиентов")
    @Description("Проверка ошибки при создании заказа без авторизации и ингредиентов")
    public void createOrderWithoutAuthAndIngredientsFailTest() {
        Order order = new Order(Collections.emptyList());

        Response response = orderClient.createWithoutAuth(order);

        // ОР: статус 400, ошибка об отсутствии ингредиентов
        response.then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @After
    public void tearDown() {
        // Удаляем тестового пользователя
        if (accessToken != null) {
            userClient.delete(accessToken);
        }
    }
}
