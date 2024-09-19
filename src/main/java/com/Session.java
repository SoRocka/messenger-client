package com;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Session extends Thread {
    private final Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final LinkedBlockingQueue<Packet> toClientQueue = new LinkedBlockingQueue<>();
    private Thread writerThread;

    public Correspondent correspondent;

    public Session(Socket socket) {
        this.socket = socket;

        writerThread = new Thread(() -> {
            for(;;) {
                try {
                    var p = toClientQueue.take();
                    System.out.println("Sending message: " + p.getType());
                    sendPacket(p);  // Отправляем пакет через ObjectOutputStream
                } 
                catch (InterruptedException x) {
                    break;
                }
            }
        });

        writerThread.start();
    }

    // Отправляем пакет через ObjectOutputStream
    private void sendPacket(Packet packet) {
        try {
            objectOutputStream.writeObject(packet);  // Отправляем пакет как объект
            objectOutputStream.flush();
        } catch (IOException e) {
            System.out.println("Error sending packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void send(Packet p) {
        toClientQueue.add(p);
    }

    public void run() {
        try {
            try (socket) {
                System.out.println("Got incoming connection");
    
                // Создаем потоки для объектов
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());
    
                // Авторизация при первом соединении
                if (!handleLogin()) {
                    close();
                    return;
                }
    
                // Основной цикл обработки пакетов после успешной авторизации
                while (!socket.isClosed()) {  // Убедитесь, что цикл продолжается, пока сокет не закрыт
                    try {
                        Packet p = (Packet) objectInputStream.readObject();  // Читаем объект пакета
    
                        if (p == null || p.getType().equals(ByePacket.type)) {
                            System.out.println("Received null or ByePacket, closing session");
                            close();
                            return;
                        }
    
                        var e = new Event(this, p);
                        Dispatcher.event(e);  // Отправляем пакет на обработку
                    } catch (EOFException e) {
                        System.out.println("Client closed connection.");
                        close();
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Session problem: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // Метод для обработки логина
    private boolean handleLogin() throws IOException, ClassNotFoundException {
        // Ожидание получения пакета с логином
        Packet loginPacket = (Packet) objectInputStream.readObject();  // Читаем объект
    
        if (loginPacket != null && loginPacket.getType().equals(LoginPacket.type)) {
            LoginPacket login = (LoginPacket) loginPacket;
    
            // Проверка логина и пароля
            Correspondent correspondent = Correspondent.findCorrespondent(login.getUsername());
            if (correspondent != null && correspondent.checkPassword(login.getPassword())) {
                this.correspondent = correspondent;
                correspondent.activeSession = this;
    
                // Отправляем ответ об успешной авторизации с correspondentId
                System.out.println("Login successful for user: " + login.getUsername());
                sendPacket(new EchoPacket("Login successful!", correspondent.getId()));  // Эта строка добавлена
    
                return true;
            } else {
                System.out.println("Login failed for user: " + login.getUsername());
                sendPacket(new EchoPacket("Login failed!"));
                return false;
            }
        }
        return false;
    }
    
    public void close() {
        try {
            System.out.println("Closing session for " + correspondent);
            if (correspondent != null) correspondent.activeSession = null;
            writerThread.interrupt();
            socket.close();
        } catch (Exception ex) {
            System.out.println("Session closing problem: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
