package common;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * BufferManager - Demonstrates Java NIO ByteBuffer management
 *
 * Features:
 * - ByteBuffer pooling for memory efficiency
 * - Direct vs Heap buffers
 * - Buffer operations (flip, compact, clear)
 * - Serialization/Deserialization utilities
 *
 * Network Concepts: ByteBuffer, Memory Management, Buffer Pooling
 */
public class BufferManager {
    private static final Logger LOGGER = Logger.getLogger(BufferManager.class.getName());

    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int POOL_SIZE = 50;

    // Buffer pool for reuse
    private final ConcurrentLinkedQueue<ByteBuffer> bufferPool;
    private final int bufferSize;
    private final boolean useDirect;

    // Statistics
    private long buffersCreated;
    private long buffersReused;

    /**
     * Constructor
     * @param bufferSize Size of each buffer
     * @param useDirect Use direct buffers (off-heap memory) for better I/O performance
     */
    public BufferManager(int bufferSize, boolean useDirect) {
        this.bufferSize = bufferSize;
        this.useDirect = useDirect;
        this.bufferPool = new ConcurrentLinkedQueue<>();

        // Pre-allocate buffers
        for (int i = 0; i < POOL_SIZE; i++) {
            bufferPool.offer(createBuffer());
        }

        LOGGER.info("BufferManager initialized with " + POOL_SIZE + " " +
                   (useDirect ? "direct" : "heap") + " buffers of size " + bufferSize);
    }

    /**
     * Default constructor
     */
    public BufferManager() {
        this(DEFAULT_BUFFER_SIZE, false);
    }

    /**
     * Create a new buffer
     */
    private ByteBuffer createBuffer() {
        buffersCreated++;
        if (useDirect) {
            // Direct buffer - allocated outside JVM heap
            // Better for I/O operations, but more expensive to create
            return ByteBuffer.allocateDirect(bufferSize);
        } else {
            // Heap buffer - allocated in JVM heap
            // Easier to garbage collect
            return ByteBuffer.allocate(bufferSize);
        }
    }

    /**
     * Acquire a buffer from pool
     */
    public ByteBuffer acquireBuffer() {
        ByteBuffer buffer = bufferPool.poll();

        if (buffer == null) {
            // Pool empty, create new buffer
            buffer = createBuffer();
        } else {
            buffersReused++;
            buffer.clear(); // Reset buffer for reuse
        }

        return buffer;
    }

    /**
     * Release buffer back to pool
     */
    public void releaseBuffer(ByteBuffer buffer) {
        if (buffer != null) {
            buffer.clear();
            bufferPool.offer(buffer);
        }
    }

    /**
     * Encode string to ByteBuffer
     */
    public ByteBuffer encodeString(String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = acquireBuffer();

        // Write length prefix (for proper message boundaries)
        buffer.putInt(bytes.length);
        buffer.put(bytes);

        // Flip buffer from writing mode to reading mode
        buffer.flip();

        return buffer;
    }

    /**
     * Decode ByteBuffer to string
     */
    public String decodeString(ByteBuffer buffer) {
        // Save position
        int position = buffer.position();

        // Read length prefix
        int length = buffer.getInt();

        // Read string bytes
        byte[] bytes = new byte[length];
        buffer.get(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Serialize quiz message to ByteBuffer
     * Format: [Type:1byte][Length:4bytes][Data:variable]
     */
    public ByteBuffer serializeMessage(String type, String data) {
        ByteBuffer buffer = acquireBuffer();

        // Message type (single byte)
        buffer.put((byte) type.charAt(0));

        // Data
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        buffer.putInt(dataBytes.length);
        buffer.put(dataBytes);

        buffer.flip();
        return buffer;
    }

    /**
     * Deserialize ByteBuffer to message parts
     */
    public String[] deserializeMessage(ByteBuffer buffer) {
        // Read message type
        byte typeByte = buffer.get();
        String type = String.valueOf((char) typeByte);

        // Read data length
        int dataLength = buffer.getInt();

        // Read data
        byte[] dataBytes = new byte[dataLength];
        buffer.get(dataBytes);
        String data = new String(dataBytes, StandardCharsets.UTF_8);

        return new String[]{type, data};
    }

    /**
     * Copy buffer efficiently
     */
    public ByteBuffer copyBuffer(ByteBuffer source) {
        ByteBuffer copy = acquireBuffer();

        // Save source position
        int originalPosition = source.position();

        // Copy from beginning
        source.position(0);
        copy.put(source);

        // Restore positions
        source.position(originalPosition);
        copy.flip();

        return copy;
    }

    /**
     * Compact buffer - removes read data and prepares for more writing
     */
    public void compactBuffer(ByteBuffer buffer) {
        buffer.compact();
    }

    /**
     * Check if buffer has complete message
     */
    public boolean hasCompleteMessage(ByteBuffer buffer) {
        if (buffer.position() < 4) {
            return false; // Not enough data for length prefix
        }

        // Peek at length without consuming
        int savedPosition = buffer.position();
        buffer.position(0);
        int messageLength = buffer.getInt();
        buffer.position(savedPosition);

        return buffer.position() >= (4 + messageLength);
    }

    /**
     * Get buffer statistics
     */
    public String getStats() {
        return String.format("Buffers Created: %d, Buffers Reused: %d, Pool Size: %d, Reuse Rate: %.2f%%",
                           buffersCreated, buffersReused, bufferPool.size(),
                           buffersCreated > 0 ? (buffersReused * 100.0 / buffersCreated) : 0);
    }

    /**
     * Clear all buffers in pool
     */
    public void clearPool() {
        bufferPool.clear();
        LOGGER.info("Buffer pool cleared");
    }

    public long getBuffersCreated() {
        return buffersCreated;
    }

    public long getBuffersReused() {
        return buffersReused;
    }

    public int getPoolSize() {
        return bufferPool.size();
    }
}

