package com;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class EchoPacket extends Packet {
    public static final String type = "ECHO";
    public String message;  // Используем message вместо text
    public int correspondentId;

    // Конструктор с двумя параметрами
    public EchoPacket(String message, int correspondentId) {
        this.message = message;  // Инициализация message
        this.correspondentId = correspondentId;
    }

    // Конструктор с одним параметром
    public EchoPacket(String message) {
        this.message = message;  // Инициализация message
    }

    // Пустой конструктор
    public EchoPacket() {}

    // Метод для получения текстового сообщения
    public String getText() {
        return message;  // Возвращаем message
    }

    // Метод для получения идентификатора корреспондента
    public int getCorrespondentId() {
        return correspondentId;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void writeBody(PrintWriter writer) throws Exception {
        writer.println(message);  // Записываем message вместо text
    }

    @Override
    public void readBody(BufferedReader reader) throws Exception {
        message = reader.readLine();  // Читаем message вместо text
    }
}
