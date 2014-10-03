package rmi;

import junit.framework.Test;
import org.junit.Assert.*;
import org.junit.Before;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;

/**
 * Created by Patrik on 2014-10-03.
 */
public class RmiServerTest {



    @org.junit.Test
    public void testRemoteObject() throws Exception {
        RmiServer server = new RmiServer(1337);
        assert(LocateRegistry.getRegistry() != null);
        LocateRegistry.getRegistry().bind("test", new RemoteStuffs());
        System.out.println(LocateRegistry.getRegistry().list());
    }

    private class RemoteStuffs implements Remote {
    }
}
