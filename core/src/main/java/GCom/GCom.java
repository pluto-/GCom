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
            throws RemoteException, UnknownHostException, AlreadyBoundException, MalformedURLException, NotBoundException {
        this.reliableMulticast = reliableMulticast;
        rmiServer = new RmiServer(rmiPort);
        this.gcomClient = gcomClient;
        this.groupManager = new GroupManager(nameService, rmiServer.getHost(), this);
    }

    public void multicast(String message, String group) {

    }
    
    public void addMember(String groupName, Host newMember) throws RemoteException, NotBoundException {
        groupManager.addMember(groupName, newMember);
    }

    public void viewChanged(String groupName, ArrayList<Host> members) throws RemoteException {
        groupManager.processViewChange(groupName, members);
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
