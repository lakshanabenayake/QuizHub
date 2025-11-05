package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * NIOChatClient - Client for NIO Chat Server
 *
 * Features:
 * - Non-blocking chat client
 * - ByteBuffer-based communication
 * - Asynchronous message handling
 *
 * Network Concepts: SocketChannel, ByteBuffer, Non-blocking I/O
 */
public class NIOChatClient {
    private static final Logger LOGGER = Logger.getLogger(NIOChatClient.class.getName());

    private String serverHost;
    private int serverPort;
    private SocketChannel socketChannel;
    private boolean connected;
    private Thread readThread;
    private ChatMessageCallback callback;

    private static final int BUFFER_SIZE = 1024;

    public interface ChatMessageCallback {
        void onChatMessage(String message);
    }

    public NIOChatClient(String serverHost, int serverPort, ChatMessageCallback callback) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.callback = callback;
    }

    /**
     * Connect to chat server
     */
    public boolean connect(String userName) throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
        socketChannel.configureBlocking(false);

        connected = true;

        // Send name
        sendMessage("NAME:" + userName);

        // Start reading messages
        startReader();

        LOGGER.info("Connected to chat server as " + userName);
        return true;
    }

    /**
     * Start reading messages from server
     */
    private void startReader() {
        readThread = new Thread(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            StringBuilder messageBuilder = new StringBuilder();

            while (connected) {
                try {
                    buffer.clear();
                    int bytesRead = socketChannel.read(buffer);

                    if (bytesRead == -1) {
                        // Server closed connection
                        disconnect();
                        break;
                    }

                    if (bytesRead > 0) {
                        buffer.flip();
                        String chunk = StandardCharsets.UTF_8.decode(buffer).toString();
                        messageBuilder.append(chunk);

                        // Process complete messages (delimited by newline)
                        String accumulated = messageBuilder.toString();
                        int newlineIndex;

                        while ((newlineIndex = accumulated.indexOf('\n')) != -1) {
                            String message = accumulated.substring(0, newlineIndex);
                            accumulated = accumulated.substring(newlineIndex + 1);

                            if (!message.isEmpty() && callback != null) {
                                callback.onChatMessage(message);
                            }
                        }

                        messageBuilder = new StringBuilder(accumulated);
                    }

                    // Small delay to avoid busy-waiting
                    Thread.sleep(10);

                } catch (IOException e) {
                    if (connected) {
                        LOGGER.warning("Error reading from server: " + e.getMessage());
                        disconnect();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        readThread.start();
    }

    /**
     * Send message to server
     */
    public void sendMessage(String message) {
        try {
            String messageWithNewline = message + "\n";
            ByteBuffer buffer = ByteBuffer.wrap(messageWithNewline.getBytes(StandardCharsets.UTF_8));

            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }

        } catch (IOException e) {
            LOGGER.severe("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Send chat message
     */
    public void sendChatMessage(String message) {
        sendMessage(message);
    }

    /**
     * Send private message
     */
    public void sendWhisper(String targetUser, String message) {
        sendMessage("/whisper " + targetUser + " " + message);
    }

    /**
     * Request user list
     */
    public void requestUserList() {
        sendMessage("/list");
    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        connected = false;

        try {
            if (socketChannel != null && socketChannel.isConnected()) {
                socketChannel.close();
            }
        } catch (IOException e) {
            LOGGER.warning("Error closing connection: " + e.getMessage());
        }

        if (readThread != null) {
            readThread.interrupt();
        }

        LOGGER.info("Disconnected from chat server");
    }

    public boolean isConnected() {
        return connected && socketChannel != null && socketChannel.isConnected();
    }
}

