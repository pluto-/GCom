package client;

import gcom.utils.Message;
import gcom.utils.VectorClock;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/** Shows the test/debug graphical user interface. Acts as View in MVC design pattern.
 * Created by Jonas on 2014-10-06.
 */
public class ClientGUI extends JFrame implements ActionListener, ItemListener {

    private JTextPane chat;
    private StyleContext context;
    private StyledDocument document;

    private Style blackStyle;
    private Style blueStyle;
    private Style redStyle;

    private JTextField message;
    private JButton send;
    private JCheckBox sendReliably;
    private JCheckBox orderCausally;
    private JCheckBox cbDebug;
    private Client client;
    private String username;

    private JMenuBar menuBar;
    private JMenu debug;
    private JCheckBoxMenuItem showLocalVectorClock;
    private JCheckBoxMenuItem showHoldBackQueue;
    private JMenuItem setMulticastSleep;

    private VectorClockGUI vectorClockGUI;
    private HoldBackQueueGUI holdBackQueueGUI;

    /**
     * GUI look taken from: http://codereview.stackexchange.com/questions/25461/simple-chat-room-swing-gui
     */
    public ClientGUI(Client client) {

        this.client = client;

        username = JOptionPane.showInputDialog("Username: ");

        String groupName = JOptionPane.showInputDialog("Group name: ");
        client.setGroupName(groupName);
        this.setTitle("Group: " + groupName + " | Username: " + username);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel southPanel = new JPanel();
        southPanel.setBackground(Color.BLUE);
        southPanel.setLayout(new GridBagLayout());

        menuBar = new JMenuBar();
        debug = new JMenu("Debug");
        showLocalVectorClock = new JCheckBoxMenuItem("Show Local Vector Clock");
        showLocalVectorClock.addItemListener(this);
        showHoldBackQueue = new JCheckBoxMenuItem("Show Hold-Back Queue");
        showHoldBackQueue.addItemListener(this);
        setMulticastSleep = new JMenuItem("Set Multicast Sleep");
        setMulticastSleep.addActionListener(this);
        debug.add(showLocalVectorClock);
        debug.add(showHoldBackQueue);
        debug.add(setMulticastSleep);
        menuBar.add(debug);
        setJMenuBar(menuBar);

        message = new JTextField(30);
        message.requestFocusInWindow();
        message.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e){

                send();

            }});

        send = new JButton("Send");
        send.addActionListener(this);

        sendReliably = new JCheckBox("Send Reliably");
        orderCausally = new JCheckBox("Order Causally");
        cbDebug = new JCheckBox("Debug Output");

        chat = new JTextPane();
        chat.setEditable(false);
        chat.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        context = new StyleContext();
        document = new DefaultStyledDocument(context);

        blackStyle = chat.addStyle("BlackStyle", null);
        StyleConstants.setFontSize(blackStyle, 14);
        StyleConstants.setForeground(blackStyle, Color.black);

        blueStyle = chat.addStyle("BlueStyle", null);
        StyleConstants.setFontSize(blueStyle, 14);
        StyleConstants.setForeground(blueStyle, Color.blue);

        redStyle = chat.addStyle("RedStyle", null);
        StyleConstants.setFontSize(redStyle, 14);
        StyleConstants.setForeground(redStyle, Color.red);

        mainPanel.add(new JScrollPane(chat), BorderLayout.CENTER);

        GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.LINE_START;
        left.fill = GridBagConstraints.HORIZONTAL;
        left.weightx = 512.0D;
        left.weighty = 1.0D;

        GridBagConstraints right = new GridBagConstraints();
        right.insets = new Insets(0, 10, 0, 0);
        right.anchor = GridBagConstraints.LINE_END;
        right.fill = GridBagConstraints.NONE;
        right.weightx = 1.0D;
        right.weighty = 1.0D;

        southPanel.add(message, left);
        southPanel.add(send, right);
        southPanel.add(sendReliably, right);
        southPanel.add(orderCausally, right);
        southPanel.add(cbDebug, right);

        mainPanel.add(BorderLayout.SOUTH, southPanel);

        add(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 300);
        setVisible(true);

        vectorClockGUI = new VectorClockGUI();
        holdBackQueueGUI = new HoldBackQueueGUI();
    }

    public boolean isDebug() {
        return cbDebug.isSelected();
    }

    public void messagePutInHoldBackQueue(Message message) {
        holdBackQueueGUI.addMessage(message);
    }
    public void messageRemovedFromHoldBackQueue(Message message) {
        holdBackQueueGUI.removeMessage(message);
    }


    public void incomingMessage(String messageText, String debugText) {

        try {
            document.insertString(document.getLength(), messageText,
                    blackStyle);
            document.insertString(document.getLength(), debugText,
                    blueStyle);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        chat.setStyledDocument(document);


        chat.setCaretPosition(chat.getDocument().getLength());
    }

    public void incomingMessageAlreadyReceived(String messageText) {

        try {
            document.insertString(document.getLength(), "[Already received message] " + messageText + "\n",
                    redStyle);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        chat.setStyledDocument(document);

        chat.setCaretPosition(chat.getDocument().getLength());
    }

    private void send() {
        if(message.getText().length() > 0) {
            if (message.getText().equals(".clear")) {
                chat.setText("Cleared all messages\n");
                message.setText("");
            } else {
                client.sendMessage(username + " > " + message.getText(), sendReliably.isSelected(), orderCausally.isSelected());
                message.setText("");
            }
        }
        message.requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(send)) {
            send();

        } else if(e.getSource().equals(setMulticastSleep)) {
            int sleepMillis = Integer.valueOf(JOptionPane.showInputDialog("Specify how many milliseconds GCom should sleep between the clients when sending a multicast."));
            client.setSleepMillisBetweenClients(sleepMillis);
        }
    }

    public void vectorClockChanged(VectorClock newLocalVectorClock) {
        vectorClockGUI.setVectorClockText(newLocalVectorClock);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if(e.getSource().equals(showLocalVectorClock)) {
            boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
            vectorClockGUI.setVisible(selected);
        } else if(e.getSource().equals(showHoldBackQueue)) {
            boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
            holdBackQueueGUI.setVisible(selected);
        }
    }
}