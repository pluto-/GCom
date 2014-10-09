package gcom.utils;

/**
 * Created by Patrik on 2014-10-03.
 */
public interface GComClient {

    public void deliverMessage(Message message);
    public void debugSetVectorClock(VectorClock vectorClock);
}
