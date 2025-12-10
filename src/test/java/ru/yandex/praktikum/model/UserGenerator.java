package ru.yandex.praktikum.model;

import com.github.javafaker.Faker;

public class UserGenerator {

    private static final Faker faker = new Faker();

    public static User getRandomUser() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 16);
        String name = faker.name().firstName();

        return new User(email, password, name);
    }

    public static User getUserWithoutEmail() {
        return new User(null, faker.internet().password(8, 16), faker.name().firstName());
    }

    public static User getUserWithoutPassword() {
        return new User(faker.internet().emailAddress(), null, faker.name().firstName());
    }

    public static User getUserWithoutName() {
        return new User(faker.internet().emailAddress(), faker.internet().password(8, 16), null);
    }
}
