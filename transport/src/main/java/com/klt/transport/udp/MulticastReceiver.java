package com.klt.transport.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.function.Consumer;

public class MulticastReceiver {
    private static final String MULTICAST_INTERFACE = System.getProperty("MULTICAST_INTERFACE", "en1");
    private static final int MULTICAST_PORT = Integer.parseInt(System.getProperty("MULTICAST_PORT", "39996"));
    private static final String MULTICAST_IP = System.getProperty("MULTICAST_IP", "228.0.0.4");
    private final DatagramChannel datagramChannel;
    private final MembershipKey membershipKey;
    private final Consumer<ByteBuffer> consumer;
    private volatile boolean stop = false;

    public MulticastReceiver(Consumer<ByteBuffer> consumer) throws IOException {
        this.consumer = consumer;
        datagramChannel = DatagramChannel.open(StandardProtocolFamily.INET);
        datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        datagramChannel.bind(new InetSocketAddress(MULTICAST_PORT));
        NetworkInterface networkInterface = NetworkInterface.getByName(MULTICAST_INTERFACE);
        datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
        InetAddress inetAddress = InetAddress.getByName(MULTICAST_IP);
        membershipKey = datagramChannel.join(inetAddress, networkInterface);

    }
    public void start() throws IOException {
        while(!stop) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            datagramChannel.receive(byteBuffer);
            byteBuffer.flip();
            consumer.accept(byteBuffer);
        }
    }

    public void stop() {
        stop = true;
    }
}
