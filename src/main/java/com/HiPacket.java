package com;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;

public class HiPacket extends Packet implements Serializable {
    private static final long serialVersionUID = 1L;  // Уникальный идентификатор версии класса

    public static final String type = "HI";

    public String login;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void writeBody(PrintWriter writer) throws Exception {
        writer.println(login);
    }

    @Override
    public void readBody(BufferedReader reader) throws Exception {
        login = reader.readLine();
    }
}
