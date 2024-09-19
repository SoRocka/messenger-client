package com;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ChatWindow extends JFrame {

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private String username;
    private int correspondentId;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    
    private Map<String, Integer> userIdMap = new HashMap<>();  // Карта для соответствия имен пользователей их ID
    private int currentCorrespondentId;  // ID текущего корреспондента
    private Map<Integer, ArrayList<String>> messagesMap = new HashMap<>();  // Карта для хранения сообщений для каждого корреспондента

    public ChatWindow(String username, int correspondentId, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, List<String> registeredUsers) {
        this.username = username;
        this.correspondentId = correspondentId;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;

        // Заполняем карту соответствий имен пользователей и их ID
        for (int i = 0; i < registeredUsers.size(); i++) {
            userIdMap.put(registeredUsers.get(i), i + 1);  // Например, присваиваем ID начиная с 1
        }

        setTitle("Chat - " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        sendButton = new JButton("Send");

        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageField.setText("");
                addMessageToChat(currentCorrespondentId, "You: " + message);
            }
        });

        userList = new JList<>(registeredUsers.toArray(new String[0]));
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                currentCorrespondentId = getUserIdByName(selectedUser);
                displayMessagesForUser(currentCorrespondentId);
            }
        });

        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userScrollPane, chatScrollPane);
        splitPane.setDividerLocation(150);

        add(splitPane, BorderLayout.CENTER);
        add(messageField, BorderLayout.SOUTH);
        add(sendButton, BorderLayout.EAST);
        setVisible(true);

        // Запускаем поток для получения сообщений от сервера
        new Thread(new MessageReceiver()).start();
    }

    private void sendMessage(String message) {
        try {
            objectOutputStream.writeObject(new MessagePacket(currentCorrespondentId, message));
            objectOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для добавления сообщения в чат
    private void addMessageToChat(int correspondentId, String message) {
        if (!messagesMap.containsKey(correspondentId)) {
            messagesMap.put(correspondentId, new ArrayList<>());
        }
        messagesMap.get(correspondentId).add(message);
    }

    // Метод для отображения сообщений для текущего пользователя
    private void displayMessagesForUser(int correspondentId) {
        chatArea.setText("");  // Очищаем чат
        List<String> messages = messagesMap.get(correspondentId);
        if (messages != null) {
            for (String message : messages) {
                chatArea.append(message + "\n");
            }
        }
    }

    // Внутренний класс для получения сообщений от сервера
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                Object incomingMessage;
                while ((incomingMessage = objectInputStream.readObject()) != null) {
                    if (incomingMessage instanceof MessagePacket) {
                        MessagePacket messagePacket = (MessagePacket) incomingMessage;
                        addMessageToChat(messagePacket.getCorrespondentId(), "Message from correspondent " + messagePacket.getCorrespondentId() + ": " + messagePacket.getMessage());
                        if (messagePacket.getCorrespondentId() == currentCorrespondentId) {
                            displayMessagesForUser(currentCorrespondentId);
                        }
                    } else {
                        System.out.println("Received unknown packet type");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                chatArea.append("Connection lost\n");
            }
        }
    }

    // Метод для получения ID пользователя по имени
    private int getUserIdByName(String username) {
        return userIdMap.getOrDefault(username, -1);  // Возвращаем -1, если пользователь не найден
    }
}
