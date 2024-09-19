package com;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Session extends Thread {
    private final Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
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
                    p.writePacket(writer);
                } 
                catch (InterruptedException x) {
                    break;
                }
            }
        });

        writerThread.start();
    }

    public void send(Packet p) {
        toClientQueue.add(p);
    }

    public void run() {
        try {
            try (socket) {
                System.out.println("Got incoming connection");

                InputStream input = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                writer = new PrintWriter(output, true);

                // Авторизация при первом соединении
                if (!handleLogin()) {
                    close();
                    return;
                }

                // Основной цикл обработки пакетов после успешной авторизации
                for(;;) {
                    var p = Packet.readPacket(reader);

                    if (p == null || p.getType().equals(ByePacket.type)) {
                        close();
                        return;
                    }

                    var e = new Event(this, p);
                    Dispatcher.event(e);
                }
            }
        } catch (IOException ex) {
            System.out.println("Session problem: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Метод для обработки логина
    private boolean handleLogin() throws IOException {
        // Ожидание получения пакета с логином
        Packet loginPacket = Packet.readPacket(reader);

        if (loginPacket != null && loginPacket.getType().equals(LoginPacket.type)) {
            LoginPacket login = (LoginPacket) loginPacket;

            // Проверка логина и пароля
            Correspondent correspondent = Correspondent.findCorrespondent(login.getUsername());
            if (correspondent != null && correspondent.checkPassword(login.getPassword())) {
                this.correspondent = correspondent;
                correspondent.activeSession = this;

                // Отправляем ответ об успешной авторизации
                writer.println("LOGIN SUCCESS");
                return true;
            } else {
                writer.println("LOGIN FAILURE");
                return false;
            }
        }
        return false;
    }

    public void close() {
        try {
            if (correspondent != null) correspondent.activeSession = null;
            writerThread.interrupt();
            socket.close();
        } catch (Exception ex) {
            System.out.println("Session closing problem: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
