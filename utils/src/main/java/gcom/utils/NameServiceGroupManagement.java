package gcom.utils;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Jonas on 2014-10-03.
 */
public interface NameServiceGroupManagement extends Remote {
    public Host joinGroup(String groupName, Host newMember) throws RemoteException, MalformedURLException, NotBoundException;
    public void removeGroup(String groupName) throws RemoteException;
    public void setLeader(String groupName, Host leader) throws RemoteException;
}
