package com.chemaxon.app;

import com.chemaxon.logging.remote.RemoteLogger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.UUID;

public class Client extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList list1;
    private JCheckBox checkBox1;
    private JTextField textField1;
    private JTextField textField2;

    public Client() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        list1.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                RemoteLogger.remoteLogger.error("list selection event: " + listSelectionEvent.toString());
            }
        });
    }

    private void onOK() {
        RemoteLogger.remoteLogger.error("pressed OK");
    }

    private void onCancel() {
        RemoteLogger.remoteLogger.error("pressed cancel");
        dispose();
    }

    public static void main(String[] args) {
        initializeRemoteLogging();

        Client dialog = new Client();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private static void initializeRemoteLogging() {
        String userDir = System.getProperty("user.dir");
        System.setProperty("org.apache.activemq.default.directory.prefix",userDir+ File.separator);
        System.out.println("userDir = " + userDir);
        final String clientId = "client id";
        final String sessionId = UUID.randomUUID().toString();

        final Thread loggerThread = RemoteLogger.start(clientId, sessionId);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                super.run();
                loggerThread.interrupt();
            }
        });
    }
}
