package gcom.utils.nameserver;

import transfer.Host;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Jonas on 2014-10-03.
 */
public interface NameServiceGroupManagement extends Remote {
    public Host joinGroup(Host newMember, String groupName) throws RemoteException;
    public void removeGroup(String groupName) throws RemoteException;
}
