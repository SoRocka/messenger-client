package com;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import javax.swing.text.StyledDocument;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.BadLocationException;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
    private JLabel headerUserLabel;

    private Map<String, Integer> userIdMap = new HashMap<>();
    private int currentCorrespondentId;
    private Map<Integer, ArrayList<String>> messagesMap = new HashMap<>();

    private JPanel mainChatPanel;
    private JScrollPane chatScrollPane;
    private int userId;


    public ChatWindow(String username, int correspondentId, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, List<String> registeredUsers) {
        this.username = username;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
    
        // Заполняем карту соответствий имен пользователей и их ID
        for (int i = 0; i < registeredUsers.size(); i++) {
            userIdMap.put(registeredUsers.get(i), i + 1);
        }
    
        // Получаем userId на основе username
        this.userId = getUserIdByName(username);
        if (this.userId == -1) {
            System.out.println("Ошибка: Не удалось найти userId для пользователя " + username);
            // Дополнительная обработка ошибки
        }
    
        this.correspondentId = correspondentId;
    
        // Применяем темную тему FlatLaf
        FlatDarkLaf.setup();
        
        // Убираем стандартные рамки окна
        setUndecorated(true); // Отключает заголовок и рамки

        
        setTitle("Telegram - " + username);
        setSize(1280, 720);  // Размер окна согласно макету
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    
        // Инициализация пользовательского интерфейса
        initUI(registeredUsers);
    
        // Запускаем поток для получения сообщений от сервера
        new Thread(new MessageReceiver()).start();
    }
    
    private void updateCurrentUserLabel(String username) {
        headerUserLabel.setText(username);  // Обновляем имя в шапке
    }

    private void initUI(List<String> registeredUsers) {
        // Цвета для интерфейса (основываемся на палитре Telegram)
        Color backgroundColor = new Color(35, 39, 42);  // Темный фон
        Color sidebarColor = new Color(30, 33, 36);  // Цвет для левого меню
        Color selectedUserColor = new Color(72, 130, 180);  // Цвет для выделения активного пользователя
        Color searchFieldColor = new Color(48, 52, 55);  // Фон для поля поиска



        // Инициализация главной панели чата с фоновым изображением
        mainChatPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("assets/chat-bg-pattern-dark.png"));
                // Ограничьте высоту, например, размером панели или окна, чтобы фон не прокручивался
                g.drawImage(icon.getImage(), 0, 0, getWidth(), mainChatPanel.getHeight(), this);
            }    
        };
        mainChatPanel.setLayout(new BoxLayout(mainChatPanel, BoxLayout.Y_AXIS));
        mainChatPanel.setOpaque(false); // Устанавливаем панель как прозрачную, чтобы фон отображался правильно
    
        // Инициализация верхней панели
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(970, 56));  // Размер верхней панели
        topPanel.setBackground(new Color(25, 25, 25));  // Тёмный фон


        // Инициализация headerUserLabel с вашим именем пользователя
        headerUserLabel = new JLabel(username);
        headerUserLabel.setForeground(new Color(40, 221, 141)); // Цвет текста
        headerUserLabel.setFont(new Font("SansSerif", Font.BOLD, 16));  // Стиль и размер текста
        headerUserLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Инициализация иконки пользователя (кружочек)
        ImageIcon userIcon = new ImageIcon(getClass().getClassLoader().getResource("assets/Ellipse_17.png"));
        JLabel userIconLabel = new JLabel(userIcon);
        userIconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));  // 10px отступ сверху для иконки пользователя


        // Добавляем кнопку настроек с иконкой шестерёнки
        ImageIcon settingsIcon = new ImageIcon(getClass().getClassLoader().getResource("assets/Setting_alt_line.png"));
        JButton settingsButton = new JButton(settingsIcon);
        settingsButton.setFocusPainted(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);

        // Устанавливаем отступ (марджин) слева для шестерёнки
        settingsButton.setBorder(BorderFactory.createEmptyBorder(7, 135, 0, 0));  // 10px отступ слева


        // Создание панели для иконки пользователя, имени и шестерёнки с уменьшенным расстоянием между ними
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));  // 5 - горизонтальный отступ, 0 - вертикальный
        userInfoPanel.setOpaque(false); // Сделать панель прозрачной
        userInfoPanel.add(userIconLabel);   // Иконка пользователя
        userInfoPanel.add(headerUserLabel); // Имя пользователя
        userInfoPanel.add(settingsButton);  // Кнопка настроек

        // Добавляем userInfoPanel (иконка, имя и настройки) в левую часть topPanel
        topPanel.add(userInfoPanel, BorderLayout.WEST);


        // Добавление topPanel в верхнюю часть окна
        add(topPanel, BorderLayout.NORTH);


        // Левое меню: поиск и список пользователей
        JPanel leftPanel = new JPanel(new BorderLayout());  // Левое меню оставляем как leftPanel
        leftPanel.setPreferredSize(new Dimension(310, 720));  // Размер левого меню
        // Фон левого меню
        leftPanel.setBackground(sidebarColor);

        
        // Поле поиска
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(310, 30));
        searchField.setBackground(searchFieldColor);
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Скругляем и добавляем отступы

        leftPanel.add(searchField, BorderLayout.NORTH);
        
                
        // Список пользователей
        List<String> modifiedUsers = new ArrayList<>(registeredUsers);

        // Заменяем имя текущего пользователя на "Заметки" сразу после загрузки
        for (int i = 0; i < modifiedUsers.size(); i++) {
            if (modifiedUsers.get(i).equals(username)) {
                modifiedUsers.set(i, "Заметки");
                break;
            }
        }

        userList = new JList<>(modifiedUsers.toArray(new String[0]));
        userList.setBackground(sidebarColor);
        userList.setForeground(Color.WHITE);
        userList.setFixedCellHeight(60);  // Высота каждого элемента списка
        userList.setSelectionBackground(selectedUserColor);  // Цвет для выделения активного пользователя
        userList.setSelectionForeground(Color.WHITE);
        userList.setFont(new Font("SansSerif", Font.PLAIN, 14));  // Шрифт для списка
            
        // Список пользователей рендерим в стиле Telegram
        userList.setCellRenderer(new UserListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    label.setBackground(selectedUserColor); // Меняем цвет фона при выделении
                } else {
                    label.setBackground(sidebarColor); // Цвет для обычных пользователей
                }
                label.setOpaque(true);  // Устанавливаем прозрачность
                label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Отступы
                return label;
            }
        });
        


        // Добавление события выбора пользователя
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                currentCorrespondentId = getUserIdByName(selectedUser);
                if (currentCorrespondentId == -1) {
                    System.out.println("Пользователь не найден: " + selectedUser);
                } else {
                    System.out.println("Текущий корреспондент: " + selectedUser + ", ID: " + currentCorrespondentId);
                    
                    // // Обновляем имя текущего пользователя около кружочка
                    // updateCurrentUserLabel(selectedUser);
        
                    // Активируем поле и кнопку отправки сообщений
                    messageField.setEnabled(true);
                    sendButton.setEnabled(true);
        
                    // Обновляем сообщения для выбранного пользователя
                    displayMessagesForUser(currentCorrespondentId);
                }
            }
        });

        // Добавляем DocumentListener для поиска
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterUserList();
            }
        
            @Override
            public void removeUpdate(DocumentEvent e) {
                filterUserList();
            }
        
            @Override
            public void changedUpdate(DocumentEvent e) {
                filterUserList();
            }
        
            // Метод для фильтрации списка пользователей
            private void filterUserList() {
                String filter = searchField.getText().toLowerCase();
                List<String> filteredUsers = new ArrayList<>();
                
                for (String user : registeredUsers) {
                    if (user.toLowerCase().contains(filter)) {
                        // Если ввод совпадает с именем текущего пользователя, добавляем "Заметки"
                        if (user.equalsIgnoreCase(username)) {
                            filteredUsers.add(0, "Заметки"); // Добавляем "Заметки" первыми
                        } else {
                            filteredUsers.add(user);
                        }
                    }
                }
            
                // Обновляем список пользователей
                userList.setListData(filteredUsers.toArray(new String[0]));
            }
        });
        
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(310, 690));  // Высота
    
        // Добавляем компоненты в левую панель
        leftPanel.add(userScrollPane, BorderLayout.CENTER);
    
        // Создаем JScrollPane для панели чата
        chatScrollPane = new JScrollPane(mainChatPanel);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(20);  // Увеличиваем шаг прокрутки
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setBorder(null);  // Убираем границы
        chatScrollPane.getViewport().setOpaque(false); // Прозрачность области просмотра
        chatScrollPane.setOpaque(false); // Прозрачность самого JScrollPane
            
        // Поле для ввода сообщений
        messageField = new JTextField();
        messageField.setBackground(new Color(48, 48, 48)); // Цвет поля
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);
        messageField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Добавляем отступы
        messageField.setPreferredSize(new Dimension(200, 50)); // Увеличиваем высоту поля
        messageField.setEnabled(false);  // Отключаем поле по умолчанию

        // Кнопка отправки сообщений
        sendButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("assets/Sign_in_circle.png")));
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setEnabled(false);  // Отключаем кнопку отправки сообщений по умолчанию

        // Теперь можно применить настройки для кнопки после ее инициализации
        sendButton.setBackground(new Color(72, 130, 180));  // Темно-синий цвет для кнопки отправки

        

        // Логика отправки сообщений
        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageField.setText("");
            }
        });

        // Нижняя панель для отправки сообщений
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.setBackground(new Color(32, 32, 32));

        // Добавляем панель чата и панель для отправки сообщений в интерфейс
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);  // Панель чата
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);  // Поле ввода сообщения

        // Разделяем левое меню и чат
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, chatPanel);
        splitPane.setDividerLocation(310);  // Положение разделителя
        splitPane.setEnabled(false);  // Отключаем изменение разделителя
        splitPane.setDividerSize(1);  // Размер разделителя

        add(splitPane, BorderLayout.CENTER);
        setVisible(true);
    }

    private void appendStyledMessage(String message, boolean isSentByUser) {
        JPanel messagePanel = new JPanel(new FlowLayout(isSentByUser ? FlowLayout.RIGHT : FlowLayout.LEFT));
        messagePanel.setOpaque(false);
    
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                int arc = 25;  // Увеличенное скругление углов
                int width = getWidth();
                int height = getHeight();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
                if (isSentByUser) {
                    g2d.setColor(new Color(0, 150, 136)); // Цвет для отправленных сообщений
                } else {
                    g2d.setColor(new Color(48, 48, 48)); // Цвет для полученных сообщений
                }
    
                g2d.fillRoundRect(0, 0, width, height, arc, arc);
                g2d.dispose();
            }
        };
        bubble.setOpaque(false);
    
        // Обновляем отступы
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 7, 10, 7));  // Отступы: сверху и снизу 10px, слева и справа 14px
    
        JLabel messageLabel = new JLabel("<html><p style=\"width: auto;\">" + message + "</p></html>");
        messageLabel.setOpaque(false);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 7, 10, 7));
        messageLabel.setFont(new Font("Commissioner", Font.PLAIN, 14));  // Установлен шрифт
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setHorizontalAlignment(SwingConstants.RIGHT);  // Выравнивание текста по правому краю
    
        bubble.add(messageLabel, BorderLayout.CENTER);
    
        messagePanel.add(bubble);
        mainChatPanel.add(messagePanel);
        mainChatPanel.add(Box.createVerticalStrut(10));  // Отступ между сообщениями
    
        mainChatPanel.revalidate();
        mainChatPanel.repaint();
    
        SwingUtilities.invokeLater(() -> {
            chatScrollPane.getVerticalScrollBar().setValue(chatScrollPane.getVerticalScrollBar().getMaximum());
        });
    }
    
    private void sendMessage(String message) {
        try {
            // Отправляем сообщение на сервер
            objectOutputStream.writeObject(new MessagePacket(currentCorrespondentId, message));
            objectOutputStream.flush();
    
            System.out.println("Сообщение отправлено: " + message);
    
            // Немедленно добавляем сообщение в локальный чат
            addMessageToChat(currentCorrespondentId, "You: " + message);
    
            // Обновляем интерфейс чата для отображения сообщения
            displayMessagesForUser(currentCorrespondentId);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
    
    private void receiveMessage(int correspondentId, String message) {
        // Если сообщение отправлено самим себе, просто игнорируем его
        if (correspondentId == this.userId) {
            return; // Сообщение уже отображено в sendMessage
        }
    
        String correspondentName = getUserNameById(correspondentId);
    
        // Добавляем сообщение как полученное
        addMessageToChat(correspondentId, correspondentName + ": " + message);
    
        // Если это сообщение от текущего корреспондента, обновляем чат
        if (correspondentId == currentCorrespondentId) {
            displayMessagesForUser(correspondentId);
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
    
    // Метод для отображения сообщений для выбранного корреспондента
    private void displayMessagesForUser(int correspondentId) {
        System.out.println("Отображение сообщений для пользователя с id: " + correspondentId);
    
        // Очищаем панель чата перед добавлением новых сообщений
        mainChatPanel.removeAll();
    
        List<String> messages = messagesMap.get(correspondentId);  // Получаем список сообщений для выбранного корреспондента
        if (messages != null) {
            for (String message : messages) {
                boolean isSentByUser = message.startsWith("You:"); // Проверка, отправлено ли сообщение текущим пользователем
                System.out.println("Добавление сообщения в чат: " + message);
                appendStyledMessage(message, isSentByUser);  // Добавляем каждое сообщение с правильным выравниванием
            }
        }
    
        // Перерисовываем интерфейс
        mainChatPanel.revalidate();
        mainChatPanel.repaint();
    }
    

    // Класс для получения сообщений от сервера
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                Object incomingMessage;
                while ((incomingMessage = objectInputStream.readObject()) != null) {
                    if (incomingMessage instanceof MessagePacket) {
                        MessagePacket messagePacket = (MessagePacket) incomingMessage;
    
                        // Получаем ID отправителя и сообщение
                        int correspondentId = messagePacket.getCorrespondentId();
                        String receivedMessage = messagePacket.getMessage();
    
                        System.out.println("Получено сообщение от пользователя с ID " + correspondentId + ": " + receivedMessage);
    
                        // Обрабатываем полученное сообщение
                        receiveMessage(correspondentId, receivedMessage);
    
                    } else {
                        System.out.println("Получен неизвестный тип пакета");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(ChatWindow.this, "Соединение потеряно.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    // Класс для кастомного отображения списка пользователей
    private class UserListCellRenderer extends DefaultListCellRenderer {
        private ImageIcon userIcon;

        public UserListCellRenderer() {
            userIcon = new ImageIcon(getClass().getClassLoader().getResource("assets/Ellipse_17.png"));
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setIcon(userIcon);
            label.setHorizontalTextPosition(JLabel.RIGHT);
            label.setIconTextGap(10); // Расстояние между иконкой и текстом
            return label;  // Возвращаем JLabel, который является java.awt.Component
        }
    }

    // Метод для получения ID пользователя по имени
    private int getUserIdByName(String username) {
        return userIdMap.getOrDefault(username, -1);
    }

    // Метод для получения имени пользователя по его ID
    private String getUserNameById(int id) {
        for (Map.Entry<String, Integer> entry : userIdMap.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey();
            }
        }
        return "Unknown";
    }
}
