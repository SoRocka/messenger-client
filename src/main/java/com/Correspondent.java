package com;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Correspondent {
    public final int id;
    public final String login;
    private final String password;
    public Session activeSession;

    private static final Map<Integer, Correspondent> correspondentById = new HashMap<>();
    private static final Map<String, Correspondent> correspondentByLogin = new HashMap<>();

    public Correspondent(int id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    public static void registerCorrespondent(Correspondent c) {
        correspondentById.put(c.id, c);
        correspondentByLogin.put(c.login, c);
    }

    // Метод для поиска корреспондента по login
    public static Correspondent findCorrespondent(String login) {
        return correspondentByLogin.get(login);
    }

    // Новый метод для поиска корреспондента по id
    public static Correspondent findCorrespondentById(int id) {
        return correspondentById.get(id);
    }

    public static Collection<Correspondent> listAll() {
        return correspondentById.values();
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public int getId() {
        return id;
    }
}
