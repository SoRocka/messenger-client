package com;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ChatWindow extends JFrame {

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private String username;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int correspondentId; // идентификатор корреспондента
    private ObjectOutputStream objectOutputStream; // поток для отправки объектов


    public ChatWindow(String username, int correspondentId, ObjectOutputStream objectOutputStream) {
        this.username = username;
        this.correspondentId = correspondentId;
        this.objectOutputStream = objectOutputStream;
    
        // Устанавливаем основные параметры
        setTitle("Chat - " + username);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Центрируем окно
    
        // Элементы интерфейса
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
    
        messageField = new JTextField();
        sendButton = new JButton("Send");
    
        // Добавляем обработчик для отправки сообщений
        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                sendMessage(message);  // Отправляем сообщение
                messageField.setText("");
            }
        });
    
        // Добавляем элементы на форму
        add(scrollPane, BorderLayout.CENTER);
        add(messageField, BorderLayout.SOUTH);
        add(sendButton, BorderLayout.EAST);
    
        // Отображаем окно чатов
        setVisible(true);
    }    

    // Метод для подключения к серверу
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 10001); // Подключение к серверу на порту 10001
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Получение сообщений от сервера
            chatArea.append("Connected to server\n");

            // Запускаем поток для получения сообщений
            new Thread(new MessageReceiver()).start();
        } catch (IOException e) {
            e.printStackTrace();
            chatArea.append("Failed to connect to server\n");
        }
    }

    // Метод отправки сообщения на сервер
    private void sendMessage(String message) {
        try {
            objectOutputStream.writeObject(new MessagePacket(correspondentId, message));  // Отправка сообщения
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Внутренний класс для получения сообщений от сервера
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    chatArea.append("Server: " + serverMessage + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                chatArea.append("Connection lost\n");
            }
        }
    }    
}
