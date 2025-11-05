package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * NIOChatServer - Demonstrates Java NIO (New I/O / Non-blocking I/O)
 *
 * Features:
 * - Non-blocking I/O using Selectors and Channels
 * - Efficient handling of multiple connections with single thread
 * - ByteBuffer for efficient data management
 * - Chat functionality for students during quiz breaks
 *
 * Network Concepts: Selector, SocketChannel, ByteBuffer, Non-blocking I/O
 */
public class NIOChatServer {
    private static final Logger LOGGER = Logger.getLogger(NIOChatServer.class.getName());

    private int port;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private boolean running;
    private Thread serverThread;

    // Map of channels to student names
    private Map<SocketChannel, String> clientNames;
    private Map<String, SocketChannel> nameToChannel;

    // ByteBuffer pool for efficient memory management
    private static final int BUFFER_SIZE = 1024;

    public NIOChatServer(int port) {
        this.port = port;
        this.clientNames = new ConcurrentHashMap<>();
        this.nameToChannel = new ConcurrentHashMap<>();
    }

    /**
     * Start NIO Chat Server
     */
    public void start() throws IOException {
        // Open Selector - multiplexes multiple channels
        selector = Selector.open();

        // Open ServerSocketChannel and configure non-blocking mode
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));

        // Register channel with selector for ACCEPT operations
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        running = true;
        LOGGER.info("NIO Chat Server started on port " + port);

        // Start server loop in separate thread
        serverThread = new Thread(this::serverLoop);
        serverThread.start();
    }

    /**
     * Main server loop using Selector
     */
    private void serverLoop() {
        while (running) {
            try {
                // Wait for events (blocking call, but handles multiple channels)
                int readyChannels = selector.select(1000); // 1 second timeout

                if (readyChannels == 0) {
                    continue;
                }

                // Get selected keys (channels with events)
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // Handle different types of events
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }

            } catch (IOException e) {
                if (running) {
                    LOGGER.severe("Error in server loop: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handle new client connection
     */
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel != null) {
            // Configure non-blocking mode
            clientChannel.configureBlocking(false);

            // Register for READ operations
            clientChannel.register(selector, SelectionKey.OP_READ);

            // Allocate ByteBuffer for this channel
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            clientChannel.register(selector, SelectionKey.OP_READ, buffer);

            LOGGER.info("New chat client connected: " + clientChannel.getRemoteAddress());

            // Send welcome message
            sendMessage(clientChannel, "Welcome to QuizHub Chat! Please identify yourself.");
        }
    }

    /**
     * Handle incoming data from client
     */
    private void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        try {
            buffer.clear(); // Prepare buffer for reading

            int bytesRead = clientChannel.read(buffer);

            if (bytesRead == -1) {
                // Client disconnected
                handleDisconnect(clientChannel);
                key.cancel();
                return;
            }

            if (bytesRead > 0) {
                // Flip buffer from writing mode to reading mode
                buffer.flip();

                // Decode message
                String message = StandardCharsets.UTF_8.decode(buffer).toString().trim();

                if (!message.isEmpty()) {
                    processMessage(clientChannel, message);
                }
            }

        } catch (IOException e) {
            LOGGER.warning("Error reading from client: " + e.getMessage());
            handleDisconnect(clientChannel);
            key.cancel();
        }
    }

    /**
     * Process received message
     */
    private void processMessage(SocketChannel clientChannel, String message) {
        String clientName = clientNames.get(clientChannel);

        // First message is the client name
        if (clientName == null) {
            if (message.startsWith("NAME:")) {
                String name = message.substring(5).trim();
                clientNames.put(clientChannel, name);
                nameToChannel.put(name, clientChannel);

                LOGGER.info("Client identified as: " + name);
                sendMessage(clientChannel, "Welcome " + name + "! You can now chat.");
                broadcastMessage(name + " joined the chat!", clientChannel);
            }
            return;
        }

        // Handle commands
        if (message.startsWith("/")) {
            handleCommand(clientChannel, message);
            return;
        }

        // Broadcast regular message
        String formattedMessage = "[" + clientName + "]: " + message;
        LOGGER.info("Chat: " + formattedMessage);
        broadcastMessage(formattedMessage, null);
    }

    /**
     * Handle chat commands
     */
    private void handleCommand(SocketChannel clientChannel, String command) {
        String clientName = clientNames.get(clientChannel);

        if (command.startsWith("/list")) {
            // List all connected users
            StringBuilder userList = new StringBuilder("Connected users: ");
            for (String name : clientNames.values()) {
                userList.append(name).append(", ");
            }
            sendMessage(clientChannel, userList.toString());

        } else if (command.startsWith("/whisper")) {
            // Private message: /whisper username message
            String[] parts = command.split(" ", 3);
            if (parts.length >= 3) {
                String targetName = parts[1];
                String privateMessage = parts[2];

                SocketChannel targetChannel = nameToChannel.get(targetName);
                if (targetChannel != null) {
                    sendMessage(targetChannel, "[Whisper from " + clientName + "]: " + privateMessage);
                    sendMessage(clientChannel, "[Whisper to " + targetName + "]: " + privateMessage);
                } else {
                    sendMessage(clientChannel, "User " + targetName + " not found.");
                }
            }

        } else if (command.equals("/help")) {
            sendMessage(clientChannel,
                "Commands: /list (show users), /whisper <user> <msg> (private message), /help");
        }
    }

    /**
     * Send message to specific client using ByteBuffer
     */
    private void sendMessage(SocketChannel channel, String message) {
        try {
            // Add newline for message boundary
            String messageWithNewline = message + "\n";

            // Allocate ByteBuffer and put data
            ByteBuffer buffer = ByteBuffer.wrap(messageWithNewline.getBytes(StandardCharsets.UTF_8));

            // Write to channel (may not write all bytes in one call)
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

        } catch (IOException e) {
            LOGGER.warning("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Broadcast message to all clients except sender
     */
    public void broadcastMessage(String message, SocketChannel excludeChannel) {
        for (SocketChannel channel : clientNames.keySet()) {
            if (channel != excludeChannel && channel.isConnected()) {
                sendMessage(channel, message);
            }
        }
    }

    /**
     * Handle client disconnect
     */
    private void handleDisconnect(SocketChannel clientChannel) {
        String clientName = clientNames.remove(clientChannel);

        if (clientName != null) {
            nameToChannel.remove(clientName);
            LOGGER.info("Client disconnected: " + clientName);
            broadcastMessage(clientName + " left the chat.", null);
        }

        try {
            clientChannel.close();
        } catch (IOException e) {
            LOGGER.warning("Error closing channel: " + e.getMessage());
        }
    }

    /**
     * Stop NIO Chat Server
     */
    public void stop() {
        running = false;

        // Close all client channels
        for (SocketChannel channel : clientNames.keySet()) {
            try {
                channel.close();
            } catch (IOException e) {
                LOGGER.warning("Error closing client channel: " + e.getMessage());
            }
        }

        // Close server channel
        try {
            if (serverChannel != null) {
                serverChannel.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            LOGGER.warning("Error closing server: " + e.getMessage());
        }

        if (serverThread != null) {
            serverThread.interrupt();
        }

        LOGGER.info("NIO Chat Server stopped");
    }

    public int getConnectedClientsCount() {
        return clientNames.size();
    }
}

