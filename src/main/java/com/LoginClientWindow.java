package com;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class LoginClientWindow extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public LoginClientWindow() {
        // Настраиваем соединение с сервером
        try {
            socket = new Socket("localhost", 10001);  // Подключаемся к серверу
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Устанавливаем параметры окна
        setTitle("Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Создаем панели и поля для логина и пароля
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        panel.add(loginButton);

        add(panel);
        setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Отправляем логин на сервер
        try {
            writer.println("LOGIN");
            writer.println(username);
            writer.println(password);
            writer.println();

            String response = reader.readLine();  // Читаем ответ от сервера
            if ("Login successful!".equals(response)) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                new ChatWindow(username);  // Открываем чат, передаем socket
                dispose();  // Закрываем окно входа
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
