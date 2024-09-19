package com;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;

public class EchoPacket extends Packet implements Serializable {
    private static final long serialVersionUID = 1L;  // Добавляем уникальный идентификатор версии класса
    
    public static final String type = "ECHO";
    public String text;

    public EchoPacket(String text) {
        this.text = text;
    }

    public EchoPacket() {
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void writeBody(PrintWriter writer) throws Exception {
        writer.println(text);
        writer.println();
    }

    @Override
    public void readBody(BufferedReader reader) throws Exception {
        text = readText(reader);
    }
}
