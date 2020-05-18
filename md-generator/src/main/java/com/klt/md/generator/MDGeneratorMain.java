package com.klt.md.generator;

import com.klt.md.generator.quote.Quote;
import com.klt.md.generator.quote.QuoteCodec;
import com.klt.transport.tcp.NonblockingSelectorServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class MDGeneratorMain implements BiConsumer<SocketChannel, ByteBuffer> {
    private static final Logger LOG = Logger.getLogger(MDGeneratorMain.class.getName());
    private static final String EURUSD_SYMBOL = "EURUSD";
    private static final double EURUSD_SPREAD = 10 * 1e-4;

    private final NonblockingSelectorServer nonblockingSelectorServer;
    private final QuoteCodec quoteCodec = new QuoteCodec();
    private final PriceGenerator eurusdPriceGenerator = new PriceGenerator(1.0825);
    private final ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();

    public MDGeneratorMain(int port) throws IOException {
        nonblockingSelectorServer = new NonblockingSelectorServer(port, this);
    }

    public void start() throws IOException {
        schedule.scheduleAtFixedRate(this::generate, 1, 1, TimeUnit.SECONDS);
        nonblockingSelectorServer.start();
    }

    @Override
    public void accept(SocketChannel socketChannel, ByteBuffer byteBuffer) {
        // no-op
    }

    private void generate() {
        double price = eurusdPriceGenerator.generate(Instant.now().getNano());
        nonblockingSelectorServer.publishToAllClients(quoteCodec.encode(new Quote(EURUSD_SYMBOL, price, price + EURUSD_SPREAD)));
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getProperty("PRICE_GENERATOR_SERVER_PORT", "8888"));
        MDGeneratorMain mdGeneratorMain = new MDGeneratorMain(port);
        mdGeneratorMain.start();
    }

    private static class PriceGenerator {
        private double lastPrice;

        public PriceGenerator(double lastPrice) {
            this.lastPrice = lastPrice;
        }

        public double generate(int seed) {
            seed = seed / 1000;
            double bps = ((seed % 5) + 1) * 1e-4;
            lastPrice = seed % 2 == 0 ? lastPrice + bps : lastPrice - bps;
            return lastPrice;
        }
    }

}
