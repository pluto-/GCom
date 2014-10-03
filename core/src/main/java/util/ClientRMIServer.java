package src.main.java.util;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public class ClientRMIServer implements util.MessageTransfer {
    
    private boolean reliableMulticast;

    public ClientRMIServer(boolean reliableMulticast) {
        this.reliableMulticast = reliableMulticast;
    }
    
    @Override
    public void readMessage(util.Host sender, String Message, Map<util.Host, Integer> vectorClock) throws RemoteException {
        
    }
}
