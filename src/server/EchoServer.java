package server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * EchoServer - Demonstrates both TCP and UDP Echo protocols
 *
 * Features:
 * - TCP Echo for connection testing and heartbeat
 * - UDP Echo for latency measurement
 * - NIO-based implementation for efficiency
 * - Used for network diagnostics and keepalive
 *
 * Network Concepts: Echo Protocol, Heartbeat, Network Testing, NIO
 */
public class EchoServer {
    private static final Logger LOGGER = Logger.getLogger(EchoServer.class.getName());

    private int tcpPort;
    private int udpPort;
    private boolean running;

    // TCP Echo components
    private ServerSocket tcpServerSocket;
    private ExecutorService tcpThreadPool;

    // UDP Echo components
    private DatagramSocket udpSocket;
    private Thread udpThread;

    // Statistics
    private long tcpEchoCount;
    private long udpEchoCount;

    public EchoServer(int tcpPort, int udpPort) {
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        this.tcpThreadPool = Executors.newCachedThreadPool();
    }

    /**
     * Start Echo Server (both TCP and UDP)
     */
    public void start() throws IOException {
        running = true;
        startTCPEcho();
        startUDPEcho();
        LOGGER.info("Echo Server started - TCP: " + tcpPort + ", UDP: " + udpPort);
    }

    /**
     * Start TCP Echo Server
     * Echoes back any data received over TCP connection
     */
    private void startTCPEcho() {
        tcpThreadPool.execute(() -> {
            try {
                tcpServerSocket = new ServerSocket(tcpPort);
                LOGGER.info("TCP Echo Server listening on port " + tcpPort);

                while (running) {
                    Socket clientSocket = tcpServerSocket.accept();
                    tcpThreadPool.execute(() -> handleTCPClient(clientSocket));
                }

            } catch (IOException e) {
                if (running) {
                    LOGGER.severe("TCP Echo Server error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Handle TCP Echo client
     */
    private void handleTCPClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            LOGGER.info("TCP Echo client connected: " + clientSocket.getRemoteSocketAddress());

            String line;
            while ((line = in.readLine()) != null) {
                // Echo back the same message
                out.println(line);
                tcpEchoCount++;

                // Special commands
                if (line.equalsIgnoreCase("PING")) {
                    out.println("PONG");
                } else if (line.equalsIgnoreCase("TIME")) {
                    out.println("SERVER_TIME:" + System.currentTimeMillis());
                } else if (line.equalsIgnoreCase("STATS")) {
                    out.println("TCP_ECHOES:" + tcpEchoCount + ",UDP_ECHOES:" + udpEchoCount);
                } else if (line.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

        } catch (IOException e) {
            LOGGER.warning("TCP Echo client error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Start UDP Echo Server
     * Echoes back any datagram received
     */
    private void startUDPEcho() {
        udpThread = new Thread(() -> {
            try {
                udpSocket = new DatagramSocket(udpPort);
                LOGGER.info("UDP Echo Server listening on port " + udpPort);

                byte[] buffer = new byte[1024];

                while (running) {
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(receivePacket);

                    // Echo back to sender
                    DatagramPacket sendPacket = new DatagramPacket(
                        receivePacket.getData(),
                        receivePacket.getLength(),
                        receivePacket.getAddress(),
                        receivePacket.getPort()
                    );

                    udpSocket.send(sendPacket);
                    udpEchoCount++;

                    // Log every 100 echoes
                    if (udpEchoCount % 100 == 0) {
                        LOGGER.info("UDP Echo count: " + udpEchoCount);
                    }
                }

            } catch (IOException e) {
                if (running) {
                    LOGGER.severe("UDP Echo Server error: " + e.getMessage());
                }
            }
        });
        udpThread.start();
    }

    /**
     * Stop Echo Server
     */
    public void stop() {
        running = false;

        // Stop TCP
        try {
            if (tcpServerSocket != null && !tcpServerSocket.isClosed()) {
                tcpServerSocket.close();
            }
        } catch (IOException e) {
            LOGGER.warning("Error closing TCP server: " + e.getMessage());
        }

        tcpThreadPool.shutdown();

        // Stop UDP
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }

        if (udpThread != null) {
            udpThread.interrupt();
        }

        LOGGER.info("Echo Server stopped");
    }

    /**
     * Get statistics
     */
    public String getStats() {
        return String.format("TCP Echoes: %d, UDP Echoes: %d", tcpEchoCount, udpEchoCount);
    }

    public long getTcpEchoCount() {
        return tcpEchoCount;
    }

    public long getUdpEchoCount() {
        return udpEchoCount;
    }
}

