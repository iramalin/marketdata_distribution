package com.klt.md.distributor;

import com.klt.transport.tcp.NonblockingSelectorClient;
import com.klt.transport.udp.MulticastPublisher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class MDDistributorMain implements BiConsumer<SocketChannel, ByteBuffer> {

    private static final Logger LOG = Logger.getLogger(MDDistributorMain.class.getName());
    private static final String PRICE_GENERATOR_SERVER_HOST = System.getProperty("PRICE_GENERATOR_SERVER_HOST", "localhost");
    private static final int PRICE_GENERATOR_SERVER_PORT = Integer.parseInt(System.getProperty("PRICE_GENERATOR_SERVER_PORT", "8888"));
    private final NonblockingSelectorClient lpClient = new NonblockingSelectorClient();
    private final MulticastPublisher mcPublisher = new MulticastPublisher();

    public MDDistributorMain() throws IOException {
    }

    private void start() throws IOException {
        startLPClient();
    }

    private void startLPClient() throws IOException {
        lpClient.connect(PRICE_GENERATOR_SERVER_HOST, PRICE_GENERATOR_SERVER_PORT, this);
        lpClient.start();
    }

    @Override
    public void accept(SocketChannel socketChannel, ByteBuffer byteBuffer) {
        try {
            mcPublisher.publish(byteBuffer);
        } catch (IOException ioException) {
            LOG.severe("" + ioException);
        }
    }

    public static void main(String[] args) throws IOException {
        MDDistributorMain mdDistributorMain = new MDDistributorMain();
        mdDistributorMain.start();
    }

}
