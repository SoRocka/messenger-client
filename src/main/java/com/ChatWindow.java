package com;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

import org.springframework.stereotype.Component;

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
        setSize(1280, 720);  // Размер окна согласно макету
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Инициализация пользовательского интерфейса
        initUI(registeredUsers);

        // Запускаем поток для получения сообщений от сервера
        new Thread(new MessageReceiver()).start();
    }

    private void initUI(List<String> registeredUsers) {
        // **Создание верхней панели с именем пользователя**
        JPanel topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(970, 56));  // Размер верхней панели
        topPanel.setBackground(new Color(25, 25, 25));  // Тёмный фон
        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));  // Стиль и размер текста
        topPanel.add(usernameLabel);
        add(topPanel, BorderLayout.NORTH);
    
        // **Настройка панели чата**
        chatArea = new JTextPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("assets/chat-bg-pattern-dark.png"));
                g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(32, 32, 32));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
    
        // **Поле ввода сообщения**
        messageField = new JTextField();
        messageField.setBackground(new Color(48, 48, 48));
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);
    
        // **Кнопка отправки сообщения с изображением**
        sendButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("assets/Sign_in_circle.png")));
        sendButton.setBackground(new Color(48, 48, 48));
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);  // Убираем границу
        sendButton.setContentAreaFilled(false);  // Отключаем заливку
    
        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageField.setText("");
            }
        });
    
        // **Левое меню (список пользователей)**
        userList = new JList<>(registeredUsers.toArray(new String[0]));
        userList.setBackground(new Color(51, 51, 51));  // #333333 - цвет фона
        userList.setForeground(Color.WHITE);
        userList.setFixedCellHeight(60);  // Высота каждого элемента списка
        userList.setSelectionBackground(new Color(98, 0, 238));  // Цвет выделения
        userList.setSelectionForeground(Color.WHITE);
        userList.setFont(new Font("SansSerif", Font.PLAIN, 14));  // Шрифт для списка
    
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                currentCorrespondentId = getUserIdByName(selectedUser);
                displayMessagesForUser(currentCorrespondentId);
            }
        });
    
        // **Добавление кружков для пользователей**
        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("assets/Ellipse_17.png"));
                label.setIcon(icon);
                label.setHorizontalTextPosition(JLabel.RIGHT);
                return label;
            }            
        });
    
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(310, 720));  // Размер левого меню
    
        // **Панель для чата и ввода сообщения**
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);  // Область чата
    
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.setBackground(new Color(32, 32, 32));
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);  // Добавляем поле ввода в правую часть чата
    
        // Панель с разделением
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userScrollPane, chatPanel);
        splitPane.setDividerLocation(310);  // Фиксированное положение разделителя
        splitPane.setEnabled(false);  // Отключаем возможность изменения положения разделителя
        splitPane.setDividerSize(1);  // Толщина разделителя
        splitPane.setBackground(Color.GRAY);  // Цвет разделителя
        splitPane.setBorder(null);  // Убираем границы
    
        add(splitPane, BorderLayout.CENTER);
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

    private void addMessageToChat(int correspondentId, String message) {
        if (!messagesMap.containsKey(correspondentId)) {
            messagesMap.put(correspondentId, new ArrayList<>());
        }
        messagesMap.get(correspondentId).add(message);
    }

    private void displayMessagesForUser(int correspondentId) {
        chatArea.setText("");  // Очищаем чат
        List<String> messages = messagesMap.get(correspondentId);
        if (messages != null) {
            for (String message : messages) {
                appendToChat(message);
            }
        }
    }

    private void appendToChat(String message) {
        try {
            chatArea.getDocument().insertString(chatArea.getDocument().getLength(), message + "\n", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                Object incomingMessage;
                while ((incomingMessage = objectInputStream.readObject()) != null) {
                    if (incomingMessage instanceof MessagePacket) {
                        MessagePacket messagePacket = (MessagePacket) incomingMessage;

                        String receivedMessage = "Message from correspondent " + messagePacket.getCorrespondentId() + ": " + messagePacket.getMessage();
                        List<String> currentMessages = messagesMap.get(messagePacket.getCorrespondentId());

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

    private int getUserIdByName(String username) {
        return userIdMap.getOrDefault(username, -1);
    }
}
