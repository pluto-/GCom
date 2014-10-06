package GCom;

import GCom.GCom.communicator.Communicator;
import GCom.GCom.groupmanager.GroupManager;
import GCom.communicator.Communicator;
import GCom.messagesorter.MessageSorter;
import sun.plugin2.message.Message;
import transfer.Host;

import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by Jonas on 2014-10-03.
 */
public class GCom {
    
    private boolean reliableMulticast;

    private GroupManager groupManager;
    private Communicator communicator;
    private MessageSorter messageSorter;

    GComClient gComClient;

    public GCom(boolean reliableMulticast, int rmiPort, GComClient gComClient) throws RemoteException, UnknownHostException, AlreadyBoundException {
        this.reliableMulticast = reliableMulticast;
        this.gComClient = gComClient;
        communicator = new Communicator(this, rmiPort);
    }

    public void deliverMessage(Message message) {
        gComClient
    }

    public ArrayList<Host> getGroupMembers(String groupName) {
        // TODO: Return members of group.
        return null;
    }

    public void addMemberInGroup(Host newMember, String groupName) {
        // TODO: Add member in group manager and tell GComClient that view has changed.
    }
}
