package gcom;

import gcom.utils.*;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Jonas on 2014-10-03.
 */
public class MessageSorter implements Runnable {

    private BlockingQueue<Message> deliverQueue;
    private volatile ConcurrentMap<Host,PriorityBlockingQueue<Message>> holdBackQueues;
    private Thread thread;
    private volatile boolean running;
    private HoldBackQueueListener listener;
    private volatile ConcurrentHashMap<VectorClock, Message> causallyInconsistentlyDeliveredMessages;
    private final Group group;

    /**
     * Constructor
     * @param deliverQueue The delivery queue of the gcom client.
     * @param group the group associated with the message sorter.
     */
    public MessageSorter(BlockingQueue<Message> deliverQueue, Group group) {
        this.deliverQueue = deliverQueue;
        holdBackQueues = new ConcurrentHashMap<>();
        this.listener = null;
        causallyInconsistentlyDeliveredMessages = new ConcurrentHashMap<>();
        this.group = group;
    }

    public void setListener(HoldBackQueueListener listener) {
        this.listener = listener;
    }

    /**
     * Handles incoming messages.
     * If message is to be delivered causally, it is put in the holdback queue associated with the source host and
     * processed by the holdback queue thread. If no holdback queue exists for the source host, one is created.
     * If the message is to be delivered unordered, the message is put in the delivery queue to the gcom client and
     * the the message and incoming vectorclock is put in a temporary map containing messages delivered out of order
     * (unless the unordered message happens to be causally consistent, in which case the local vector clock is
     * increased).
     * The holdback queue processing thread is also started.
     *
     * @param message - the message to be received.
     */
    public synchronized void receive(Message message) {
        if(message.deliverCausally()) {

            if (!holdBackQueues.containsKey(message.getSource())) {
                holdBackQueues.putIfAbsent(message.getSource(), new PriorityBlockingQueue<>(5, new MessageComparator()));
            }
            holdBackQueues.get(message.getSource()).put(message);
            if (listener != null) {
                listener.messagePutInHoldBackQueue(message);
            }
        } else {
            if(message.isCausallyConsistent(group.getVectorClock())) {
                System.err.println("message causally consistent -> delivering");
                deliverMessage(message);
            } else {
                System.err.println("message not causally consistent -> inconsistently delivered");
                try {
                    deliverQueue.put(message);
                    causallyInconsistentlyDeliveredMessages.putIfAbsent(message.getVectorClock(), message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        startThread();
    }

    /**
     * Starts a new holdback queue processing thread if one is not currently started. If one is already running, it will
     * do another iteration through the holdback queues.
     */
    private synchronized void startThread() {
        running = true;
        if(thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Delivers a message into the delivery queue of the gcom client. Increments the entry for the source host in the
     * local vector clock and updates the local vector clock in case previously delivered unordered messages have now
     * been delivered in a causal way.
     * @param message
     */
    private synchronized void deliverMessage(Message message) {
        try {
            System.err.println("Delivering to deliverQueue");
            deliverQueue.put(message);
            incrementLocalVectorClock(message.getSource());
            updateVectorClockCausality();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startThread();
    }

    /**
     * Updates the local vector clock if possible with previously delivered unordered message according to causality.
     */
    private synchronized void updateVectorClockCausality() {
        boolean delivered = false;
        for(VectorClock vectorClock : causallyInconsistentlyDeliveredMessages.keySet()) {
            Message message = causallyInconsistentlyDeliveredMessages.get(vectorClock);
            if(message.isCausallyConsistent(group.getVectorClock())) {
                causallyInconsistentlyDeliveredMessages.remove(message.getVectorClock());
                incrementLocalVectorClock(message.getSource());
                delivered = true;
            }
        }
        if(delivered) {
            updateVectorClockCausality();
        }

    }

    private synchronized void incrementLocalVectorClock(Host host) {
        group.getVectorClock().increment(host);
    }

    /**
     * Holdback queues processing thread
     */
    @Override
    public void run() {
        while(running) {
            running = false;
            for(Host holdBackQueueHost : holdBackQueues.keySet()) {

                PriorityBlockingQueue<Message> holdBackQueue = holdBackQueues.get(holdBackQueueHost);

                Message firstMessage = holdBackQueue.peek();
                if(firstMessage != null)
                    System.out.print("holdback queue processing: " + firstMessage.getSource() + " " + firstMessage.getText() + "... ");
                if((firstMessage != null) && firstMessage.isCausallyConsistent(group.getVectorClock())){

                    // Deliver

                    holdBackQueue.remove(firstMessage);
                    deliverMessage(firstMessage);
                    System.out.println("succeeded!");
                    if(listener != null) {
                        listener.messageRemovedFromHoldBackQueue(firstMessage);
                    }
                } else if (firstMessage != null) {
                    System.out.println("failed due to: message not causally consistent");
                    System.out.println("local vectorClock: " + group.getVectorClock() + " message vectorClock: " + firstMessage.getVectorClock());
                }
            }
        }
    }

    public boolean hasMessageInHoldbackQueue(Message message) {
        PriorityBlockingQueue<Message> holdBackQueue = holdBackQueues.get(message.getSource());
        return (holdBackQueue!= null && holdBackQueue.contains(message));
    }

    private class MessageComparator implements Comparator<Message>
    {

        @Override
        public int compare(Message first, Message second)
        {
            if (first.getVectorClock().isBefore(second.getVectorClock(), first.getSource()))
            {
                return -1;
            }
            if (second.getVectorClock().isBefore(first.getVectorClock(), first.getSource()))
            {
                return 1;
            }
            return 0;
        }
    }
}
