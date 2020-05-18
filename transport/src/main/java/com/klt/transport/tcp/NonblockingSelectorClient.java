package com.klt.transport.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class NonblockingSelectorClient {
    private static final Logger LOG = Logger.getLogger(NonblockingSelectorClient.class.getName());
    private final Selector selector;
    private final Map<SocketChannel, BiConsumer<SocketChannel, ByteBuffer>> consumerMapping = new ConcurrentHashMap<>();
    private final Map<SocketChannel, Queue<ByteBuffer>> writableData = new ConcurrentHashMap<>();
    private volatile boolean stop = false;

    public NonblockingSelectorClient() throws IOException {
        selector = Selector.open();
    }

    public void start() throws IOException {
        while (!stop) {
            selector.select(); // blocking
            for (Iterator<SelectionKey> itKeys = selector.selectedKeys().iterator(); itKeys.hasNext(); ) {
                SelectionKey key = itKeys.next();
                itKeys.remove();
                if (key.isValid()) {
                    if (key.isConnectable()) {
                        connect(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    }
                }
            }
        }
    }

    public SocketChannel connect(String host, int port, BiConsumer<SocketChannel, ByteBuffer> consumer) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        socketChannel.connect(new InetSocketAddress(host, port));
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        consumerMapping.put(socketChannel, consumer);
        return socketChannel;
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        if (socketChannel.finishConnect()) {
            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            writableData.computeIfAbsent(socketChannel, sc -> new ConcurrentLinkedQueue<>());
            LOG.info("connected to " + socketChannel.getRemoteAddress());
        } else {
            LOG.info("Failed to connect to host:" + socketChannel.getRemoteAddress());
            key.cancel();
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer incomingByteBuffer = ByteBuffer.allocate(1024);
        int numberOfBytesRead;
        while ((numberOfBytesRead = socketChannel.read(incomingByteBuffer)) > 0) {
            incomingByteBuffer.flip();
            consumerMapping.get(socketChannel).accept(socketChannel, incomingByteBuffer);
            incomingByteBuffer = ByteBuffer.allocate(1024);
        }
        if (numberOfBytesRead == -1) {
            key.cancel();
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Queue<ByteBuffer> queue = writableData.get(socketChannel);
        ByteBuffer byteBuffer;
        while ((byteBuffer = queue.peek()) != null) {
            socketChannel.write(byteBuffer);
            if (!byteBuffer.hasRemaining()) {
                queue.poll();
            } else {
                return;
            }
        }
    }

    public void publish(SocketChannel socketChannel, ByteBuffer byteBuffer) {
        writableData.get(socketChannel).add(byteBuffer);
    }

    public void stop(boolean stop) {
        this.stop = stop;
    }
}
