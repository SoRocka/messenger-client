package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MessengerClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessengerClientApplication.class, args);
        MessengerServer.startServer(); // Запуск вашего сервера
    }
}
