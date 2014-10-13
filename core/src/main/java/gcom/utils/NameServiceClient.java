package gcom.utils;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Is used by the GroupManager class to add members and set leaders.
 * Created by Patrik on 2014-10-07.
 */
public interface NameServiceClient extends Remote {

    public void addMember(String groupName, Host newMember) throws RemoteException;
    public void setLeader(String groupName, Host leader) throws RemoteException;

}
