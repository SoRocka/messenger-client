package com;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;

public class MessagePacket extends Packet implements Serializable {
    public static final String type = "MESSAGE";
    private int correspondentId;
    private String message;

    // Конструктор с параметрами
    public MessagePacket(int correspondentId, String message) {
        this.correspondentId = correspondentId;
        this.message = message;
    }

    // Метод для получения сообщения
    public String getMessage() {
        return message;
    }

    // Метод для получения идентификатора корреспондента
    public int getCorrespondentId() {
        return correspondentId;
    }

    // Метод для установки идентификатора корреспондента
    public void setCorrespondentId(int correspondentId) {
        this.correspondentId = correspondentId;
    }

    // Метод для установки сообщения
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getType() {
        return type;
    }

    // Запись тела пакета в поток
    @Override
    public void writeBody(PrintWriter writer) throws Exception {
        writer.println(correspondentId);
        writer.println(message);
        writer.println();  // Завершаем сообщение пустой строкой
    }

    // Чтение тела пакета из потока
    @Override
    public void readBody(BufferedReader reader) throws Exception {
        correspondentId = Integer.parseInt(reader.readLine());  // Чтение идентификатора корреспондента
        message = reader.readLine();  // Чтение сообщения
    }
}
