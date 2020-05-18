package com.klt.transport.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class NonblockingSelectorServer {
    private static final Logger LOG = Logger.getLogger(NonblockingSelectorServer.class.getName());
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private final int port;
    private final BiConsumer<SocketChannel, ByteBuffer> consumer;
    private final Map<SocketChannel, Queue<ByteBuffer>> writableData = new ConcurrentHashMap<>();
    private volatile boolean stop = false;

    public NonblockingSelectorServer(int port, BiConsumer<SocketChannel, ByteBuffer> consumer) throws IOException {
        this.port = port;
        this.consumer = consumer;
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
    }

    public void start() throws IOException {
        serverSocketChannel.bind(new InetSocketAddress("localhost", port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while(!stop){
            try {
                selector.select(); // blocking
                for (Iterator<SelectionKey> itKeys = selector.selectedKeys().iterator(); itKeys.hasNext(); ) {
                    SelectionKey key = itKeys.next();
                    itKeys.remove();
                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }
                    }
                }
            } catch(IOException ioException) {
                LOG.severe(""+ioException);
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept(); // non-blocking
        sc.configureBlocking(false);
        sc.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        writableData.computeIfAbsent(sc, sc1->new ConcurrentLinkedQueue<>());
        LOG.info("Accepted connection on " + sc.getRemoteAddress());
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer incomingByteBuffer = ByteBuffer.allocate(1024);
        int numberOfBytesRead;
        while((numberOfBytesRead = socketChannel.read(incomingByteBuffer)) > 0) {
            incomingByteBuffer.flip();
            consumer.accept(socketChannel, incomingByteBuffer);
            incomingByteBuffer = ByteBuffer.allocate(1024);
        }
        if(numberOfBytesRead == -1){
            key.cancel();
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Queue<ByteBuffer> queue = writableData.get(socketChannel);
        ByteBuffer byteBuffer;
        while((byteBuffer = queue.peek()) != null) {
            socketChannel.write(byteBuffer);
            if(!byteBuffer.hasRemaining()){
                queue.poll();
            } else {
                return;
            }
        }
    }

    public void publish(SocketChannel socketChannel, ByteBuffer byteBuffer){
        writableData.get(socketChannel).add(byteBuffer);
    }

    public void publishToAllClients(ByteBuffer byteBuffer) {
        writableData.forEach((k,v)->v.add(byteBuffer.duplicate()));
    }

    public void stop(boolean stop){
        this.stop = stop;
    }
}
