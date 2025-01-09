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

public class ChatServer implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());
    private static final int BASE_CHAT_PORT = 5000;
    private static final int BUFFER_SIZE = 1024;
    
    private final UUID roomId;
    private ServerSocketChannel serverChannel;
    private SocketChannel clientChannel;
    private final AtomicBoolean isRunning;
    private final AtomicBoolean isConnected;
    private Thread selectorThread;
    private int port;
    private final ScheduledExecutorService scheduler;
    private Selector selector;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private StringBuilder messageBuilder;
    private final Queue<String> messageQueue;

    public ChatServer(UUID roomId) {
        this.roomId = roomId;
        this.isRunning = new AtomicBoolean(false);
        this.isConnected = new AtomicBoolean(false);
        this.port = BASE_CHAT_PORT + (Math.abs(roomId.hashCode()) % 1000);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.messageBuilder = new StringBuilder();
        this.messageQueue = new ConcurrentLinkedQueue<>();
        LOGGER.info("Initialized chat server for room " + roomId + " on port " + port);
    }

    public void start() throws IOException {
        if (serverChannel != null && serverChannel.isOpen()) {
            stop();
        }

        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            
            boolean bound = false;
            int attempts = 0;
            while (!bound && attempts < 10) {
                try {
                    if (port <= 65535) {
                        serverChannel.socket().bind(new InetSocketAddress(port));
                        bound = true;
                    } else {
                        throw new IOException("Port number exceeded maximum value");
                    }
                } catch (IOException e) {
                    port++;
                    attempts++;
                    if (attempts >= 10) {
                        throw new IOException("Could not find available port after " + attempts + " attempts");
                    }
                }
            }

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            isRunning.set(true);
            
            selectorThread = new Thread(this::processEvents);
            selectorThread.setDaemon(true);
            selectorThread.start();
            
            LOGGER.info("Chat server started on port " + port);
        } catch (IOException e) {
            LOGGER.severe("Failed to start chat server: " + e.getMessage());
            throw e;
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

                    if (key.isAcceptable()) {
                        handleAccept();
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

    private void handleAccept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        if (channel != null) {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            clientChannel = channel;
            isConnected.set(true);
            LOGGER.info("Client connected from: " + channel.getRemoteAddress());
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
        LOGGER.warning("Connection failure detected for room " + roomId);
    }

    public void sendMessage(String message) {
        if (clientChannel != null && clientChannel.isOpen() && isConnected.get()) {
            try {
                writeBuffer.clear();
                writeBuffer.put((message + "\n").getBytes());
                writeBuffer.flip();
                while (writeBuffer.hasRemaining()) {
                    clientChannel.write(writeBuffer);
                }
            } catch (Exception e) {
                LOGGER.severe("Error sending message: " + e.getMessage());
                handleConnectionFailure();
            }
        }
    }

    public void stop() {
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
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
            if (clientChannel != null && clientChannel.isOpen()) {
                clientChannel.close();
            }
            if (selectorThread != null) {
                selectorThread.interrupt();
            }
            LOGGER.info("Chat server stopped for room " + roomId);
        } catch (IOException e) {
            LOGGER.severe("Error closing chat connections: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        stop();
    }

    public int getPort() {
        return port;
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public String receiveMessage() {
        return messageQueue.poll();
    }
}
