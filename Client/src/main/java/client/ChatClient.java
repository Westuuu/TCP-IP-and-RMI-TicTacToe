package client;

import java.io.*;
import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatClient implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(ChatClient.class.getName());
    private static final int BUFFER_SIZE = 1024;
    private static final int CONNECTION_TIMEOUT = 5000; // milliseconds

    private SocketChannel channel;
    private final AtomicBoolean isRunning;
    private final AtomicBoolean isConnected;
    private Thread selectorThread;
    private final ScheduledExecutorService scheduler;
    private Selector selector;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private StringBuilder messageBuilder;
    private final Queue<String> messageQueue;

    public ChatClient() {
        this.isRunning = new AtomicBoolean(false);
        this.isConnected = new AtomicBoolean(false);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.messageBuilder = new StringBuilder();
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    public void connect(String host, int port) throws IOException {
        if (channel != null && channel.isOpen()) {
            disconnect();
        }

        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            
            channel.connect(new InetSocketAddress(host, port));
            
            channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
            isRunning.set(true);
            
            selectorThread = new Thread(this::processEvents);
            selectorThread.setDaemon(true);
            selectorThread.start();

            long startTime = System.currentTimeMillis();
            while (!isConnected.get()) {
                if (System.currentTimeMillis() - startTime > CONNECTION_TIMEOUT) {
                    throw new IOException("Connection timeout");
                }
                Thread.sleep(100);
            }

            LOGGER.info("Connected to chat server at " + host + ":" + port);
        } catch (IOException | InterruptedException e) {
            LOGGER.severe("Failed to connect to server: " + e.getMessage());
            disconnect();
            throw new IOException("Failed to connect to server", e);
        }
    }

    private void processEvents() {
        while (isRunning.get()) {
            try {
                if (selector.select(1000) == 0) {
                    continue;
                }

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isConnectable()) {
                        handleConnect(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (IOException e) {
                if (isRunning.get()) {
                    LOGGER.severe("Error in event processing: " + e.getMessage());
                    handleConnectionFailure();
                }
            }
        }
    }

    private void handleConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (channel.finishConnect()) {
                isConnected.set(true);
                LOGGER.info("Connection established");
            }
        } catch (IOException e) {
            key.cancel();
            handleConnectionFailure();
            throw e;
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        readBuffer.clear();
        int read;

        try {
            while ((read = channel.read(readBuffer)) > 0) {
                readBuffer.flip();
                while (readBuffer.hasRemaining()) {
                    char c = (char) readBuffer.get();
                    if (c == '\n') {
                        String message = messageBuilder.toString();
                        messageQueue.offer(message);
                        messageBuilder.setLength(0);
                    } else {
                        messageBuilder.append(c);
                    }
                }
                readBuffer.clear();
            }

            if (read < 0) {
                handleConnectionFailure();
                key.cancel();
                channel.close();
            }
        } catch (IOException e) {
            key.cancel();
            channel.close();
            handleConnectionFailure();
        }
    }

    private void handleConnectionFailure() {
        isConnected.set(false);
        LOGGER.warning("Connection failure detected");
    }

    public void sendMessage(String message) {
        if (channel != null && channel.isOpen() && isConnected.get()) {
            try {
                writeBuffer.clear();
                writeBuffer.put((message + "\n").getBytes());
                writeBuffer.flip();
                while (writeBuffer.hasRemaining()) {
                    channel.write(writeBuffer);
                }
            } catch (Exception e) {
                LOGGER.severe("Error sending message: " + e.getMessage());
                handleConnectionFailure();
            }
        }
    }

    public void disconnect() {
        isRunning.set(false);
        isConnected.set(false);
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (selectorThread != null) {
                selectorThread.interrupt();
            }
            LOGGER.info("Disconnected from chat server");
        } catch (IOException e) {
            LOGGER.severe("Error closing chat connections: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        disconnect();
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public String receiveMessage() {
        return messageQueue.poll();
    }
}
