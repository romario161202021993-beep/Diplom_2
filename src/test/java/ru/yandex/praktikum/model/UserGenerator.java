package ru.yandex.praktikum.model;

public class UserGenerator {

    public static User getRandomUser() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String email = "test_user_" + timestamp + "@yandex.ru";
        String password = "password123";
        String name = "TestUser" + timestamp;

        return new User(email, password, name);
    }

    public static User getUserWithoutEmail() {
        return new User(null, "password123", "TestUser");
    }

    public static User getUserWithoutPassword() {
        return new User("test@yandex.ru", null, "TestUser");
    }

    public static User getUserWithoutName() {
        return new User("test@yandex.ru", "password123", null);
    }
}
