package gcom.utils;


import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public interface PeerCommunication extends Remote {
    public void receiveMessage(Message message) throws RemoteException;
    public void addMember(Host newMember, String groupName) throws RemoteException, NotBoundException;
    public void viewChanged(Host newMember, String groupName) throws RemoteException;
}