package client;

import java.io.IOException;
import java.net.*;
import java.util.logging.Logger;

/**
 * UDPClient - Client-side UDP receiver
 *
 * Features:
 * - Subscribe to UDP broadcasts from server
 * - Receive real-time notifications
 * - Multicast group membership
 *
 * Network Concepts: UDP Client, Multicast, Datagram Reception
 */
public class UDPClient {
    private static final Logger LOGGER = Logger.getLogger(UDPClient.class.getName());

    private String serverHost;
    private int udpPort;
    private DatagramSocket socket;
    private boolean running;
    private Thread listenerThread;
    private UDPMessageCallback callback;

    // Multicast
    private MulticastSocket multicastSocket;
    private InetAddress multicastGroup;
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int MULTICAST_PORT = 9999;

    public interface UDPMessageCallback {
        void onUDPMessage(String message);
    }

    public UDPClient(String serverHost, int udpPort, UDPMessageCallback callback) {
        this.serverHost = serverHost;
        this.udpPort = udpPort;
        this.callback = callback;
    }

    /**
     * Start UDP client and subscribe to server broadcasts
     */
    public void start() throws IOException {
        // Create socket for receiving broadcasts
        socket = new DatagramSocket();

        // Join multicast group
        multicastSocket = new MulticastSocket(MULTICAST_PORT);
        multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
        multicastSocket.joinGroup(multicastGroup);

        running = true;

        // Subscribe to server
        subscribeToServer();

        // Start listening
        startListener();

        LOGGER.info("UDP Client started, subscribed to " + serverHost + ":" + udpPort);
    }

    /**
     * Send subscription request to server
     */
    private void subscribeToServer() throws IOException {
        String message = "SUBSCRIBE";
        byte[] data = message.getBytes();

        InetAddress serverAddress = InetAddress.getByName(serverHost);
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, udpPort);

        socket.send(packet);
        LOGGER.info("Subscription request sent to server");
    }

    /**
     * Listen for UDP messages
     */
    private void startListener() {
        // Listen to unicast messages
        listenerThread = new Thread(() -> {
            byte[] buffer = new byte[1024];

            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());

                    if (callback != null) {
                        callback.onUDPMessage(message);
                    }

                    LOGGER.info("UDP received: " + message);

                } catch (IOException e) {
                    if (running) {
                        LOGGER.warning("Error receiving UDP: " + e.getMessage());
                    }
                }
            }
        });
        listenerThread.start();

        // Listen to multicast messages
        Thread multicastThread = new Thread(() -> {
            byte[] buffer = new byte[1024];

            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());

                    if (callback != null) {
                        callback.onUDPMessage("[MULTICAST] " + message);
                    }

                    LOGGER.info("Multicast received: " + message);

                } catch (IOException e) {
                    if (running) {
                        LOGGER.warning("Error receiving multicast: " + e.getMessage());
                    }
                }
            }
        });
        multicastThread.start();
    }

    /**
     * Send heartbeat/ping
     */
    public void sendHeartbeat() {
        try {
            String message = "HEARTBEAT|" + System.currentTimeMillis();
            byte[] data = message.getBytes();

            InetAddress serverAddress = InetAddress.getByName(serverHost);
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, udpPort);

            socket.send(packet);
        } catch (IOException e) {
            LOGGER.warning("Failed to send heartbeat: " + e.getMessage());
        }
    }

    /**
     * Stop UDP client
     */
    public void stop() {
        running = false;

        try {
            if (multicastSocket != null) {
                multicastSocket.leaveGroup(multicastGroup);
                multicastSocket.close();
            }
        } catch (IOException e) {
            LOGGER.warning("Error leaving multicast: " + e.getMessage());
        }

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        if (listenerThread != null) {
            listenerThread.interrupt();
        }

        LOGGER.info("UDP Client stopped");
    }
}

