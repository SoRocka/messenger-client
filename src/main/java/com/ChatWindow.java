package com;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.BadLocationException;

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

    private JPanel mainChatPanel;
    private JScrollPane chatScrollPane;

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
        // Верхняя панель с именем пользователя и иконкой настроек
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(970, 56));  // Размер верхней панели
        topPanel.setBackground(new Color(25, 25, 25));  // Тёмный фон
    
        // Создание mainChatPanel с фоновым изображением
        mainChatPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon icon = new ImageIcon("/home/soroka/Documents/repos/messenger-client/src/main/resources/assets/chat-bg-pattern-dark.png");
                g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainChatPanel.setLayout(new BoxLayout(mainChatPanel, BoxLayout.Y_AXIS));
        mainChatPanel.setBackground(new Color(32, 32, 32)); // Тёмный фон        
    
        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setForeground(new Color(40, 221, 141)); // Цвет текста
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));  // Стиль и размер текста
    
        // Добавляем кнопку настроек с иконкой шестерёнки
        ImageIcon settingsIcon = new ImageIcon("/home/soroka/Documents/repos/messenger-client/src/main/resources/assets/Setting_alt_line.png");
        JButton settingsButton = new JButton(settingsIcon);
        settingsButton.setFocusPainted(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
    
        topPanel.add(usernameLabel, BorderLayout.WEST);  // Имя пользователя слева
        topPanel.add(settingsButton, BorderLayout.EAST); // Кнопка настроек справа
    
        add(topPanel, BorderLayout.NORTH);
    
        // Левое меню: поиск и список пользователей
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(310, 720));  // Размер левого меню
        leftPanel.setBackground(new Color(51, 51, 51));  // #333333 - цвет фона
    
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(310, 30));
        searchField.setBackground(new Color(41, 41, 41));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
        leftPanel.add(searchField, BorderLayout.NORTH);
    
        // Список пользователей
        userList = new JList<>(registeredUsers.toArray(new String[0]));
        userList.setBackground(new Color(51, 51, 51));  // #333333 - цвет фона
        userList.setForeground(Color.WHITE);
        userList.setFixedCellHeight(60);  // Высота каждого элемента списка
        userList.setSelectionBackground(new Color(98, 0, 238));  // Цвет выделения
        userList.setSelectionForeground(Color.WHITE);
        userList.setFont(new Font("SansSerif", Font.PLAIN, 14));  // Шрифт для списка
    
        // Добавление события выбора пользователя
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                currentCorrespondentId = getUserIdByName(selectedUser);  // Присваиваем ID выбранного пользователя
                if (currentCorrespondentId == -1) {
                    System.out.println("Пользователь не найден: " + selectedUser);
                } else {
                    System.out.println("Текущий корреспондент: " + selectedUser + ", ID: " + currentCorrespondentId);
                }
                displayMessagesForUser(currentCorrespondentId);  // Отображение сообщений выбранного пользователя
            }
        });
    
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(310, 690));  // Высота
    
        // Добавляем компоненты в левую панель
        leftPanel.add(userScrollPane, BorderLayout.CENTER);
    
        // Используем mainChatPanel для отображения сообщений
        chatScrollPane = new JScrollPane(mainChatPanel);  
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setBorder(null);  // Убираем границы
    
        // **Инициализация компонентов отправки сообщений**
        messageField = new JTextField();  // Инициализация поля ввода сообщения
        messageField.setBackground(new Color(48, 48, 48));
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);
    
        sendButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("assets/Sign_in_circle.png")));  // Инициализация кнопки отправки
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
    
        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageField.setText("");
            }
        });
    
        // Панель для отправки сообщений
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.setBackground(new Color(32, 32, 32));
    
        // **Панель для чата и ввода сообщения**
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);  // Область чата должна занимать центральную часть
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);  // Поле ввода сообщения внизу чата
        
        // Добавляем компоненты в окно
        add(chatPanel, BorderLayout.CENTER);
        
        // Панель с разделением
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, chatPanel);
        splitPane.setDividerLocation(310);  // Фиксированное положение разделителя
        splitPane.setEnabled(false);  // Отключаем возможность изменения положения разделителя
        splitPane.setDividerSize(1);  // Толщина разделителя
        splitPane.setBackground(Color.GRAY);  // Цвет разделителя
        splitPane.setBorder(null);  // Убираем границы
    
        // Добавляем компоненты в окно
        add(splitPane, BorderLayout.CENTER);
        setVisible(true);
    }
    
    
    private void appendStyledMessage(String message, boolean isSentByUser) {
        System.out.println("Добавление сообщения в интерфейс: " + message);
    
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
    
        // Создаем метку с сообщением
        JLabel messageLabel = new JLabel(message);
        messageLabel.setOpaque(true);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));  // Отступы внутри сообщения
    
        if (isSentByUser) {
            messageLabel.setBackground(new Color(0, 255, 136));  // Зеленый фон
            messageLabel.setForeground(Color.WHITE);  // Белый текст
            messagePanel.add(messageLabel, BorderLayout.EAST);  // Выравниваем по правому краю
        } else {
            messageLabel.setBackground(new Color(220, 220, 220));  // Серый фон
            messageLabel.setForeground(Color.BLACK);  // Черный текст
            messagePanel.add(messageLabel, BorderLayout.WEST);  // Выравниваем по левому краю
        }
    
        // Добавляем панель с сообщением в главный чат-панель
        mainChatPanel.add(messagePanel);
    
        // Обновляем интерфейс и прокручиваем чат вниз
        mainChatPanel.revalidate();
        mainChatPanel.repaint();
    
        // Прокрутка чата вниз
        if (chatScrollPane != null) {
            chatScrollPane.getVerticalScrollBar().setValue(chatScrollPane.getVerticalScrollBar().getMaximum());
        } else {
            System.out.println("chatScrollPane is null, не удалось прокрутить чат");
        }
    }
    

    private void sendMessage(String message) {
        try {
            // Отправляем сообщение на сервер
            objectOutputStream.writeObject(new MessagePacket(currentCorrespondentId, message));
            objectOutputStream.flush();

            System.out.println("Отправка сообщения: " + message);

            // Добавляем отправленное сообщение в карту сообщений
            addMessageToChat(currentCorrespondentId, "You: " + message);

            // Обновляем интерфейс с добавлением нового сообщения
            appendStyledMessage("You: " + message, true);  // true указывает, что сообщение отправлено вами

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void receiveMessage(int correspondentId, String message) {
        // Добавляем полученное сообщение от корреспондента в карту сообщений
        System.out.println("Получено сообщение от корреспондента с id: " + correspondentId + ", сообщение: " + message);
        addMessageToChat(correspondentId, "Correspondent: " + message);
    
        // Если это сообщение от текущего корреспондента, обновляем чат
        if (correspondentId == currentCorrespondentId) {
            System.out.println("Отображение сообщений для пользователя с id: " + correspondentId);
            displayMessagesForUser(correspondentId);  // Отобразить все сообщения
        }
    }
    


    // Метод для добавления сообщения в карту сообщений
    private void addMessageToChat(int correspondentId, String message) {
        System.out.println("Добавляем сообщение в карту для корреспондента с id: " + correspondentId + ", сообщение: " + message);
    
        if (!messagesMap.containsKey(correspondentId)) {
            messagesMap.put(correspondentId, new ArrayList<>());
        }
        messagesMap.get(correspondentId).add(message);
    }
    

    private void displayMessagesForUser(int correspondentId) {
        System.out.println("Отображение сообщений для пользователя с id: " + correspondentId);
        
        // Очищаем текущую панель чата перед добавлением новых сообщений
        mainChatPanel.removeAll();
    
        List<String> messages = messagesMap.get(correspondentId);  // Получаем список сообщений для выбранного корреспондента
        if (messages != null) {
            for (String message : messages) {
                boolean isSentByUser = message.startsWith("You:"); // Проверка, отправлено ли сообщение текущим пользователем
                System.out.println("Добавление сообщения в чат: " + message);
                appendStyledMessage(message, isSentByUser);  // Добавляем каждое сообщение с правильным выравниванием
            }
        }
    
        // Обновляем интерфейс
        mainChatPanel.revalidate();
        mainChatPanel.repaint();
    }
    


    // Метод для добавления сообщения в JTextPane (chatArea)
    private void appendToChat(String message) {
        try {
            chatArea.getDocument().insertString(chatArea.getDocument().getLength(), message + "\n", null);  // Добавляем сообщение в JTextPane
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для обработки входящих сообщений в MessageReceiver
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                Object incomingMessage;
                while ((incomingMessage = objectInputStream.readObject()) != null) {
                    if (incomingMessage instanceof MessagePacket) {
                        MessagePacket messagePacket = (MessagePacket) incomingMessage;

                        // Получаем ID корреспондента и сообщение
                        int correspondentId = messagePacket.getCorrespondentId();
                        String receivedMessage = messagePacket.getMessage();

                        // Добавляем полученное сообщение в локальный чат
                        receiveMessage(correspondentId, receivedMessage);

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
