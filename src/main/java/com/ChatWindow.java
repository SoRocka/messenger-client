package com;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatWindow extends JFrame {

    private JTextPane chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private String username;
    private int correspondentId;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    
    private Map<String, Integer> userIdMap = new HashMap<>();
    private int currentCorrespondentId;
    private Map<Integer, ArrayList<String>> messagesMap = new HashMap<>();

    public ChatWindow(String username, int correspondentId, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, List<String> registeredUsers) {
        this.username = username;
        this.correspondentId = correspondentId;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;

        // Применяем темную тему FlatLaf
        FlatDarkLaf.setup();

        // Заполняем карту соответствий имен пользователей и их ID
        for (int i = 0; i < registeredUsers.size(); i++) {
            userIdMap.put(registeredUsers.get(i), i + 1);
        }

        setTitle("Telegram - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Инициализация пользовательского интерфейса
        initUI(registeredUsers);

        // Запускаем поток для получения сообщений от сервера
        new Thread(new MessageReceiver()).start();
    }

    private void initUI(List<String> registeredUsers) {
        // Настройка панели чата
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(32, 32, 32));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // Поле ввода сообщения
        messageField = new JTextField();
        messageField.setBackground(new Color(48, 48, 48));
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);

        // Кнопка отправки сообщения
        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(98, 0, 238));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageField.setText("");
            }
        });

        // Список пользователей
        userList = new JList<>(registeredUsers.toArray(new String[0]));
        userList.setBackground(new Color(25, 25, 25));
        userList.setForeground(Color.WHITE);
        userList.setSelectionBackground(new Color(98, 0, 238));
        userList.setSelectionForeground(Color.WHITE);

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                currentCorrespondentId = getUserIdByName(selectedUser);
                displayMessagesForUser(currentCorrespondentId);
            }
        });

        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(200, 0));

        // Панель с разделением
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userScrollPane, chatScrollPane);
        splitPane.setDividerLocation(200);
        splitPane.setBackground(new Color(32, 32, 32));
        splitPane.setBorder(null);

        // Добавляем компоненты в окно
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.setBackground(new Color(32, 32, 32));

        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void sendMessage(String message) {
        try {
            // Отправляем сообщение на сервер
            objectOutputStream.writeObject(new MessagePacket(currentCorrespondentId, message));
            objectOutputStream.flush();

            // Добавляем сообщение с "You: " в локальный чат
            addMessageToChat(currentCorrespondentId, "You: " + message);
            
            // Обновляем отображение чата для текущего корреспондента
            displayMessagesForUser(currentCorrespondentId);
            
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
                appendToChat(message);
            }
        }
    }

    // Метод для добавления сообщений в JTextPane
    private void appendToChat(String message) {
        try {
            chatArea.getDocument().insertString(chatArea.getDocument().getLength(), message + "\n", null);
        } catch (Exception e) {
            e.printStackTrace();
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

                        // Проверяем, что сообщение не дублируется
                        String receivedMessage = "Message from correspondent " + messagePacket.getCorrespondentId() + ": " + messagePacket.getMessage();
                        List<String> currentMessages = messagesMap.get(messagePacket.getCorrespondentId());

                        // Если сообщение не было отправлено самим пользователем, добавляем его
                        if (currentMessages == null || !currentMessages.contains(receivedMessage)) {
                            addMessageToChat(messagePacket.getCorrespondentId(), receivedMessage);
                            if (messagePacket.getCorrespondentId() == currentCorrespondentId) {
                                displayMessagesForUser(currentCorrespondentId);
                            }
                        }
                    } else {
                        System.out.println("Received unknown packet type");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                chatArea.setText(chatArea.getText() + "\nConnection lost\n");
            }
        }
    }

    // Метод для получения ID пользователя по имени
    private int getUserIdByName(String username) {
        return userIdMap.getOrDefault(username, -1);
    }
}
