package gcom.utils;

/** Every client which wants to receive messages from GCom when they are received from remote clients need to
 * implement the GComClient interface. It contains the deliverMessage which is called by GCom when delivering a
 * message to the GComClient. Some debug methods are also used by the debug client, these are:
 * deliverAlreadyReceivedMessage and debugSetVectorClock. These are used to give information about messages
 * already received and the local vector clock.
 * Created by Patrik on 2014-10-03.
 */
public interface GComClient {

    public void deliverMessage(Message message);
    public void deliverAlreadyReceivedMessage(Message message);
}
