package com.klt.md.generator.tcp;

import com.klt.transport.tcp.NonblockingSelectorClient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class EchoTestClient implements BiConsumer<SocketChannel, ByteBuffer> {

    private final NonblockingSelectorClient nonblockingSelectorClient;

    private int counter;

    private SocketChannel socketChannel;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public EchoTestClient() throws IOException {
        this.nonblockingSelectorClient = new NonblockingSelectorClient();
    }

    public void start() throws IOException, InterruptedException {
        socketChannel = nonblockingSelectorClient.connect("localhost", 9666, this);
        scheduledExecutorService.scheduleAtFixedRate(this::publish, 1, 10, TimeUnit.SECONDS);
        nonblockingSelectorClient.start();
    }

    private void publish() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(counter++);
        byteBuffer.flip();
        nonblockingSelectorClient.publish(socketChannel, byteBuffer);
    }

    @Override
    public void accept(SocketChannel socketChannel, ByteBuffer byteBuffer) {
        System.out.println(byteBuffer.getInt());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        EchoTestClient echoTestClient = new EchoTestClient();
        echoTestClient.start();
    }

}

//[SO_KEEPALIVE, SO_OOBINLINE, TCP_KEEPCOUNT, SO_LINGER, IP_TOS, SO_SNDBUF,
// TCP_KEEPIDLE, SO_RCVBUF, SO_REUSEADDR, SO_REUSEPORT, TCP_NODELAY, TCP_KEEPINTERVAL]