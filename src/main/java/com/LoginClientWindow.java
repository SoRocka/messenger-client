package com;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

import com.ui.RoundedButtonUI;
import com.ui.RoundedBorderUI;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;


    public class LoginClientWindow extends JFrame {

        private JTextField usernameField;
        private JPasswordField passwordField;
        private Socket socket;
        private ObjectInputStream objectInputStream;
        private ObjectOutputStream objectOutputStream;
        private JLabel errorLabel;
        private boolean connected = false;
        private Point initialClick;  // Для хранения начальной точки клика

        public LoginClientWindow() {
            FlatDarkLaf.setup();

            // Убираем стандартные рамки окна
            setUndecorated(true); // Отключает заголовок и рамки

            // Добавляем обработчик для перетаскивания окна мышкой
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    initialClick = e.getPoint(); // Запоминаем начальное положение курсора
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    // Получаем текущее положение окна
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;

                    // Вычисляем смещение курсора
                    int xMoved = e.getX() - initialClick.x;
                    int yMoved = e.getY() - initialClick.y;

                    // Новая позиция окна
                    int X = thisX + xMoved;
                    int Y = thisY + yMoved;

                    // Устанавливаем новое положение окна
                    setLocation(X, Y);
                }
            });

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

            // Устанавливаем фокус на поле логина по умолчанию
            usernameField.requestFocusInWindow();

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
            usernameField.setMaximumSize(new Dimension(400, 50));  // Увеличиваем высоту
            usernameField.setBackground(new Color(48, 48, 48));
            usernameField.setForeground(Color.WHITE);
            usernameField.setCaretColor(Color.WHITE);
            
            // Используем шрифт Roboto
            usernameField.setFont(new Font("Roboto", Font.PLAIN, 16));
        
            // Плейсхолдер серого цвета
            usernameField.setText("логин");
            usernameField.setForeground(Color.GRAY);
        
            // Устанавливаем внутренние отступы через Border
            usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(146, 146, 146)),  // Нижнее подчёркивание
                BorderFactory.createEmptyBorder(10, 15, 10, 15)  // Внутренние отступы
            ));
        
            usernameField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (usernameField.getText().equals("логин")) {
                        usernameField.setText("");
                        usernameField.setForeground(Color.WHITE);
                    }
                }
        
                @Override
                public void focusLost(FocusEvent e) {
                    if (usernameField.getText().isEmpty()) {
                        usernameField.setForeground(Color.GRAY);
                        usernameField.setText("логин");
                    }
                }
            });
        
            return usernameField;
        }
        

        
        private JPasswordField createPasswordField() {
            JPasswordField passwordField = new JPasswordField();
            passwordField.setMaximumSize(new Dimension(400, 50));  // Увеличиваем высоту
            passwordField.setBackground(new Color(48, 48, 48));
            passwordField.setForeground(Color.WHITE);
            passwordField.setCaretColor(Color.WHITE);
            
            // Шрифт Roboto для поля пароля
            passwordField.setFont(new Font("Roboto", Font.PLAIN, 16));
        
            // Плейсхолдер серого цвета
            passwordField.setEchoChar((char) 0);  // Отключаем отображение символов пароля
            passwordField.setText("пароль");
            passwordField.setForeground(Color.GRAY);
        
            // Устанавливаем внутренние отступы через Border
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(146, 146, 146)),  // Нижнее подчёркивание
                BorderFactory.createEmptyBorder(10, 15, 10, 15)  // Внутренние отступы
            ));
        
            passwordField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (new String(passwordField.getPassword()).equals("пароль")) {
                        passwordField.setText("");
                        passwordField.setForeground(Color.WHITE);
                        passwordField.setEchoChar('●');  // Включаем символы пароля
                    }
                }
        
                @Override
                public void focusLost(FocusEvent e) {
                    if (new String(passwordField.getPassword()).isEmpty()) {
                        passwordField.setEchoChar((char) 0);  // Отключаем символы пароля
                        passwordField.setForeground(Color.GRAY);
                        passwordField.setText("пароль");
                    }
                }
            });
        
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
            
            // Добавляем пустое пространство, чтобы кнопка и метка были внизу
            loginPanel.add(Box.createVerticalGlue());
            
            // Панель для кнопки авторизации и метки "Нет аккаунта?"
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
            bottomPanel.setOpaque(false);  // Прозрачный фон для нижней панели
            
            // Кнопка авторизации
            bottomPanel.add(createLoginButton());
            bottomPanel.add(Box.createRigidArea(new Dimension(0, 40)));  // Отступ
            
            // Метка "Нет аккаунта?"
            bottomPanel.add(createRegisterLabel());
            
            // Ошибки
            errorLabel = createErrorLabel();
            bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            bottomPanel.add(errorLabel);
        
            // Добавляем нижнюю панель в основную панель
            loginPanel.add(bottomPanel);
        
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
        
        private void closeSocket() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        


        private void handleLogin(ActionEvent e) {
            errorLabel.setText(" ");  // Очищаем ошибку при новой попытке
        
            if (!connected || socket.isClosed()) {
                reconnectToServer();  // Переподключение, если соединение было закрыто
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
                        List<String> users = (List<String>) objectInputStream.readObject();
                        System.out.println("Получен список пользователей: " + users);
        
                        // Открываем окно чата и закрываем окно логина
                        SwingUtilities.invokeLater(() -> {
                            new ChatWindow(username, echoPacket.getCorrespondentId(), objectOutputStream, objectInputStream, users);
                            dispose();  // Закрываем окно авторизации
                        });
                    } else {
                        // Неправильный логин или пароль - очищаем оба поля
                        errorLabel.setText("Неправильный логин или пароль.");
                        usernameField.setText("");  // Очищаем поле логина
                        passwordField.setText("");  // Очищаем поле пароля
        
                        // Закрываем старое соединение, чтобы создать новое при следующей попытке
                        closeSocket();
                    }
                } else {
                    errorLabel.setText("Ошибка: Некорректный ответ от сервера.");
                    closeSocket();
                }
            } catch (IOException | ClassNotFoundException ex) {
                errorLabel.setText("Ошибка отправки данных на сервер.");
                ex.printStackTrace();
                closeSocket();
            }
        }
        

        
        private void reconnectToServer() {
            closeSocket();  // Закрываем старое соединение, если оно есть
        
            try {
                socket = new Socket("localhost", 10001);  // Новый сокет
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                connected = true;
                System.out.println("Успешно переподключились к серверу!");
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Не удалось подключиться к серверу.");
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
