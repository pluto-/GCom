package gcom;

import gcom.communicator.Communicator;
import gcom.utils.*;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jonas on 2014-10-03.
 */
public class GCom implements Runnable {

    private boolean reliableMulticast;
    private GroupManager groupManager;
    private Communicator communicator;
    private Map<String, MessageSorter> messageSorters;
    private boolean running;
    private Thread thread;
    private BlockingQueue<Message> deliveryQueue;
    private Host self;

    private GComClient gcomClient;

    public GCom(boolean reliableMulticast, int rmiPort, GComClient gcomClient, Host nameService)
            throws Exception {
        this.reliableMulticast = reliableMulticast;
        RmiServer rmiServer = new RmiServer(rmiPort);
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

    public void attachHoldBackQueueListener(HoldBackQueueListener listener, String groupName) {
        messageSorters.get(groupName).setListener(listener);
    }

    public void sendMessage(String text, String group, boolean sendReliably, boolean deliverCausally) {
        groupManager.getVectorClock(group).increment(self);
        Message message = new Message(sendReliably, deliverCausally, text, self, groupManager.getVectorClock(group), group);
        sendMessage(message, groupManager.getGroup(group).getMembers());
    }

    public VectorClock getVectorClock(String groupName) {
        return groupManager.getVectorClock(groupName);
    }

    public void incrementVectorClock(String groupName, Host host) {
        groupManager.getVectorClock(groupName).increment(host);
    }

    public void leaveGroup(String group) throws RemoteException {
        groupManager.leaveGroup(group);
    }

    public boolean hasReceived(Message message) {
        return (messageSorters.get(message.getGroupName()).hasMessageInHoldbackQueue(message) || groupManager.getVectorClock(message.getGroupName()).hasReceived(message));
    }

    public void joinGroup(String groupName) throws RemoteException, NotBoundException, MalformedURLException {
        Group group = new Group(groupName);
        messageSorters.put(groupName, new MessageSorter(deliveryQueue, group.getVectorClock()));
        groupManager.sendJoinGroup(group);
    }

    public void triggerViewChange(Host deadHost, String groupName) {
        Group group = groupManager.getGroup(groupName);
        group.getMembers().remove(deadHost);
        sendViewChange(group);
    }

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

    public void receive(Message message) {
        if(hasReceived(message)) {
            alreadyReceived(message);
        } else {
            if (message.isReliable()) {
                message.addToBeenAt(self);
                communicator.multicast(message, getGroupMembers(message.getGroupName());
            }

            if (message.deliverCausally()) {
                messageSorters.get(message.getGroupName()).receive(message);

            } else {
                deliveryQueue.add(message);
            }
        }
    }

    public void alreadyReceived(Message message) {
        gcomClient.deliverAlreadyReceivedMessage(message);
    }

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

            } catch (InterruptedException | RemoteException | MalformedURLException | NotBoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSleepMillisBetweenClients(int millis) {
        communicator.setSleepMillisBetweenClients(millis);
    }

}