package gcom.utils;

/** Is used by the debug-client to listen for changes in the hold-back queues. Contains the methods
 * messagePutInHoldBackQueue and messageRemovedFromHoldBackQueue to listen for changes.
 * Created by Jonas on 2014-10-09.
 */
public interface HoldBackQueueListener {
    public void messagePutInHoldBackQueue(Message message);
    public void messageRemovedFromHoldBackQueue(Message message);
}
