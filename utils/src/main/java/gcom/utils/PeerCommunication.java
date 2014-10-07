package gcom.utils;


import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public interface PeerCommunication extends Remote {
    public void receiveMessage(Message message) throws RemoteException;
    public void addMember(String groupName, Host newMember) throws RemoteException, NotBoundException;
    public void viewChanged(String groupName, ArrayList<Host> members) throws RemoteException;
    public void electLeader(String groupName) throws RemoteException;
}