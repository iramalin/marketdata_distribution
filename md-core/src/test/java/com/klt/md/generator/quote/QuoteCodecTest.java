package com.klt.md.generator.quote;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class QuoteCodecTest {

    private final Quote quote = new Quote("EURUSD", 1.08258, 1.08268);

    @Test
    public void testCodec() {
        QuoteCodec quoteCodec = new QuoteCodec();
        ByteBuffer byteBuffer = quoteCodec.encode(quote);
        Quote decodedQuote = quoteCodec.decode(byteBuffer);
        Assert.assertEquals(quote.getSymbol(), decodedQuote.getSymbol());
        Assert.assertEquals(quote.getBid(), decodedQuote.getBid(), 1e-8);
        Assert.assertEquals(quote.getAsk(), decodedQuote.getAsk(), 1e-8);
    }

}
