package gcom.utils;

import java.io.IOException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.security.Permission;

/** When an object of this class i created, it creates a local RMI registry on a specified port. It also has a
 * method for binding objects to the registry.
 * Created by Patrik on 2014-10-03.
 */
public class RmiServer {

    private Registry registry;
    private InetAddress externalIp;
    private int port;
    private Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Initializes and creates the local registry.
     * @param portNumber the port to use for the registry.
     * @throws IOException
     */
    public RmiServer(int portNumber) throws IOException {
        this.port = portNumber;
        logger.error("before security manager");
        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager() {
                public void checkConnect (String host, int port) {/*logger.error("connect");*/}
                public void checkConnect (String host, int port, Object context) {/*logger.error("connect");*/}
                public void checkPermission(Permission permission) {/*logger.error("connect");*/}
                public void checkPermission(Permission permission, Object context) {/*logger.error("connect");*/}
                public void checkAccept(String host, int port) {/*logger.error("connect");*/}
            });
        }
        logger.error("after security manager");
        System.setProperty("java.rmi.server.hostname", IpChecker.getIp());
        registry = java.rmi.registry.LocateRegistry.createRegistry(portNumber);
        externalIp = InetAddress.getByName(IpChecker.getIp());
        logger.error("constructor done");
    }

    /**
     * Rebind the object to the registry with specified name.
     * @param name the name.
     * @param object the object.
     * @throws RemoteException
     */
    public void bind(String name, Remote object) throws RemoteException {
        registry.rebind(name, object);
    }

    public Host getHost() {
        return new Host(externalIp, port);
    }

}
