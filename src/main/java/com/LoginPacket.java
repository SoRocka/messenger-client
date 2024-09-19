package com;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;  // Добавляем Serializable

public class LoginPacket extends Packet implements Serializable {  // Реализуем Serializable
    public static final String type = "LOGIN";

    private String username;
    private String password;

    public LoginPacket() {
    }

    public LoginPacket(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void writeBody(PrintWriter writer) throws Exception {
        writer.println(username);
        writer.println(password);
    }

    @Override
    public void readBody(BufferedReader reader) throws Exception {
        username = reader.readLine();
        password = reader.readLine();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
