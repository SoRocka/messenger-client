package com;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Correspondent {
    public final int id;
    public final String login;
    private final String password; // Добавляем поле для пароля

    public Session activeSession;

    public Correspondent(int id, String login, String password) {  // Изменяем конструктор
        this.id = id;
        this.login = login;
        this.password = password;
    }

    private static final Map<Integer, Correspondent> correspondentById = new HashMap<>();
    private static final Map<String, Correspondent> correspondentByLogin = new HashMap<>();

    public static void registerCorrespondent(Correspondent c) {
        correspondentById.put(c.id, c);
        correspondentByLogin.put(c.login, c);
    }

    public static Correspondent findCorrespondent(String login) {
        return correspondentByLogin.get(login);
    }

    public static Collection<Correspondent> listAll() {
        return correspondentById.values();
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}
