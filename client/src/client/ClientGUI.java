package client;

import gcom.utils.VectorClock;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by Jonas on 2014-10-06.
 */
public class ClientGUI extends JFrame implements ActionListener, ItemListener {

    JTextPane chat;
    StyleContext context;
    StyledDocument document;
    Style style;

    JTextField message;
    JButton send;
    JButton sendSlow;
    JCheckBox sendReliably;
    JCheckBox orderCausally;
    JCheckBox cbDebug;
    Client client;
    String username;

    JMenuBar menuBar;
    JMenu debug;
    JCheckBoxMenuItem showLocalVectorClock;

    VectorClockGUI vectorClockGUI;

    /**
     * GUI look taken from: http://codereview.stackexchange.com/questions/25461/simple-chat-room-swing-gui
     */
    public ClientGUI(Client client) {

        this.client = client;

        username = JOptionPane.showInputDialog("Username: ");

        client.setGroupName(JOptionPane.showInputDialog("Group name: "));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel southPanel = new JPanel();
        southPanel.setBackground(Color.BLUE);
        southPanel.setLayout(new GridBagLayout());

        menuBar = new JMenuBar();
        debug = new JMenu("Debug");
        showLocalVectorClock = new JCheckBoxMenuItem("Show Local Vector Clock");
        showLocalVectorClock.addItemListener(this);
        debug.add(showLocalVectorClock);
        menuBar.add(debug);
        setJMenuBar(menuBar);

        message = new JTextField(30);
        message.requestFocusInWindow();

        send = new JButton("Send");
        send.addActionListener(this);

        sendSlow = new JButton("Send Slow");
        sendSlow.addActionListener(this);

        sendReliably = new JCheckBox("Send Reliably");
        orderCausally = new JCheckBox("Order Causally");
        cbDebug = new JCheckBox("Debug");

        chat = new JTextPane();
        chat.setEditable(false);
        chat.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        context = new StyleContext();
        document = new DefaultStyledDocument(context);
        style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontSize(style, 14);

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
        southPanel.add(sendSlow, right);
        southPanel.add(sendReliably, right);
        southPanel.add(orderCausally, right);
        southPanel.add(cbDebug, right);

        mainPanel.add(BorderLayout.SOUTH, southPanel);

        add(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 300);
        setVisible(true);

        vectorClockGUI = new VectorClockGUI();
    }

    public boolean isDebug() {
        return cbDebug.isSelected();
    }

    public void incomingMessage(String messageText, String debugText) {

        try {
            StyleConstants.setForeground(style, Color.black);
            document.insertString(document.getLength(), messageText,
                    style);
            StyleConstants.setForeground(style, Color.blue);
            document.insertString(document.getLength(), debugText,
                    style);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        chat.setStyledDocument(document);


        chat.setCaretPosition(chat.getDocument().getLength());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(send)) {
            if (message.getText().length() < 1) {
                // do nothing
            } else if (message.getText().equals(".clear")) {
                chat.setText("Cleared all messages\n");
                message.setText("");
            } else {
                try {
                    client.sendMessage(username + " > " + message.getText(), sendReliably.isSelected(), orderCausally.isSelected());
                } catch (NotBoundException e1) {
                    e1.printStackTrace();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
                message.setText("");
            }
            message.requestFocusInWindow();

        } else if(e.getSource().equals(sendSlow)) {
            if (message.getText().length() < 1) {
                // do nothing
            } else if (message.getText().equals(".clear")) {
                chat.setText("Cleared all messages\n");
                message.setText("");
            } else {
                int speed = Integer.valueOf(JOptionPane.showInputDialog("Speed: "));
                /*try {
                    client.sendMessage(message.getText());
                } catch (NotBoundException e1) {
                    e1.printStackTrace();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }*/
                message.setText("");
            }
            message.requestFocusInWindow();

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
        }
    }
}