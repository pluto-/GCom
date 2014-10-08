package gcom.messagesorter;

import gcom.GCom;
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
    private VectorClock localVectorClock;
    private Map<Host,PriorityBlockingQueue<Message>> holdBackQueues;

    public MessageSorter(BlockingQueue<Message> deliverQueue, VectorClock localVectorClock) {
        this.deliverQueue = deliverQueue;
        this.localVectorClock = localVectorClock;
        holdBackQueues = new ConcurrentHashMap<>();
    }

    public void receive(Message message) {

        if(!holdBackQueues.containsKey(message.getSender())) {
            holdBackQueues.put(message.getSender(), new PriorityBlockingQueue<>(5, new MessageComparator()));
        }
        holdBackQueues.get(message.getSender()).add(message);

        startThread();
    }

    private void startThread() {
        Thread thread = new Thread(this);
        thread.start();
    }

    private void incrementLocalVectorClock(Host host) {
        localVectorClock.increment(host);
    }

    @Override
    public void run() {
        boolean running = true;
        while(running) {

            running = false;
            Iterator<Host> keys = holdBackQueues.keySet().iterator();
            Host key;
            while(keys.hasNext()) {
                key = keys.next();

                PriorityBlockingQueue<Message> holdBackQueue = holdBackQueues.get(key);

                Message firstMessage = holdBackQueue.peek();

                if(firstMessage == null) {}
                else if((firstMessage.getVectorClock().getValue(key) == (localVectorClock.getValue(key) + 1)) &&
                        firstMessage.getVectorClock().isBeforeOrEqualOnAllValuesExcept(localVectorClock, key)){

                    // Deliver
                    deliverQueue.add(firstMessage);
                    incrementLocalVectorClock(key);
                    holdBackQueue.remove();
                    running = true;
                }

            }
        }
    }

    private class MessageComparator implements Comparator<Message>
    {

        @Override
        public int compare(Message first, Message second)
        {
            if (first.getVectorClock().isBefore(second.getVectorClock(), first.getSender()))
            {
                return -1;
            }
            if (second.getVectorClock().isBefore(first.getVectorClock(), first.getSender()))
            {
                return 1;
            }
            return 0;
        }
    }
}
