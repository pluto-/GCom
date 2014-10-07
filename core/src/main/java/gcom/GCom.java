package gcom;

import gcom.communicator.Communicator;
import gcom.groupmanager.GroupManager;
import gcom.utils.*;
import gcom.messagesorter.MessageSorter;

import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class GCom {
    
    private boolean reliableMulticast;
    private RmiServer rmiServer;

    private GroupManager groupManager;
    private Communicator communicator;
    private MessageSorter messageSorter;

    private GComClient gcomClient;

    public GCom(boolean reliableMulticast, int rmiPort, GComClient gcomClient, Host nameService)
            throws Exception {
        this.reliableMulticast = reliableMulticast;
        rmiServer = new RmiServer(rmiPort);
        this.gcomClient = gcomClient;
        this.groupManager = new GroupManager(nameService, rmiServer.getHost(), this);
        this.communicator = new Communicator(this);
        PeerCommunication stub = (PeerCommunication) UnicastRemoteObject.exportObject(this.communicator, rmiPort);
        rmiServer.bind(PeerCommunication.class.getSimpleName(), stub);
    }

    public void multicast(String text, String group) throws UnknownHostException, RemoteException, NotBoundException {
        // BTW: Vector Clock is not what it should be.
        Message message = new Message(false, text, rmiServer.getHost(), new VectorClock(), group);
        communicator.multicast(message, groupManager.getMembers(group));
    }
    
    public void addMember(String groupName, Host newMember) throws RemoteException, NotBoundException {
        groupManager.addMember(groupName, newMember);
    }

    public void viewChanged(String groupName, ArrayList<Host> members) throws RemoteException {
        groupManager.processViewChange(groupName, members);
    }

    public void joinGroup(String groupName) throws RemoteException {
        groupManager.joinGroup(groupName);
    }

    public void sendViewChange(Group group) throws RemoteException, NotBoundException {
        communicator.sendViewChange(group.getMembers(), group.getName());
    }

    public void sendLeaderElection(Group group) {
        //TODO fix
    }

    public ArrayList<Host> getGroupMembers(String groupName) {
        return groupManager.getMembers(groupName);
    }

    public void deliverMessage(String message) {
        gcomClient.deliverMessage(message);
    }
}
