package gcom;

import gcom.communicator.Communicator;
import gcom.utils.*;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** The most essential class of the GCom. The GCom class acts as the spider in a web. It handles the three modules
 * of GCom (group management, communication and message ordering) by sending and receiving information to/from the
 * modules. It takes, among other arguments, a GComClient which is used to deliver received and ordered messages
 * to. This class has a delivery queue which is attached to the message sorters and it's messages are delivered
 * to the GComClient using a thread.
 * Created by Jonas on 2014-10-03.
 */
public class GCom implements Runnable {

    private GroupManager groupManager;
    private Communicator communicator;
    private Map<String, MessageSorter> messageSorters;
    private boolean running;
    private Thread thread;
    private BlockingQueue<Message> deliveryQueue;
    private Host self;

    private DatabaseHandler databaseHandler;

    private GComClient gcomClient;

    /**
     * Creates a RMI server on the specified port. Creates the thread which delivers the messages to the GComClient.
     * @param rmiPort the port number for the local RMI registry.
     * @param gcomClient The GComClient which uses GCom.
     * @param nameService the name service.
     * @throws Exception
     */
    public GCom(int rmiPort, GComClient gcomClient, Host nameService, String cassandraAddress)
            throws Exception {
        RmiServer rmiServer = new RmiServer(rmiPort);
        databaseHandler = new DatabaseHandler(cassandraAddress);
        self = rmiServer.getHost();
        this.gcomClient = gcomClient;
        groupManager = new GroupManager(nameService, self, this);
        communicator = new Communicator(this, self);
        PeerCommunication stub = (PeerCommunication) UnicastRemoteObject.exportObject(communicator, rmiPort);
        rmiServer.bind(PeerCommunication.class.getSimpleName(), stub);
        NameServiceClient nameServiceClient = (NameServiceClient) UnicastRemoteObject.exportObject(groupManager, rmiPort);
        rmiServer.bind(NameServiceClient.class.getSimpleName(), nameServiceClient);
        messageSorters = new HashMap<>();

        deliveryQueue = new LinkedBlockingQueue<>();

        running = true;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Attaches a Hold-back queue listener to the specified message sorter.
     * @param listener the listener.
     * @param groupName the group to attach to.
     */
    public void attachHoldBackQueueListener(HoldBackQueueListener listener, String groupName) {
        messageSorters.get(groupName).setListener(listener);
    }

    /**
     * The method used by the client to send messages to the other remote clients in the group.
     * @param text the text to send.
     * @param group the group to multicast to.
     * @param sendReliably if the message should be sent reliably.
     * @param deliverCausally if the message should be ordered causally.
     */
    public void sendMessage(String text, String group, boolean sendReliably, boolean deliverCausally) {
        groupManager.getVectorClock(group).increment(self);
        Message message = new Message(sendReliably, deliverCausally, text, self, groupManager.getVectorClock(group), group);
        sendMessage(message, groupManager.getGroup(group).getMembers());
    }

    /**
     * returns true if the message has been received before, otherwise false.
     * @param message the message.
     * @return true if received before, else false.
     */
    public boolean hasReceived(Message message) {
        return (messageSorters.get(message.getGroupName()).hasMessageInHoldbackQueue(message) || groupManager.getVectorClock(message.getGroupName()).hasReceived(message));
    }

    /**
     * Joins the specified group.
     * @param groupName the group to join.
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public void joinGroup(String groupName) throws RemoteException, NotBoundException, MalformedURLException, UnknownHostException {
        Group group = new Group(groupName);
        messageSorters.put(groupName, new MessageSorter(deliveryQueue, group.getVectorClock()));
        groupManager.sendJoinGroup(group);
    }

    /**
     * This method is called if a dead host has been detected. Sends a viewChange to all other clients.
     * @param deadHost
     * @param groupName
     */
    public void triggerViewChange(Host deadHost, String groupName) {
        Group group = groupManager.getGroup(groupName);
        group.getMembers().remove(deadHost);
        sendViewChange(group);
    }

    /**
     * Creates the viewChange object and uses sendMessage to send it.
     * @param group the new view.
     */
    public void sendViewChange(Group group) {
        groupManager.getVectorClock(group.getName()).increment(self);
        ViewChange viewChange = new ViewChange(true, true, null, self, groupManager.getVectorClock(group.getName()), group.getName(), group.getMembers());
        sendMessage(viewChange, viewChange.getMembers());
    }

    private void sendMessage(Message message, ArrayList<Host> members){
        deliveryQueue.add(message);
        communicator.multicast(message, members);
    }

    public ArrayList<Host> getGroupMembers(String groupName) {
        return groupManager.getMembers(groupName);
    }

    /**
     * Is called when Communicator delivers a message. Checks if the message has already been received, if it has it is
     * delivered as an already received message. Otherwise, it is multicasted to all other clients if it is a reliable
     * message, then it is given to the message sorter.
     * @param message the received message from Communicator.
     */
    public void receive(Message message) {
        if(hasReceived(message)) {
            alreadyReceived(message);
        } else {
            if (message.isReliable()) {
                message.addToBeenAt(self);
                communicator.multicast(message, getGroupMembers(message.getGroupName()));
            }
            messageSorters.get(message.getGroupName()).receive(message);
        }
    }

    public void alreadyReceived(Message message) {
        gcomClient.deliverAlreadyReceivedMessage(message);
    }

    /**
     * Thread which takes from the deliveryQueue and delivers to the GComClient. If it is a viewChange,
     * it is processed.
     */
    @Override
    public void run() {
        while (running) {
            try {
                Message message = deliveryQueue.take();
                if(message instanceof ViewChange) {
                    System.out.println("received view change");
                    groupManager.processViewChange((ViewChange)message);
                } else {
                    gcomClient.deliverMessage(message);
                    gcomClient.debugSetVectorClock(groupManager.getGroup(message.getGroupName()).getVectorClock());
                }

            } catch (InterruptedException | RemoteException | MalformedURLException | NotBoundException | UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSleepMillisBetweenClients(int millis) {
        communicator.setSleepMillisBetweenClients(millis);
    }

}