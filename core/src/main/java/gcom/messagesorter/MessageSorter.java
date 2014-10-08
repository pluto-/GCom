package gcom.messagesorter;

import gcom.GCom;
import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.VectorClock;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Jonas on 2014-10-03.
 */
public class MessageSorter implements Runnable {

    private GCom gCom;
    private Map<Host,PriorityBlockingQueue<Message>> holdBackQueues;
    private String group;

    public MessageSorter(GCom gCom, String group) {
        this.gCom = gCom;
        this.group = group;
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

    private VectorClock getLocalVectorClock() {
        return gCom.getVectorClock(group);
    }

    private void incrementLocalVectorClock(Host host) {
        gCom.incrementVectorClock(group, host);
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

                if((firstMessage.getVectorClock().getValue(key) == (getLocalVectorClock().getValue(key) + 1)) &&
                        firstMessage.getVectorClock().isBeforeOrEqualOnAllValuesExcept(getLocalVectorClock(), key)){

                    // Deliver
                    gCom.deliverMessage(firstMessage.getText());
                    incrementLocalVectorClock(key);
                    holdBackQueue.remove();
                    running = true;
                }

            }
        }
    }
}
