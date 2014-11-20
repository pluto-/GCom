package gcom.client;

import gcom.utils.Host;
import gcom.utils.Message;
import gcom.utils.ViewChange;

import javax.swing.*;
import java.util.List;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** If enabled, shows a new window containing information about the hold-back queue.
 * Created by Jonas on 2014-10-09.
 */
public class HoldBackQueueGUI extends JFrame {

    private JTextArea holdBackQueueText;
    private List<Message> messages;

    public HoldBackQueueGUI() {
        super("Hold-Back Queue");
        setLayout(new BorderLayout());
        messages = Collections.synchronizedList(new ArrayList<Message>());

        JLabel label = new JLabel("Hold-Back Queue");
        holdBackQueueText = new JTextArea("");
        holdBackQueueText.setEditable(false);

        add(label, BorderLayout.PAGE_START);
        add(new JScrollPane(holdBackQueueText), BorderLayout.CENTER);

        setSize(300, 300);
    }

    public void addMessage(Message message) {
        System.err.println("Adding " + message.getText() + " in holdback queue GUI.");
        messages.add(message);
        updateText();
    }

    public void removeMessage(Message message) {
        System.err.println("Removing " + message.getText() + " from holdback queue GUI.");
        messages.remove(message);
        updateText();
    }

    private void updateText() {
        synchronized(messages) {
            String text = "";

            for (Message message : messages) {
                if (message instanceof ViewChange) {
                    text = text.concat("\nView Change from Host: " + message.getSource());
                    text = text.concat("\n\tVector Clock");
                    for (Host member : message.getVectorClock().getClock().keySet()) {
                        text = text.concat("\n\tHost: " + member + " Clock: " + message.getVectorClock().getValue(member));

                    }
                } else {
                    text = text.concat("\nHost: " + message.getSource() + " Message: " + message.getText());
                    text = text.concat("\n\tVector Clock");
                    for (Host member : message.getVectorClock().getClock().keySet()) {
                        text = text.concat("\n\tHost: " + member + " Clock: " + message.getVectorClock().getValue(member));

                    }

                }
            }
            holdBackQueueText.setText(text);
        }
    }
}
