package com.klt.md.generator.tcp;

import com.klt.transport.tcp.NonblockingSelectorServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.function.BiConsumer;


public class EchoTestServer implements BiConsumer<SocketChannel, ByteBuffer> {

    private final NonblockingSelectorServer nonblockingSelectorServer;

    public EchoTestServer() throws IOException {
        nonblockingSelectorServer = new NonblockingSelectorServer(9666, this);
    }

    public void start() throws IOException {
        nonblockingSelectorServer.start();
    }

    @Override
    public void accept(SocketChannel socketChannel, ByteBuffer byteBuffer) {
        System.out.println(byteBuffer.getInt());
        byteBuffer.flip();
        nonblockingSelectorServer.publish(socketChannel, byteBuffer);
    }

    public static void main(String[] args) throws IOException {
        EchoTestServer echoTestServer = new EchoTestServer();
        echoTestServer.start();
    }

}

//[SO_REUSEPORT, TCP_KEEPCOUNT, TCP_KEEPIDLE, SO_RCVBUF, SO_REUSEADDR, TCP_KEEPINTERVAL]