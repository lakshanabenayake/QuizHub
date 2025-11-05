package client;

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

/**
 * EchoTestClient - Client for testing Echo Server
 *
 * Features:
 * - TCP Echo test for connection verification
 * - UDP Echo test for latency measurement
 * - Heartbeat/keepalive mechanism
 *
 * Network Concepts: Echo Protocol, Latency Testing, Keepalive
 */
public class EchoTestClient {
    private static final Logger LOGGER = Logger.getLogger(EchoTestClient.class.getName());

    private String serverHost;
    private int tcpPort;
    private int udpPort;

    public EchoTestClient(String serverHost, int tcpPort, int udpPort) {
        this.serverHost = serverHost;
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
    }

    /**
     * Test TCP Echo - returns round-trip time in milliseconds
     */
    public long testTCPEcho(String message) {
        try (Socket socket = new Socket(serverHost, tcpPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            long startTime = System.currentTimeMillis();

            // Send message
            out.println(message);

            // Receive echo
            String response = in.readLine();

            long endTime = System.currentTimeMillis();
            long rtt = endTime - startTime;

            LOGGER.info("TCP Echo - Sent: " + message + ", Received: " + response + ", RTT: " + rtt + "ms");

            return rtt;

        } catch (IOException e) {
            LOGGER.severe("TCP Echo test failed: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Test UDP Echo - returns round-trip time in milliseconds
     */
    public long testUDPEcho(String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000); // 5 second timeout

            // Prepare send packet
            byte[] sendData = message.getBytes();
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            DatagramPacket sendPacket = new DatagramPacket(
                sendData, sendData.length, serverAddress, udpPort);

            long startTime = System.currentTimeMillis();

            // Send datagram
            socket.send(sendPacket);

            // Receive echo
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            long endTime = System.currentTimeMillis();
            long rtt = endTime - startTime;

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

            LOGGER.info("UDP Echo - Sent: " + message + ", Received: " + response + ", RTT: " + rtt + "ms");

            return rtt;

        } catch (IOException e) {
            LOGGER.severe("UDP Echo test failed: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Send PING command
     */
    public boolean ping() {
        try (Socket socket = new Socket(serverHost, tcpPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("PING");
            String response = in.readLine();

            boolean success = "PONG".equals(response);
            LOGGER.info("PING test: " + (success ? "SUCCESS" : "FAILED"));

            return success;

        } catch (IOException e) {
            LOGGER.severe("PING failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get server time
     */
    public long getServerTime() {
        try (Socket socket = new Socket(serverHost, tcpPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("TIME");
            String response = in.readLine();

            if (response != null && response.startsWith("SERVER_TIME:")) {
                long serverTime = Long.parseLong(response.substring(12));
                LOGGER.info("Server time: " + serverTime);
                return serverTime;
            }

        } catch (IOException | NumberFormatException e) {
            LOGGER.severe("Get server time failed: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Measure average latency with multiple tests
     */
    public double measureAverageLatency(int iterations) {
        long totalRTT = 0;
        int successCount = 0;

        LOGGER.info("Starting latency measurement with " + iterations + " iterations...");

        for (int i = 0; i < iterations; i++) {
            long rtt = testUDPEcho("LATENCY_TEST_" + i);
            if (rtt >= 0) {
                totalRTT += rtt;
                successCount++;
            }

            // Small delay between tests
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }

        if (successCount > 0) {
            double avgLatency = (double) totalRTT / successCount;
            LOGGER.info(String.format("Average latency: %.2f ms (%d/%d successful)",
                                     avgLatency, successCount, iterations));
            return avgLatency;
        }

        return -1;
    }

    /**
     * Run comprehensive connection test
     */
    public boolean runConnectionTest() {
        LOGGER.info("=== Running Connection Test ===");

        // Test 1: PING
        boolean pingSuccess = ping();

        // Test 2: TCP Echo
        long tcpRTT = testTCPEcho("Hello Server");

        // Test 3: UDP Echo
        long udpRTT = testUDPEcho("Hello Server UDP");

        // Test 4: Server time sync
        long serverTime = getServerTime();
        long localTime = System.currentTimeMillis();
        long timeDiff = Math.abs(serverTime - localTime);

        LOGGER.info("=== Test Results ===");
        LOGGER.info("PING: " + (pingSuccess ? "PASS" : "FAIL"));
        LOGGER.info("TCP RTT: " + (tcpRTT >= 0 ? tcpRTT + "ms" : "FAIL"));
        LOGGER.info("UDP RTT: " + (udpRTT >= 0 ? udpRTT + "ms" : "FAIL"));
        LOGGER.info("Time sync difference: " + timeDiff + "ms");

        return pingSuccess && tcpRTT >= 0 && udpRTT >= 0;
    }
}

