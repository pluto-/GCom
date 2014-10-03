package src.main.java.util;

import transfer.Host;
import transfer.MessageTransfer;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class ClientRMIServer implements MessageTransfer {
    
    private boolean reliableMulticast;

    public ClientRMIServer(boolean reliableMulticast) {
        this.reliableMulticast = reliableMulticast;
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
