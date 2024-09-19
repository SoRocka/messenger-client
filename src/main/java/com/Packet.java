package com;

import java.io.*;
import java.util.*;
import java.util.function.*;

public abstract class Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    // Карта для сопоставления типов пакетов с их конструкторами
    private static Map<String, Supplier<Packet>> typeMap = Map.of(
        EchoPacket.type, EchoPacket::new,
        HiPacket.type, HiPacket::new,
        ByePacket.type, ByePacket::new,
        MessagePacket.type, MessagePacket::new,
        ListPacket.type, ListPacket::new,
        LoginPacket.type, LoginPacket::new
    );

    // Получение типа пакета
    public abstract String getType();

    // Сериализация пакета через ObjectOutputStream
    public void writePacket(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(this);  // Запись объекта в поток
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error writing packet", e);
        }
    }

    // Чтение пакета через ObjectInputStream
    public static Packet readPacket(ObjectInputStream inputStream) {
        try {
            return (Packet) inputStream.readObject();  // Чтение объекта из потока
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error reading packet", e);
        }
    }

    // Этот метод больше не нужен, так как используется объектная передача пакетов
    @Deprecated
    public void writePacket(PrintWriter writer) {
        try {
            writer.println(getType());
            writeBody(writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Этот метод также больше не нужен, но может остаться для совместимости
    @Deprecated
    public static Packet readPacket(BufferedReader reader) {
        try {
            var type = reader.readLine();
            if (type == null) type = "";
            var packetSupplier = typeMap.get(type);
            if (packetSupplier == null) {
                System.out.println("Unrecognized message type '" + type + "'");
                return null;
            }

            var packet = packetSupplier.get();
            packet.readBody(reader);
            return packet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Абстрактные методы для наследников пакетов
    public abstract void writeBody(PrintWriter writer) throws Exception;

    public abstract void readBody(BufferedReader reader) throws Exception;

    // Метод для чтения текстовых данных (оставляем для совместимости)
    public String readText(BufferedReader reader) throws Exception {
        StringBuilder text = new StringBuilder();
        for (;;) {
            var s = reader.readLine();
            if (s.isEmpty()) break;

            if (text.length() > 0) text.append("\n");
            text.append(s);
        }
        return text.toString();
    }
}
