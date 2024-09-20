package com;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.*;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.List;
import com.ui.RoundedButtonUI;



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
            setSize(1280, 720); // Изменяем размер окна
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(false);
    
            // Создаем фон с изображением
            JPanel backgroundPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Загружаем изображение из папки ресурсов                
                    ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("assets/chat-bg-pattern-dark.png")); // корректный путь к фону
                    g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            };
    
            backgroundPanel.setLayout(null);
    
            // Полупрозрачная панель для формы логина
            JPanel loginPanel = new JPanel();
            loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
            loginPanel.setBackground(new Color(0, 0, 0, 75));  // Прозрачный черный фон (более светлый)
            loginPanel.setBounds(400, 120, 480, 500); // Устанавливаем размер и положение панели
            loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
    
            // Логотип/название
            JLabel titleLabel = new JLabel("Вход");
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            titleLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    
            // Поле для ввода логина с нижней серой границей
            usernameField = new JTextField();
            usernameField.setMaximumSize(new Dimension(400, 40));
            usernameField.setBackground(new Color(48, 48, 48));
            usernameField.setForeground(Color.WHITE);
            usernameField.setCaretColor(Color.WHITE);
            usernameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            usernameField.setToolTipText("Имя пользователя");
            usernameField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(146, 146, 146)));  // Тонкая нижняя граница
    
            // Поле для ввода пароля с нижней серой границей
            passwordField = new JPasswordField();
            passwordField.setMaximumSize(new Dimension(400, 40));
            passwordField.setBackground(new Color(48, 48, 48));
            passwordField.setForeground(Color.WHITE);
            passwordField.setCaretColor(Color.WHITE);
            passwordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            passwordField.setToolTipText("Пароль");
            passwordField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(146, 146, 146)));  // Тонкая нижняя граница
    
            JButton loginButton = new JButton("Авторизоваться");
            loginButton.setUI(new RoundedButtonUI(50));  // Применяем кастомный UI с радиусом 50
            loginButton.setBackground(new Color(0, 150, 136));  // Цвет фона кнопки
            loginButton.setForeground(Color.WHITE);  // Цвет текста
            loginButton.setFont(new Font("Roboto", Font.BOLD, 18));  // Шрифт кнопки
            loginButton.setFocusPainted(false);  // Отключаем рисование фокуса
            loginButton.setBorderPainted(false);  // Отключаем рисование границ
            loginButton.setOpaque(false);  // Отключаем стандартную заливку
            loginButton.setContentAreaFilled(false);  // Убираем стандартную заливку
            loginButton.setRolloverEnabled(false);  // Отключаем эффект при наведении
            loginButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            loginButton.addActionListener(this::handleLogin);
            loginButton.setPressedIcon(null);
            
                       
            
            // Добавляем кнопку в панель
            loginPanel.add(loginButton);
            
    
            // Добавляем текст "Нет аккаунта?" внизу
            JLabel registerLabel = new JLabel("Нет аккаунта?");
            registerLabel.setForeground(new Color(180, 180, 180));
            registerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            registerLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    
            // Добавляем элементы в панель
            loginPanel.add(Box.createRigidArea(new Dimension(0, 50)));
            loginPanel.add(titleLabel);
            loginPanel.add(Box.createRigidArea(new Dimension(0, 40)));
            loginPanel.add(usernameField);
            loginPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            loginPanel.add(passwordField);
            loginPanel.add(Box.createRigidArea(new Dimension(0, 30)));
            loginPanel.add(loginButton);
            loginPanel.add(Box.createRigidArea(new Dimension(0, 40)));
            loginPanel.add(registerLabel);
    
            // Добавляем панель с фоном и форму логина
            backgroundPanel.add(loginPanel);
            add(backgroundPanel);
            setVisible(true);
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
