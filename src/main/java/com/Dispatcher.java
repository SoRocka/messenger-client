package com;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Dispatcher implements Runnable {
    private static final LinkedBlockingQueue<Event> packetQueue = new LinkedBlockingQueue<>();

    public static void event(Event e) {
        packetQueue.add(e);
    }

    public void run() {
        for (;;) {
            try {
                var e = packetQueue.take();
                processPacket(e.session, e.packet);
            } catch (InterruptedException x) {
                break;
            }
        }
    }

    private void processPacket(Session session, Packet p) {
        System.out.println("Processing packet: " + p.getType());
        try {
            switch (p) {
                case MessagePacket mP -> {
                    if (session.correspondent == null) {
                        System.out.println("Non-authorized");
                        return;
                    }
    
                    // Находим корреспондента по ID
                    var correspondent = Correspondent.findCorrespondentById(mP.getCorrespondentId());
    
                    if (correspondent != null) {
                        if (correspondent.activeSession != null) {
                            // Устанавливаем отправителя
                            mP.setCorrespondentId(session.correspondent.id);
                            System.out.println("Sending message to correspondent, id: " + correspondent.id);
                            correspondent.activeSession.send(mP);
                        } else {
                            System.out.println("Target correspondent is not connected, id: " + correspondent.id);
                        }
                    } else {
                        System.out.println("Correspondent not found for id: " + mP.getCorrespondentId());
                    }
                }
    
                case ListPacket emptyListP -> {
                    var filledListP = new ListPacket();
                    for (var c : Correspondent.listAll()) {
                        filledListP.addItem(c.id, c.login);
                    }
                    session.send(filledListP);
                }
                    
                case LoginPacket loginP -> {
                    String login = loginP.getUsername();  
                    String password = loginP.getPassword(); 

                    System.out.println("Received login: " + login);
                    System.out.println("Received password: " + password);

                    Correspondent correspondent = Correspondent.findCorrespondent(login);

                    if (correspondent != null && correspondent.checkPassword(password)) {
                        // Если у пользователя уже есть активная сессия, закрываем её
                        if (correspondent.activeSession != null) {
                            System.out.println("Closing previous session for " + login);
                            correspondent.activeSession.close();
                        }

                        // Устанавливаем новую активную сессию
                        correspondent.activeSession = session;
                        session.correspondent = correspondent;
                        System.out.println("Login successful for: " + login + ". Active session: " + correspondent.activeSession);
                        
                        // Отправляем подтверждение об успешной авторизации
                        session.send(new EchoPacket("Login successful!", correspondent.getId()));

                        // Отправляем список пользователей клиенту
                        List<String> users = Correspondent.listAll().stream()
                                .map(c -> c.login)
                                .collect(Collectors.toList());

                        // Используем метод sendObject() из Session
                        session.sendObject(users);

                    } else {
                        System.out.println("Login failed for: " + login);
                        session.send(new EchoPacket("Login failed!"));
                        session.close();
                    }                    
                }
                          
    
                default -> {
                    System.out.println("Unexpected packet type: " + p.getType());
                }
            }
        } catch (Exception ex) {
            System.out.println("Dispatcher problem: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
}
