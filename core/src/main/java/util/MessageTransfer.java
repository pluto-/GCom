package util;

import java.rmi.Remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Created by Jonas on 2014-10-03.
 */
public interface MessageTransfer extends Remote {
    public void readMessage(util.Host sender, String Message, Map<util.Host, Integer> vectorClock) throws RemoteException;
}