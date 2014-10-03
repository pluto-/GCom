package util;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public interface MessageTransfer extends Remote {
    public void readMessage(Host sender, String Message, Map<Host, Integer> vectorClock) throws RemoteException;
    public void addMember(Host newMember) throws RemoteException;
    public void viewChanged(Host newMember) throws RemoteException;
}