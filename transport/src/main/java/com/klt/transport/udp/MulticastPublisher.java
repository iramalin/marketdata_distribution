package com.klt.transport.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class MulticastPublisher {

    private static final String MULTICAST_INTERFACE = System.getProperty("MULTICAST_INTERFACE", "en1");
    private static final int MULTICAST_PORT = Integer.parseInt(System.getProperty("MULTICAST_PORT", "39996"));
    private static final String MULTICAST_IP = System.getProperty("MULTICAST_IP", "228.0.0.4");
    private final DatagramChannel datagramChannel;
    private final InetSocketAddress inetSocketAddress;

    public MulticastPublisher() throws IOException {
        datagramChannel = DatagramChannel.open();
        datagramChannel.bind(null);
        NetworkInterface networkInterface = NetworkInterface.getByName(MULTICAST_INTERFACE);
        datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
        inetSocketAddress = new InetSocketAddress(MULTICAST_IP, MULTICAST_PORT);
    }

    public void publish(ByteBuffer byteBuffer) throws IOException {
        datagramChannel.send(byteBuffer, inetSocketAddress);
    }

}
