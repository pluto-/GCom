package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by Patrik on 2014-10-03.
 */
public class RmiServerTest {

    public void testRemoteObject() throws Exception {
        RmiServer server = new RmiServer(1337);
        MyRemoteStuffs obj = new MyRemoteStuffs();
        RemoteStuffs stub = (RemoteStuffs) UnicastRemoteObject.exportObject(obj, 0);
        server.bind("test", stub);
        System.out.println(LocateRegistry.getRegistry(1337).list());
    }

    public static void main(String args[]) {
        RmiServerTest rmiServerTest = new RmiServerTest();
        try {
            rmiServerTest.testRemoteObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyRemoteStuffs implements RemoteStuffs {

        @Override
        public void test() throws RemoteException {
            System.out.println("TEST");
        }
    }
    private interface RemoteStuffs extends Remote {
        public void test() throws RemoteException;
    }
}
