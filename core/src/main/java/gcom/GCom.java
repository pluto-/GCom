package gcom;

import gcom.communicator.Communicator;
import gcom.groupmanager.GroupManager;
import gcom.utils.*;
import gcom.messagesorter.MessageSorter;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jonas on 2014-10-03.
 */
public class GCom implements Runnable {

    private boolean reliableMulticast;
    private RmiServer rmiServer;
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
        rmiServer = new RmiServer(rmiPort);
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

    public void leaveGroup(String group) throws RemoteException, NotBoundException, MalformedURLException {
        groupManager.leaveGroup(group);
    }

    public boolean hasReceived(Message message) {
        return (groupManager.getVectorClock(message.getGroupName()).hasReceived(message));
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

    public void sendToMessageSorter(Message message) {
        messageSorters.get(message.getGroupName()).receive(message);
    }

    public void receive(Message message) throws RemoteException, NotBoundException {

        if(message.isReliable()) {
            message.addToBeenAt(self);

            ArrayList<Host> members = getGroupMembers(message.getGroupName());
            communicator.multicast(message, members);
        }
        sendToMessageSorter(message);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Message message = deliveryQueue.take();
                if(message instanceof ViewChange) {
                    groupManager.processViewChange((ViewChange)message);
                } else {
                    gcomClient.deliverMessage(message);
                    gcomClient.debugSetVectorClock(groupManager.getGroup(message.getGroupName()).getVectorClock());
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
    }
}