package gcom.utils;


import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Jonas on 2014-10-03.
 */
public interface PeerCommunication extends Remote {
    public void receiveMessage(Message message) throws RemoteException;
}