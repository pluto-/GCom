package gcom.utils;


import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/** Is used when two clients communicate with each other. Has the remote method receiveMessage which is called by a
 * remote client to deliver a message.
 * Created by Jonas on 2014-10-03.
 */
public interface PeerCommunication extends Remote {
    public void receiveMessage(Message message) throws RemoteException, UnknownHostException;
}