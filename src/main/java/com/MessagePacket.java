package com;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class MessagePacket extends Packet {
    public static final String type = "MESSAGE";
    private int correspondentId;
    private String message;

    // Конструктор с двумя параметрами
    public MessagePacket(int correspondentId, String message) {
        this.correspondentId = correspondentId;
        this.message = message;
    }

    // Конструктор по умолчанию для создания пакета через Supplier
    public MessagePacket() {
        // Параметры можно оставить неинициализированными, если это необходимо для создания по умолчанию
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void writeBody(PrintWriter writer) throws Exception {
        writer.println(correspondentId);
        writer.println(message);
        writer.println();  // Это завершает сообщение пустой строкой
    }

    @Override
    public void readBody(BufferedReader reader) throws Exception {
        correspondentId = Integer.parseInt(reader.readLine());  // Чтение идентификатора корреспондента
        message = reader.readLine();  // Чтение самого сообщения
    }

    public int getCorrespondentId() {
        return correspondentId;
    }

    public String getMessage() {
        return message;
    }

    public void setCorrespondentId(int correspondentId) {
        this.correspondentId = correspondentId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
