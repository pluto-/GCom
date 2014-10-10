package gcom.communicator;

import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.PeerCommunication;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Patrik on 2014-10-10.
 */
public class CommunicationChannel implements Runnable {

    private Host host;
    private Thread thread;
    private BlockingQueue<Message> queue;
    private PeerCommunication remoteHost;
    private Communicator communicator;
    private final Logger logger = LogManager.getLogger(CommunicationChannel.class);

    public CommunicationChannel(Host host, Communicator communicator) throws RemoteException, NotBoundException, MalformedURLException {
        this.host = host;
        this.communicator = communicator;
        queue = new LinkedBlockingQueue<>();
//        Registry memberRegistry = LocateRegistry.getRegistry(host.getAddress().getHostAddress(), host.getPort());
//        remoteHost = (PeerCommunication) memberRegistry.lookup(PeerCommunication.class.getSimpleName());
        remoteHost = (PeerCommunication) Naming.lookup("rmi://" + host.getAddress().getHostAddress() + ":" + host.getPort() + "/" + PeerCommunication.class.getSimpleName());
        thread = new Thread(this);
        thread.start();
    }


    public void send(Message message) throws InterruptedException {
        queue.put(message);
    }

    @Override
    public void run() {
        Message message;
        boolean running = true;
        while(running) {
            message = null;
            try {
                message = queue.take();
            } catch (InterruptedException e) {
                running = false;
                e.printStackTrace();
            }

            try{
                remoteHost.receiveMessage(message);
            } catch (NoSuchObjectException e) {
                try {
                    remoteHost = (PeerCommunication) Naming.lookup("rmi://" + host.getAddress().getHostAddress() + ":" + host.getPort() + "/" + PeerCommunication.class.getSimpleName());
                    remoteHost.receiveMessage(message);
                } catch (NotBoundException | MalformedURLException | RemoteException e1) {
                    if(message != null) {
                        communicator.triggerViewChange(host, message.getGroupName());
                    }
                }
            } catch (RemoteException | NotBoundException e) {

                if (message != null) {
                    logger.error(e.getClass().getSimpleName() + " contacting: " + host + " for group: " + message.getGroupName() + " - triggering view change");
                    communicator.triggerViewChange(host, message.getGroupName());
                }
            }
        }
    }

    public void stop() {
        thread.interrupt();
    }
}
