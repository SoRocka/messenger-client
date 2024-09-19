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

    public ChatWindow(String username) {
        this.username = username;

        // Настраиваем основные параметры окна
        setTitle("Chat - " + username);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Центрируем окно

        // Создаем элементы интерфейса
        chatArea = new JTextArea();
        chatArea.setEditable(false); // Запретить редактирование области чата
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        sendButton = new JButton("Send");

        // Добавляем обработчик для отправки сообщений
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Создаем панель для ввода сообщения
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Добавляем все элементы в окно
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // Подключаемся к серверу
        connectToServer();

        // Делаем окно видимым
        setVisible(true);

        // Начинаем поток для получения сообщений
        new Thread(new MessageReceiver()).start();
    }

    // Метод для подключения к серверу
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 10001); // Подключение к серверу на порту 10001
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            chatArea.append("Connected to server\n");
        } catch (IOException e) {
            e.printStackTrace();
            chatArea.append("Failed to connect to server\n");
        }
    }

    // Метод для отправки сообщений
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            chatArea.append(username + ": " + message + "\n");
            out.println(message); // Отправляем сообщение на сервер
            messageField.setText(""); // Очищаем поле ввода
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatWindow("testuser"));
    }
}
