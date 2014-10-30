package gcom.client;

import gcom.GCom;
import gcom.utils.*;

import javax.swing.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Creates the GCom and the ClientGUI. Implements GComClient and receives the messages GCom delivers which are
 * later sent to the ClientGUI. This class also connects itself as a listener to the hold-back queue of GCom and
 * when the hold-back queue is changed this class tells the ClientGUI to update it's window containing information
 * about the hold-back queue. This class acts as model and controller in the MVC design pattern.
 */
public class Client implements GComClient, HoldBackQueueListener {

    private ClientGUI clientGUI;
    private GCom gCom;
    private String group;

    /**
     * Creates the GUI object and the GCom object.
     * @param rmiPort the local RMI port.
     * @param nameServiceAddress address to the name service.
     * @param nameServicePort port of the name service.
     */
    public Client(int rmiPort, String nameServiceAddress, int nameServicePort) {

        System.out.println("Local RMI port to be used: " + rmiPort);
        System.out.println("Name service address: " + nameServiceAddress);
        System.out.println("Name service port: " + nameServicePort);

        System.out.print("Initializing GUI...");
        clientGUI = new ClientGUI(this);
        System.out.println(" Done!");
        System.out.print("Setting up GCom...");
        Host nameService = null;
        try {
            nameService = new Host(InetAddress.getByName(nameServiceAddress), nameServicePort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if(nameService == null) {
            System.err.println("Name service address/port problem.");
            System.exit(1);
        }
        try {
            gCom = new GCom(rmiPort, this, nameService, nameServiceAddress);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(" Done!");
        System.out.print("Trying to join group ...");
        try {
            gCom.joinGroup(group);
            gCom.attachHoldBackQueueListener(this, group);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println(" Done!");

    }

    /**
     * When GCom delivers a message to this client, it calls this method.
     * @param message the message.
     */
    @Override
    public void deliverMessage(Message message) {

        String messageText = message.getText() + "\n";
        String debugText = "";
        if(clientGUI.isDebug()) {
            debugText = debugText.concat("\n------DEBUG-----");
            debugText = debugText.concat("\n\nSender: " + message.getSource());

            debugText = debugText.concat("\n\nVector Clock");
            for(Host key : message.getVectorClock().getClock().keySet()) {
                debugText = debugText.concat("\n" + key + " Clock: " + message.getVectorClock().getValue(key));
            }

            debugText = debugText.concat("\n\nBeen at (After sender)");
            for(int i = 0; i < message.getBeenAt().size(); i++) {
                debugText = debugText.concat("\n" + i + ": " + message.getBeenAt().get(i));
            }
            debugText = debugText.concat("\n----------------\n");
        }

        clientGUI.incomingMessage(messageText, debugText);
    }

    /**
     * When GCom delivers a message which has already been delivered.
     * @param message the already delivered message.
     */
    @Override
    public void deliverAlreadyReceivedMessage(Message message) {
        if(clientGUI.isDebug()) {
            if(message instanceof ViewChange) {
                ViewChange viewChange = (ViewChange)message;
                String members = "";
                for(Host member : viewChange.getMembers()) {
                    members = members.concat(" " + member.toString());
                }
                clientGUI.incomingMessageAlreadyReceived("View change, members:" + members);
            }else {
                clientGUI.incomingMessageAlreadyReceived(message.getText());
            }
        }
    }

    @Override
    public void debugSetVectorClock(VectorClock vectorClock) {
        clientGUI.vectorClockChanged(vectorClock);

    }

    /**
     * Sends a message to GCom.
     * @param message the message text
     * @param sendReliably if the message should be sent reliably.
     * @param deliverCausally if the message should be ordered causally.
     */
    public void sendMessage(String message, boolean sendReliably, boolean deliverCausally) {
        gCom.sendMessage(message, group, sendReliably, deliverCausally);
    }

    public void setSleepMillisBetweenClients(int millis) {
        gCom.setSleepMillisBetweenClients(millis);
    }

    public void setGroupName(String groupName) {
        this.group = groupName;
    }

    public static void main(String[] args) {
        if(args.length < 3) {
            System.err.println("Parameters: [Local RMI port to be used] [Name service address] [Name service port]");
            System.exit(1);
        }
        final int rmiPort = Integer.valueOf(args[0]);
        final String nameServiceAddress = args[1];
        final int nameServicePort = Integer.valueOf(args[2]);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(nameServiceAddress.equalsIgnoreCase("localhost")) {
                    try {
                        new Client(rmiPort, InetAddress.getLocalHost().getHostAddress(), nameServicePort);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                } else {
                    new Client(rmiPort, nameServiceAddress, nameServicePort);
                }
            }
        });
    }

    @Override
    public void messagePutInHoldBackQueue(Message message) {
        clientGUI.messagePutInHoldBackQueue(message);
    }

    @Override
    public void messageRemovedFromHoldBackQueue(Message message) {
        clientGUI.messageRemovedFromHoldBackQueue(message);
    }
}