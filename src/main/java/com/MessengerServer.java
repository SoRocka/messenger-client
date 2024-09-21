package com;

import java.net.*;

public class MessengerServer {

    public static void startServer() {
        // Регистрация пользователей с паролями
        Correspondent.registerCorrespondent(new Correspondent(1, "Полиночка", "<3"));
        Correspondent.registerCorrespondent(new Correspondent(2, "Призрачный гонщик", ""));
        Correspondent.registerCorrespondent(new Correspondent(3, "Citadel", "3798"));
        Correspondent.registerCorrespondent(new Correspondent(4, "user1", "password1"));
        Correspondent.registerCorrespondent(new Correspondent(5, "Активный пользователь", "123456"));
        Correspondent.registerCorrespondent(new Correspondent(6, "Кореш", ""));

        try (ServerSocket serverSocket = new ServerSocket(10001)) {
            new Thread(new Dispatcher()).start();

            System.out.println("Waiting for incoming connection");

            while (true) {
                Socket socket = serverSocket.accept();
                new Session(socket).start();
            }

        } catch (Exception ex) {
            System.out.println("Problem when starting server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
