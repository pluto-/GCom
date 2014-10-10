package gcom.utils;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Patrik on 2014-10-07.
 */
public interface NameServiceClient extends Remote {

    public void addMember(String groupName, Host newMember) throws RemoteException, NotBoundException, MalformedURLException;
    public void setLeader(String groupName, Host leader) throws RemoteException;

}
