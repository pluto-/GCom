package gcom.utils;

import java.io.Serializable;
import java.util.ArrayList;

/** Objects of this class is sent between clients when communicating. Contains the following information: If the
 * message is to be sent reliably, if the message is to be delivered causally, the text, the sender, the vector clock
 * of the message, the name of the group which is to be sent to and a list containing all the addresses of the clients
 * which the message has passed through.
 * Created by Jonas on 2014-10-06.
 */
public class Message implements Serializable {

    private final boolean isReliable;
    private final boolean deliverCausally;
    private String text;
    private final Host source;
    private final VectorClock vectorClock;
    private final String groupName;
    private final ArrayList<Host> beenAt;

    public Message(boolean isReliable, boolean deliverCausally, String text, Host source, VectorClock vectorClock, String groupName) {
        this.isReliable = isReliable;
        this.deliverCausally = deliverCausally;
        this.text = text;
        this.source = source;
        this.vectorClock = new VectorClock(vectorClock);
        this.groupName = groupName;
        beenAt = new ArrayList<>();
    }

    public void addToBeenAt(Host host) {
        beenAt.add(host);
    }

    public ArrayList<Host> getBeenAt() {
        return beenAt;
    }

    public boolean deliverCausally() {
        return deliverCausally;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getGroupName() {
        return groupName;
    }

    public VectorClock getVectorClock() {
        return vectorClock;
    }

    public boolean isReliable() {
        return isReliable;
    }

    public String getText() {
        return text;
    }

    public Host getSource() {
        return source;
    }

    public boolean isCausallyConsistent(VectorClock localVectorClock) {
        return (localVectorClock.isEmpty() || (vectorClock.getValue(source) == (localVectorClock.getValue(source) + 1) &&
                getVectorClock().isBeforeOrEqualOnAllValuesExcept(localVectorClock, source)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (deliverCausally != message.deliverCausally) return false;
        if (isReliable != message.isReliable) return false;
        if (groupName != null ? !groupName.equals(message.groupName) : message.groupName != null) return false;
        if (source != null ? !source.equals(message.source) : message.source != null) return false;
        if (text != null ? !text.equals(message.text) : message.text != null) return false;
        if (vectorClock != null ? !vectorClock.equals(message.vectorClock) : message.vectorClock != null) return false;

        return true;
    }

}
