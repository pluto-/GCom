package gcom.communicator;

import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.PeerCommunication;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** An internal queue in this class is holding the messages which are to be sent to a remote host. These are
 * sent by a thread through the PeerCommunication interface (which the remote host implements in it's
 * Communicator class).
 * Created by Patrik on 2014-10-10.
 */
public class CommunicationChannel implements Runnable {

    private final Host host;
    private Thread thread;
    private final BlockingQueue<Message> queue;
    private PeerCommunication remoteHost;
    private final Communicator communicator;
    private final Logger logger = LogManager.getLogger(CommunicationChannel.class);

    /**
     * Initializes, connects to the remote client and starts the thread.
     * @param host the remote client.
     * @param communicator The communicator which created this channel.
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public CommunicationChannel(Host host, Communicator communicator) throws RemoteException, NotBoundException, MalformedURLException {
        this.host = host;
        this.communicator = communicator;
        queue = new LinkedBlockingQueue<>();
        remoteHost = (PeerCommunication) Naming.lookup("rmi://" + host.getAddress().getHostAddress() + ":" + host.getPort() + "/" + PeerCommunication.class.getSimpleName());
        thread = new Thread(this);
        thread.start();
    }


    /**
     * Puts the message in the queue and will later be sent.
     * @param message the message.
     * @throws InterruptedException
     */
    public void send(Message message) throws InterruptedException {
        queue.put(message);
    }

    /**
     * This thread will attempt to take messages from the queue and call the remote hosts receiveMessage to
     * deliver the messages. In case of a NoSuchObjectException, which happens if the host process has restarted since
     * the remoteHost was bound, one attempt at rebinding the remoteHost and resending the message will be done. If this
     * fails, or the host cannot be reached, the host is considered dead and removed from the group and a viewChange
     * will be sent to all other clients in group.
     */
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
                logger.error("Interrupted exception while taking message from queue");
            }

            try{
                remoteHost.receiveMessage(message);
            } catch (RemoteException e) {
                try {
                    remoteHost = (PeerCommunication) Naming.lookup("rmi://" + host.getAddress().getHostAddress() + ":" + host.getPort() + "/" + PeerCommunication.class.getSimpleName());
                    remoteHost.receiveMessage(message);
                } catch (NotBoundException | MalformedURLException | RemoteException e1) {
                    if(message != null) {
                        logger.error(e.getClass().getSimpleName() + " contacting: " + host + " for group: " + message.getGroupName() + " - triggering view change");
                        try {
                            communicator.triggerViewChange(host, message.getGroupName());
                        } catch (UnknownHostException e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        thread.interrupt();
    }
}
