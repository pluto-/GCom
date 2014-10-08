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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jonas on 2014-10-03.
 */
public class GCom implements Runnable {

    private boolean reliableMulticast;
    private RmiServer rmiServer;
    private VectorClock vectorClock;
    private GroupManager groupManager;
    private Communicator communicator;
    private MessageSorter messageSorter;
    private boolean running;
    private Thread thread;
    private BlockingQueue<String> deliveryQueue;

    private GComClient gcomClient;

    public GCom(boolean reliableMulticast, int rmiPort, GComClient gcomClient, Host nameService)
            throws Exception {
        this.reliableMulticast = reliableMulticast;
        rmiServer = new RmiServer(rmiPort);
        this.gcomClient = gcomClient;
        groupManager = new GroupManager(nameService, rmiServer.getHost(), this);
        communicator = new Communicator(this);
        PeerCommunication stub = (PeerCommunication) UnicastRemoteObject.exportObject(communicator, rmiPort);
        rmiServer.bind(PeerCommunication.class.getSimpleName(), stub);
        NameServiceClient nameServiceClient = (NameServiceClient) UnicastRemoteObject.exportObject(groupManager, rmiPort);
        rmiServer.bind(NameServiceClient.class.getSimpleName(), nameServiceClient);

        deliveryQueue = new LinkedBlockingQueue<>();

        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void sendMessage(String text, String group, boolean sendReliably, boolean deliverCausally) throws UnknownHostException, RemoteException, NotBoundException {
        vectorClock.increment(rmiServer.getHost());
        Message message = new Message(sendReliably, deliverCausally, text, rmiServer.getHost(), vectorClock, group);

        communicator.multicast(message, groupManager.getMembers(group));
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

    public void viewChanged(String groupName, ArrayList<Host> members) throws RemoteException, MalformedURLException, NotBoundException {
        groupManager.processViewChange(groupName, members);
    }

    public boolean hasReceived(Message message) {
        return (groupManager.getVectorClock(message.getGroupName()).hasReceived(message));
    }

    public void joinGroup(String groupName) throws RemoteException, MalformedURLException, NotBoundException {
        groupManager.sendJoinGroup(groupName);
    }

    public void sendViewChange(Group group) throws RemoteException, NotBoundException, MalformedURLException {
        communicator.sendViewChange(group.getMembers(), group.getName());
    }

    public ArrayList<Host> getGroupMembers(String groupName) {
        return groupManager.getMembers(groupName);
    }

    public void deliverMessage(String message) {
        gcomClient.deliverMessage(message);
    }

    @Override
    public void run() {
        while (running) {
            try {
                gcomClient.deliverMessage(deliveryQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}