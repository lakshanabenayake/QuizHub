package demo;

import server.*;
import client.*;
import common.BufferManager;
import common.Protocol;

import java.io.IOException;
import java.util.Scanner;

/**
 * NetworkFeaturesDemo - Demonstrates all advanced network programming features
 *
 * This class showcases:
 * 1. UDP Broadcasting (connectionless communication)
 * 2. Java NIO Chat Server (non-blocking I/O with Selector)
 * 3. Echo Server (TCP & UDP for testing/diagnostics)
 * 4. ByteBuffer management (efficient data handling)
 *
 * Run this to see all network features in action!
 */
public class NetworkFeaturesDemo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║     QuizHub - Network Programming Features Demo         ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Select demo to run:");
        System.out.println("1. Start All Servers (UDP + NIO Chat + Echo)");
        System.out.println("2. UDP Broadcast Demo");
        System.out.println("3. NIO Chat Server Demo");
        System.out.println("4. Echo Server Demo");
        System.out.println("5. ByteBuffer Management Demo");
        System.out.println("6. Complete Integration Demo");
        System.out.println("0. Exit");
        System.out.print("\nChoice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        try {
            switch (choice) {
                case 1:
                    startAllServers();
                    break;
                case 2:
                    udpDemo();
                    break;
                case 3:
                    nioChatDemo();
                    break;
                case 4:
                    echoDemo();
                    break;
                case 5:
                    bufferDemo();
                    break;
                case 6:
                    integrationDemo();
                    break;
                case 0:
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Start all network servers
     */
    private static void startAllServers() throws IOException {
        System.out.println("\n=== Starting All Network Servers ===\n");

        // Start UDP Broadcast Service
        UDPBroadcastService udpService = new UDPBroadcastService(Protocol.UDP_PORT);
        udpService.start();
        System.out.println("✓ UDP Broadcast Service started on port " + Protocol.UDP_PORT);

        // Start NIO Chat Server
        NIOChatServer chatServer = new NIOChatServer(Protocol.CHAT_PORT);
        chatServer.start();
        System.out.println("✓ NIO Chat Server started on port " + Protocol.CHAT_PORT);

        // Start Echo Server
        EchoServer echoServer = new EchoServer(Protocol.ECHO_TCP_PORT, Protocol.ECHO_UDP_PORT);
        echoServer.start();
        System.out.println("✓ Echo Server started (TCP: " + Protocol.ECHO_TCP_PORT +
                         ", UDP: " + Protocol.ECHO_UDP_PORT + ")");

        System.out.println("\n=== All Servers Running ===");
        System.out.println("Press Enter to stop servers...");
        new Scanner(System.in).nextLine();

        // Shutdown
        udpService.stop();
        chatServer.stop();
        echoServer.stop();
        System.out.println("\nAll servers stopped.");
    }

    /**
     * UDP Broadcasting demonstration
     */
    private static void udpDemo() throws IOException, InterruptedException {
        System.out.println("\n=== UDP Broadcast Demo ===");
        System.out.println("Demonstrates: UDP sockets, Multicast, Connectionless communication\n");

        UDPBroadcastService udpService = new UDPBroadcastService(Protocol.UDP_PORT);
        udpService.start();

        System.out.println("Server started. Starting client...\n");
        Thread.sleep(1000);

        // Create UDP client
        UDPClient udpClient = new UDPClient("localhost", Protocol.UDP_PORT, message -> {
            System.out.println("[CLIENT RECEIVED]: " + message);
        });
        udpClient.start();

        Thread.sleep(1000);

        // Demo broadcasts
        System.out.println("\nSending broadcasts...\n");
        udpService.notifyQuizStart("QUIZ-001");
        Thread.sleep(500);

        udpService.notifyTimeUpdate(30);
        Thread.sleep(500);

        udpService.sendAlert("Quiz starting in 5 seconds!");
        Thread.sleep(500);

        System.out.println("\nSubscribed clients: " + udpService.getSubscribedClientsCount());

        System.out.println("\nPress Enter to stop...");
        new Scanner(System.in).nextLine();

        udpClient.stop();
        udpService.stop();
    }

    /**
     * NIO Chat Server demonstration
     */
    private static void nioChatDemo() throws IOException {
        System.out.println("\n=== NIO Chat Server Demo ===");
        System.out.println("Demonstrates: Java NIO, Selector, SocketChannel, Non-blocking I/O\n");

        NIOChatServer chatServer = new NIOChatServer(Protocol.CHAT_PORT);
        chatServer.start();

        System.out.println("Chat server started on port " + Protocol.CHAT_PORT);
        System.out.println("\nTo connect clients, run in separate terminals:");
        System.out.println("  telnet localhost " + Protocol.CHAT_PORT);
        System.out.println("  Or use: nc localhost " + Protocol.CHAT_PORT);
        System.out.println("\nPress Enter to stop server...");
        new Scanner(System.in).nextLine();

        chatServer.stop();
    }

    /**
     * Echo Server demonstration
     */
    private static void echoDemo() throws IOException, InterruptedException {
        System.out.println("\n=== Echo Server Demo ===");
        System.out.println("Demonstrates: Echo protocol, TCP/UDP testing, Latency measurement\n");

        EchoServer echoServer = new EchoServer(Protocol.ECHO_TCP_PORT, Protocol.ECHO_UDP_PORT);
        echoServer.start();

        Thread.sleep(1000);

        // Create test client
        EchoTestClient testClient = new EchoTestClient("localhost",
                                                       Protocol.ECHO_TCP_PORT,
                                                       Protocol.ECHO_UDP_PORT);

        System.out.println("Running connection tests...\n");
        testClient.runConnectionTest();

        System.out.println("\nMeasuring average latency...");
        testClient.measureAverageLatency(10);

        System.out.println("\n" + echoServer.getStats());

        System.out.println("\nPress Enter to stop...");
        new Scanner(System.in).nextLine();

        echoServer.stop();
    }

    /**
     * ByteBuffer management demonstration
     */
    private static void bufferDemo() {
        System.out.println("\n=== ByteBuffer Management Demo ===");
        System.out.println("Demonstrates: ByteBuffer, Buffer pooling, Memory efficiency\n");

        // Create buffer manager
        BufferManager bufferManager = new BufferManager(1024, false);

        System.out.println("1. Encoding/Decoding strings:");
        String testMessage = "Hello QuizHub!";
        var encoded = bufferManager.encodeString(testMessage);
        System.out.println("   Encoded: " + testMessage + " (buffer size: " + encoded.remaining() + " bytes)");

        encoded.rewind();
        String decoded = bufferManager.decodeString(encoded);
        System.out.println("   Decoded: " + decoded);

        System.out.println("\n2. Serializing quiz messages:");
        var serialized = bufferManager.serializeMessage("Q", "What is 2+2?~A:3~B:4~C:5~D:6");
        System.out.println("   Serialized question (buffer size: " + serialized.remaining() + " bytes)");

        serialized.rewind();
        String[] deserialized = bufferManager.deserializeMessage(serialized);
        System.out.println("   Type: " + deserialized[0] + ", Data: " + deserialized[1]);

        System.out.println("\n3. Buffer pooling test:");
        for (int i = 0; i < 100; i++) {
            var buffer = bufferManager.acquireBuffer();
            buffer.put(("Test " + i).getBytes());
            bufferManager.releaseBuffer(buffer);
        }

        System.out.println("   " + bufferManager.getStats());

        System.out.println("\nDemo complete!");
    }

    /**
     * Complete integration demonstration
     */
    private static void integrationDemo() throws IOException, InterruptedException {
        System.out.println("\n=== Complete Integration Demo ===");
        System.out.println("Starting all services...\n");

        // Start all services
        UDPBroadcastService udpService = new UDPBroadcastService(Protocol.UDP_PORT);
        NIOChatServer chatServer = new NIOChatServer(Protocol.CHAT_PORT);
        EchoServer echoServer = new EchoServer(Protocol.ECHO_TCP_PORT, Protocol.ECHO_UDP_PORT);
        BufferManager bufferManager = new BufferManager();

        udpService.start();
        chatServer.start();
        echoServer.start();

        System.out.println("✓ All services started\n");

        // Simulate quiz scenario
        System.out.println("=== Simulating Quiz Scenario ===\n");

        System.out.println("1. Teacher sends UDP broadcast: Quiz starting...");
        udpService.multicastMessage("ALERT|Quiz starting in 10 seconds!");
        Thread.sleep(1000);

        System.out.println("2. Students chatting before quiz (NIO Chat)...");
        chatServer.broadcastMessage("System: Quiz will start soon!", null);
        Thread.sleep(1000);

        System.out.println("3. Checking connections (Echo test)...");
        EchoTestClient testClient = new EchoTestClient("localhost",
                                                       Protocol.ECHO_TCP_PORT,
                                                       Protocol.ECHO_UDP_PORT);
        testClient.ping();
        Thread.sleep(1000);

        System.out.println("4. Broadcasting time updates via UDP...");
        for (int i = 10; i > 0; i--) {
            udpService.notifyTimeUpdate(i);
            Thread.sleep(100);
        }

        System.out.println("\n5. Quiz started! UDP notification sent.");
        udpService.notifyQuizStart("QUIZ-DEMO-001");

        System.out.println("\n=== Statistics ===");
        System.out.println("UDP Subscribers: " + udpService.getSubscribedClientsCount());
        System.out.println("Chat Clients: " + chatServer.getConnectedClientsCount());
        System.out.println(echoServer.getStats());
        System.out.println(bufferManager.getStats());

        System.out.println("\nPress Enter to stop all services...");
        new Scanner(System.in).nextLine();

        // Cleanup
        udpService.stop();
        chatServer.stop();
        echoServer.stop();

        System.out.println("\nAll services stopped. Demo complete!");
    }
}
