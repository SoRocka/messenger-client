package com;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class LoginClientWindow extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public LoginClientWindow() {
        // Настраиваем тему FlatLaf (темная тема)
        FlatDarkLaf.setup();
        
        // Настраиваем соединение с сервером
        try {
            socket = new Socket("localhost", 10001);  // Подключаемся к серверу
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Устанавливаем параметры окна
        setTitle("Вход - Telegram");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Создаем панели и поля для логина и пароля
        JPanel panel = new JPanel();
        panel.setBackground(new Color(28, 28, 28));  // Темный фон
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Логотип/название
        JLabel titleLabel = new JLabel("Telegram");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Поле для ввода логина
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(300, 40));
        usernameField.setBackground(new Color(48, 48, 48));
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        usernameField.setBorder(createRoundedBorder());
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setToolTipText("Имя пользователя");

        // Поле для ввода пароля
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setBackground(new Color(48, 48, 48));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setBorder(createRoundedBorder());
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setToolTipText("Пароль");

        // Кнопка входа
        JButton loginButton = new JButton("Войти");
        loginButton.setMaximumSize(new Dimension(300, 40));
        loginButton.setBackground(new Color(0, 150, 136));  // Зеленая кнопка как в Telegram
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setBorder(createRoundedBorder());
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton.addActionListener(this::handleLogin);

        // Добавляем элементы в панель
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(loginButton);

        // Добавляем панель в окно
        add(panel);
        setVisible(true);
    }

    // Метод для создания закругленных границ
    private Border createRoundedBorder() {
        return new CompoundBorder(
                new LineBorder(new Color(60, 60, 60), 1, true),  // Граница с закруглением
                new EmptyBorder(5, 15, 5, 15));                  // Внутренний отступ
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Отправляем логин на сервер
        try {
            System.out.println("Отправка логин-пакета на сервер...");

            // Создаем объект LoginPacket и отправляем его на сервер
            LoginPacket loginPacket = new LoginPacket(username, password);
            sendPacket(loginPacket);  // Отправляем пакет на сервер

            // Получаем ответ от сервера через ObjectInputStream
            Packet response = (Packet) objectInputStream.readObject();  // Читаем объект пакета ответа

            if (response instanceof EchoPacket) {
                EchoPacket echoPacket = (EchoPacket) response;

                // Проверяем успешный ли логин
                if (echoPacket.getText().equals("Login successful!")) {
                    JOptionPane.showMessageDialog(this, "Login successful!");

                    // Передаем список зарегистрированных пользователей и BufferedReader
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    List<String> registeredUsers = List.of("user1", "user2", "user3");

                    // Открываем окно чата
                    new ChatWindow(username, echoPacket.getCorrespondentId(), objectOutputStream, objectInputStream, registeredUsers);
                    dispose();  // Закрываем окно входа
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.");
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void sendPacket(Packet packet) {
        // Метод для отправки пакета на сервер через ObjectOutputStream
        try {
            objectOutputStream.writeObject(packet);  // Отправляем объект пакета
            objectOutputStream.flush();              // Завершаем отправку
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginClientWindow::new);
    }
}
