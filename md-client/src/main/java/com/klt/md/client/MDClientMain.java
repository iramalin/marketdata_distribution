package com.klt.md.client;

import com.klt.md.generator.quote.Quote;
import com.klt.md.generator.quote.QuoteCodec;
import com.klt.transport.udp.MulticastReceiver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MDClientMain implements Consumer<ByteBuffer> {
    private static final Logger LOG = Logger.getLogger(MDClientMain.class.getName());
    private final MulticastReceiver mcReceiver;
    private final QuoteCodec quoteCodec = new QuoteCodec();

    public MDClientMain() throws IOException {
        mcReceiver = new MulticastReceiver(this);
    }

    private void start() throws IOException {
        mcReceiver.start();
    }

    @Override
    public void accept(ByteBuffer byteBuffer) {
        Quote quote = quoteCodec.decode(byteBuffer);
        LOG.info("Received " + quote);
    }

    public static void main(String[] args) throws IOException {
        MDClientMain mdClientMain = new MDClientMain();
        mdClientMain.start();
    }
}
