package gcom.utils;

/**
 * Created by Jonas on 2014-10-09.
 */
public interface HoldBackQueueListener {
    public void messagePutInHoldBackQueue(Message message);
    public void messageRemovedFromHoldBackQueue(Message message);
}
