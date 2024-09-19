package com;

import java.util.concurrent.LinkedBlockingQueue;

public class Dispatcher implements Runnable {
     private static final LinkedBlockingQueue<Event> packetQueue = new LinkedBlockingQueue<>();

     public static void event(Event e) {
        packetQueue.add(e);
     }

    public void run() {
        for(;;) {
            try {
                var e = packetQueue.take();
                processPacket(e.session, e.packet);
            }
            catch(InterruptedException x) {
                break;
            }
        }
    }

    private void processPacket(Session session, Packet p) {
        System.out.println("Processing packet: " + p.getType());
        try {
        switch(p) {
            case EchoPacket echoP -> {
                session.send(p);
            }

            case HiPacket hiP -> {
                var correspondent = Correspondent.findCorrespondent(hiP.login);
                if(correspondent == null) {
                    session.close();
                    return;
                }
                session.correspondent = correspondent;
                correspondent.activeSession = session;
                System.out.println("Correspondent authorized, id: " + correspondent.id);
            }

            case MessagePacket mP -> {
                if(session.correspondent == null) {
                    System.out.println("Non-authorized");
                    return;
                }
                var correspondent = Correspondent.findCorrespondent(mP.correspondentId);
                mP.correspondentId = session.correspondent.id;
                if(correspondent.activeSession != null) {
                    System.out.println("Sending message to correspondent, id: " + correspondent.id);
                    correspondent.activeSession.send(mP);
                } else {
                    System.out.println("Target correspondent not conneacted, id: " + correspondent.id);
                }
            }

            case ListPacket emptyListP -> {
                var filledListP = new ListPacket();
                for(var c : Correspondent.listAll()) {
                    filledListP.addItem(c.id, c.login);
                }
                session.send(filledListP);
            }
            
            default -> {
                System.out.println("Unexpected packet type: " + p.getType());
            }
        }
        } catch(Exception ex) {
			System.out.println("Dispatcher problem: " + ex.getMessage());
			ex.printStackTrace();
        }
    }
}