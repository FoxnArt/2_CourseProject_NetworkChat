package ru.netology;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDateTime;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDR = "127.0.0.1";
    private static int port = -1;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader("settings.txt"))) {
            String string;
            String substring = "port";
            while ((string = br.readLine()) != null) {
                if (string.toLowerCase().contains(substring.toLowerCase())) {
                    String portString = string.substring(string.indexOf("'") + 1, string.lastIndexOf("'"));
                    port = Integer.parseInt(portString);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }

    private final JTextArea log = new JTextArea();
    private final JTextField fieldNickname = new JTextField("Name");
    private final JTextField fieldInput = new JTextField();

    private TCPConnection connection;

    private ClientWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        log.setEditable(false);
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);
        fieldInput.addActionListener(this);
        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickname, BorderLayout.NORTH);
        setVisible(true);

        try {
            connection = new TCPConnection(this, IP_ADDR, port);
        } catch (IOException e) {
            printMsg("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if (msg.equals("")) return;
        fieldInput.setText(null);
        connection.sendString(fieldNickname.getText() + ": " + msg);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e);
    }

    private synchronized void printMsg(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
                logger(msg);
            }
        });
    }

    private void logger(String value) {
        File file = new File(fieldNickname.getText() + ".txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fieldNickname.getText() + ".txt", true))) {
            writer.write(LocalDateTime.now() + ": " + value + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}