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
import java.util.stream.Collectors;

import com.ui.RoundedButtonUI;



    public class LoginClientWindow extends JFrame {

        private JTextField usernameField;
        private JPasswordField passwordField;
        private Socket socket;
        private ObjectInputStream objectInputStream;
        private ObjectOutputStream objectOutputStream;
        private JLabel errorLabel;
        private List<String> registeredUsers;
        private boolean connected = false;


        public LoginClientWindow() {
            FlatDarkLaf.setup();
            UIManager.put("Button.paint", false);  // Отключаем стандартную отрисовку кнопок в FlatLaf

            // Устанавливаем параметры окна
            setTitle("Вход - Telegram");
            setSize(1280, 720); 
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(false);

            JPanel backgroundPanel = createBackgroundPanel();
            JPanel loginPanel = createLoginPanel();

            backgroundPanel.add(loginPanel);
            add(backgroundPanel);

            connectToServer();  // Запускаем попытки подключения
            setVisible(true);
        }


        private void connectToServer() {
            new Thread(() -> {
                while (!connected) {
                    try {
                        socket = new Socket("localhost", 10001);
                        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        objectInputStream = new ObjectInputStream(socket.getInputStream());
                        connected = true;
                        errorLabel.setText(" ");
                        System.out.println("Успешно подключились к серверу!");
                    } catch (IOException e) {
                        errorLabel.setText(" ");  // Очищаем старое сообщение
                        errorLabel.setText("Соединение с сервером временно недоступно.");
                        System.out.println("Не удалось подключиться к серверу, повторная попытка через 5 секунд...");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        private JLabel createTitleLabel() {
            JLabel titleLabel = new JLabel("Вход");
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            titleLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            return titleLabel;
        }
        
        private JTextField createUsernameField() {
            JTextField usernameField = new JTextField();
            usernameField.setMaximumSize(new Dimension(400, 40));
            usernameField.setBackground(new Color(48, 48, 48));
            usernameField.setForeground(Color.WHITE);
            usernameField.setCaretColor(Color.WHITE);
            usernameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            usernameField.setToolTipText("Имя пользователя");
            usernameField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(146, 146, 146))); 
            return usernameField;
        }
        
        private JPasswordField createPasswordField() {
            JPasswordField passwordField = new JPasswordField();
            passwordField.setMaximumSize(new Dimension(400, 40));
            passwordField.setBackground(new Color(48, 48, 48));
            passwordField.setForeground(Color.WHITE);
            passwordField.setCaretColor(Color.WHITE);
            passwordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            passwordField.setToolTipText("Пароль");
            passwordField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(146, 146, 146))); 
            return passwordField;
        }
        
        private JLabel createRegisterLabel() {
            JLabel registerLabel = new JLabel("Нет аккаунта?");
            registerLabel.setForeground(new Color(180, 180, 180));
            registerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            registerLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            return registerLabel;
        }
        
        private JLabel createErrorLabel() {
            
            JLabel errorLabel = new JLabel(" ");
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            return errorLabel;
        }
        

        private JPanel createLoginPanel() {
            JPanel loginPanel = new JPanel();
            loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
            loginPanel.setBounds(400, 120, 480, 500);
            loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
            loginPanel.setOpaque(true); 
            loginPanel.setBackground(new Color(0, 0, 0, 75));  // Полупрозрачный черный фон
            
            // Добавление элементов в панель логина
            loginPanel.add(createTitleLabel());
            loginPanel.add(Box.createRigidArea(new Dimension(0, 40)));
            loginPanel.add(usernameField = createUsernameField());
            loginPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            loginPanel.add(passwordField = createPasswordField());
            loginPanel.add(Box.createRigidArea(new Dimension(0, 30)));
            loginPanel.add(createLoginButton());
            loginPanel.add(Box.createRigidArea(new Dimension(0, 40)));
            loginPanel.add(createRegisterLabel());
            
            // Ошибки
            errorLabel = createErrorLabel();
            loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            loginPanel.add(errorLabel);
        
            return loginPanel;
        }
        
        private JButton createLoginButton() {
            JButton loginButton = new JButton("Авторизоваться");
            loginButton.setUI(new RoundedButtonUI(50));
            loginButton.setBackground(new Color(0, 150, 136));
            loginButton.setForeground(Color.WHITE);
            loginButton.setFont(new Font("Roboto", Font.BOLD, 18));
            loginButton.setFocusPainted(false);
            loginButton.setBorderPainted(false);
            loginButton.setOpaque(false);
            loginButton.setContentAreaFilled(false);
            loginButton.setRolloverEnabled(false);
            loginButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            loginButton.addActionListener(this::handleLogin);
            return loginButton;
        }
        

        private JPanel createBackgroundPanel() {
            JPanel backgroundPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("assets/chat-bg-pattern-dark.png"));
                    g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            };
            backgroundPanel.setLayout(null);
            return backgroundPanel;
        }
        

        private void handleLogin(ActionEvent e) {
            errorLabel.setText(" ");  // Очищаем ошибку при новой попытке
        
            if (!connected) {
                // Попытка подключения к серверу
                try {
                    socket = new Socket("localhost", 10001);
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    connected = true;
                    System.out.println("Соединение с сервером установлено.");
                } catch (IOException ex) {
                    errorLabel.setText("Не удалось подключиться к серверу.");
                    ex.printStackTrace();
                    return;  // Выходим из метода, если подключение не удалось
                }
            }
        
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
        
            try {
                System.out.println("Отправка логин-пакета на сервер...");
                LoginPacket loginPacket = new LoginPacket(username, password);
                sendPacket(loginPacket);
        
                Object response = objectInputStream.readObject();
                if (response instanceof EchoPacket) {
                    EchoPacket echoPacket = (EchoPacket) response;
                    if (echoPacket.getText().equals("Login successful!")) {
                        errorLabel.setText(" ");
                        JOptionPane.showMessageDialog(this, "Login successful!");
        
                        // Ожидаем получения списка пользователей от сервера
                        @SuppressWarnings("unchecked")
                        List<String> users = (List<String>) objectInputStream.readObject();  // Получаем список пользователей
                        System.out.println("Получен список пользователей: " + users);
        
                        // Передаём список пользователей в окно чата
                        SwingUtilities.invokeLater(() -> {
                            new ChatWindow(username, echoPacket.getCorrespondentId(), objectOutputStream, objectInputStream, users);
                            dispose();  // Закрываем окно авторизации
                        });
                    } else {
                        errorLabel.setText("Неправильный логин или пароль.");
                    }
                } else {
                    errorLabel.setText("Ошибка: Некорректный ответ от сервера.");
                }
            } catch (IOException | ClassNotFoundException ex) {
                errorLabel.setText("Ошибка отправки данных на сервер.");
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
