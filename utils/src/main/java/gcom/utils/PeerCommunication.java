package gcom.utils;


import java.net.MalformedURLException;
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
    public void viewChanged(String groupName, ArrayList<Host> members) throws RemoteException, MalformedURLException, NotBoundException;
}