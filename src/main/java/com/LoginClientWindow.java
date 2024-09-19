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
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public LoginClientWindow() {
        // Настраиваем соединение с сервером
        try {
            socket = new Socket("localhost", 10001);  // Подключаемся к серверу
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Устанавливаем параметры окна
        setTitle("Login");
        setSize(800, 350);
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
            System.out.println("Sending login packet to server...");
    
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
                
                    // Открываем окно чата
                    new ChatWindow(username, echoPacket.getCorrespondentId(), objectOutputStream);
                    dispose();  // Закрываем окно входа
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.");
                }                
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    

    private void sendPacket(Packet packet) {
        // Метод для отправки пакета на сервер через ObjectOutputStream
        try {
            objectOutputStream.writeObject(packet);  // Отправляем объект пакета
            objectOutputStream.flush();              // Завершаем отправку
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginClientWindow());
    }
}
