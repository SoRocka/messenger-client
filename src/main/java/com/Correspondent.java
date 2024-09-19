package com;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Correspondent {
    public final int id;
    public final String login;

    public Session activeSession;

    public Correspondent(int id, String login) {
        this.id = id;
        this.login = login;
    }

    private static final Map<Integer, Correspondent> correspondentById = new HashMap<>();
    private static final Map<String, Correspondent> correspondentByLogin = new HashMap<>();

    public static void registerCorrespondent(Correspondent c) {
        correspondentById.put(c.id, c);
        correspondentByLogin.put(c.login, c);
    }

    public static Correspondent findCorrespondent(int id) {
        return correspondentById.get(id);
    }

    public static Correspondent findCorrespondent(String login) {
        return correspondentByLogin.get(login);
    }

    public static Collection<Correspondent> listAll() {
        return correspondentById.values();
    }
}