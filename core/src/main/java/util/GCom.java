package util;

import rmi.RmiServer;
import transfer.Host;
import transfer.PeerCommunication;

import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class GCom implements PeerCommunication {
    
    private boolean reliableMulticast;
    private RmiServer rmiServer;

    private GroupManager groupManager;
    private Communicator communicator;
    private MessageSorter messageSorter;

    GComClient gcomClient;

    public GCom(boolean reliableMulticast, int rmiPort, GComClient gcomClient) throws RemoteException, UnknownHostException, AlreadyBoundException {
        this.reliableMulticast = reliableMulticast;
        rmiServer = new RmiServer(rmiPort);
        this.gcomClient = gcomClient;
    }

    public void multicast() {

    }
    
    @Override
    public void readMessage(Host sender, String Message, Map<Host, Integer> vectorClock) throws RemoteException {
        
    }

    @Override
    public void addMember(Host newMember) throws RemoteException {
        
    }

    @Override
    public void viewChanged(Host newMember) throws RemoteException {

    }
}
