package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private ChatServer() {
        int port = -1;
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
        System.out.println("Server running");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while(true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnections("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnections(String value) {
        System.out.println(value);
        logger(value);
        for (TCPConnection tcpConnection : connections) {
            tcpConnection.sendString(value);
        }
    }

    private void logger(String value) {
        File file = new File("log.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true))) {
            writer.write(LocalDateTime.now() + ": " + value + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
