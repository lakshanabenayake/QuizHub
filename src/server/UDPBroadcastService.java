package server;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * UDPBroadcastService - Demonstrates UDP (User Datagram Protocol)
 *
 * Features:
 * - Connectionless communication for real-time notifications
 * - Low latency broadcasts (quiz alerts, timer updates)
 * - Multicast support for efficient group communication
 *
 * Network Concepts: UDP Sockets, Multicast, Datagram Packets
 */
public class UDPBroadcastService {
    private static final Logger LOGGER = Logger.getLogger(UDPBroadcastService.class.getName());

    private int udpPort;
    private DatagramSocket socket;
    private boolean running;
    private Thread listenerThread;
    private CopyOnWriteArrayList<InetSocketAddress> subscribedClients;

    // Multicast configuration
    private MulticastSocket multicastSocket;
    private InetAddress multicastGroup;
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int MULTICAST_PORT = 9999;

    public UDPBroadcastService(int udpPort) {
        this.udpPort = udpPort;
        this.subscribedClients = new CopyOnWriteArrayList<>();
    }

    /**
     * Start UDP broadcast service
     */
    public void start() throws IOException {
        socket = new DatagramSocket(udpPort);
        running = true;

        // Initialize multicast
        multicastSocket = new MulticastSocket(MULTICAST_PORT);
        multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
        multicastSocket.joinGroup(multicastGroup);

        LOGGER.info("UDP Broadcast Service started on port " + udpPort);
        LOGGER.info("Multicast group: " + MULTICAST_ADDRESS + ":" + MULTICAST_PORT);

        // Start listening for client subscriptions
        startListener();
    }

    /**
     * Listen for UDP subscription requests
     */
    private void startListener() {
        listenerThread = new Thread(() -> {
            byte[] buffer = new byte[1024];

            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());

                    if (message.startsWith("SUBSCRIBE")) {
                        InetSocketAddress clientAddress = new InetSocketAddress(
                            packet.getAddress(), packet.getPort());
                        subscribeClient(clientAddress);

                        // Send acknowledgment
                        sendUnicast("SUBSCRIBED", clientAddress);
                    }

                } catch (IOException e) {
                    if (running) {
                        LOGGER.warning("Error receiving UDP packet: " + e.getMessage());
                    }
                }
            }
        });
        listenerThread.start();
    }

    /**
     * Subscribe a client to broadcasts
     */
    public void subscribeClient(InetSocketAddress clientAddress) {
        if (!subscribedClients.contains(clientAddress)) {
            subscribedClients.add(clientAddress);
            LOGGER.info("Client subscribed: " + clientAddress);
        }
    }

    /**
     * Broadcast message to all subscribed clients (UDP)
     */
    public void broadcastToClients(String message) {
        byte[] data = message.getBytes();

        for (InetSocketAddress client : subscribedClients) {
            try {
                DatagramPacket packet = new DatagramPacket(
                    data, data.length, client.getAddress(), client.getPort());
                socket.send(packet);
            } catch (IOException e) {
                LOGGER.warning("Failed to send to " + client + ": " + e.getMessage());
            }
        }
    }

    /**
     * Multicast message to all clients in group
     */
    public void multicastMessage(String message) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                data, data.length, multicastGroup, MULTICAST_PORT);
            multicastSocket.send(packet);
            LOGGER.info("Multicast sent: " + message);
        } catch (IOException e) {
            LOGGER.severe("Multicast failed: " + e.getMessage());
        }
    }

    /**
     * Send unicast message to specific client
     */
    public void sendUnicast(String message, InetSocketAddress client) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                data, data.length, client.getAddress(), client.getPort());
            socket.send(packet);
        } catch (IOException e) {
            LOGGER.warning("Unicast failed to " + client + ": " + e.getMessage());
        }
    }

    /**
     * Broadcast quiz start notification (uses UDP for speed)
     */
    public void notifyQuizStart(String quizId) {
        String notification = "QUIZ_START|" + quizId + "|" + System.currentTimeMillis();
        broadcastToClients(notification);
        multicastMessage(notification);
    }

    /**
     * Broadcast time update (UDP is perfect for frequent updates)
     */
    public void notifyTimeUpdate(int timeRemaining) {
        String notification = "TIME_SYNC|" + timeRemaining;
        broadcastToClients(notification);
    }

    /**
     * Broadcast urgent alert
     */
    public void sendAlert(String alertMessage) {
        String notification = "ALERT|" + alertMessage;
        multicastMessage(notification);
    }

    /**
     * Stop the UDP service
     */
    public void stop() {
        running = false;

        if (multicastSocket != null) {
            try {
                multicastSocket.leaveGroup(multicastGroup);
                multicastSocket.close();
            } catch (IOException e) {
                LOGGER.warning("Error leaving multicast group: " + e.getMessage());
            }
        }

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        if (listenerThread != null) {
            listenerThread.interrupt();
        }

        LOGGER.info("UDP Broadcast Service stopped");
    }

    public int getSubscribedClientsCount() {
        return subscribedClients.size();
    }
}

