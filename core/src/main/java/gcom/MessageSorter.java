package gcom;

import gcom.utils.HoldBackQueueListener;
import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.VectorClock;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Jonas on 2014-10-03.
 */
public class MessageSorter implements Runnable {

    private BlockingQueue<Message> deliverQueue;
    private volatile VectorClock localVectorClock;
    private volatile Map<Host,PriorityBlockingQueue<Message>> holdBackQueues;
    private Thread thread;
    private volatile boolean running;
    private HoldBackQueueListener listener;
    private HashMap<VectorClock, Message> causallyInconsistentlyDeliveredMessages;

    public MessageSorter(BlockingQueue<Message> deliverQueue, VectorClock localVectorClock) {
        this.deliverQueue = deliverQueue;
        this.localVectorClock = localVectorClock;
        holdBackQueues = new ConcurrentHashMap<>();
        this.listener = null;
        causallyInconsistentlyDeliveredMessages = new HashMap<>();
    }

    public void setListener(HoldBackQueueListener listener) {
        this.listener = listener;
    }

    public synchronized void receive(Message message) {
        if(message.deliverCausally()) {

            if (!holdBackQueues.containsKey(message.getSource())) {
                holdBackQueues.put(message.getSource(), new PriorityBlockingQueue<>(5, new MessageComparator()));
            }
            holdBackQueues.get(message.getSource()).put(message);
            if (listener != null) {
                listener.messagePutInHoldBackQueue(message);
            }
        } else {
            if(message.isCausallyConsistent(localVectorClock)) {
                deliverMessage(message);
            } else {
                try {
                    deliverQueue.put(message);
                    causallyInconsistentlyDeliveredMessages.put(message.getVectorClock(), message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        startThread();
    }

    private synchronized void startThread() {
        running = true;
        if(thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            thread.start();
        }
    }

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

    private synchronized void updateVectorClockCausality() {
        boolean delivered = false;
        for(VectorClock vectorClock : causallyInconsistentlyDeliveredMessages.keySet()) {
            Message message = causallyInconsistentlyDeliveredMessages.get(vectorClock);
            if(message.isCausallyConsistent(localVectorClock)) {
                causallyInconsistentlyDeliveredMessages.remove(message);
                incrementLocalVectorClock(message.getSource());
                delivered = true;
            }
        }
        if(delivered) {
            updateVectorClockCausality();
        }

    }

    private synchronized void incrementLocalVectorClock(Host host) {
        localVectorClock.increment(host);
    }

    @Override
    public void run() {
        while(running) {
            running = false;
            for(Host holdBackQueueHost : holdBackQueues.keySet()) {

                PriorityBlockingQueue<Message> holdBackQueue = holdBackQueues.get(holdBackQueueHost);

                Message firstMessage = holdBackQueue.peek();

                if((firstMessage != null) && firstMessage.isCausallyConsistent(localVectorClock)){

                    // Deliver

                    deliverMessage(firstMessage);
                    holdBackQueue.remove(firstMessage);
                    if(listener != null) {
                        listener.messageRemovedFromHoldBackQueue(firstMessage);
                    }
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
