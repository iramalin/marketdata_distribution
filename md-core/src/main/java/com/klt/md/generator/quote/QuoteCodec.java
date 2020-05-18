package com.klt.md.generator.quote;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class QuoteCodec {

    private final HashMap<String, byte[]> bytesMap = new HashMap<>();

    public ByteBuffer encode(Quote quote) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(22);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put(getBytes(quote.getSymbol()));
        byteBuffer.putDouble(quote.getBid());
        byteBuffer.putDouble(quote.getAsk());
        byteBuffer.flip();
        return byteBuffer;
    }

    public Quote decode(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.nativeOrder());
        byte[] symbolBytes = new byte[6];
        byteBuffer.get(symbolBytes);
        String symbol = new String(symbolBytes, StandardCharsets.UTF_8);
        return new Quote(symbol, byteBuffer.getDouble(), byteBuffer.getDouble());
    }

    private byte[] getBytes(String symbol) {
        return bytesMap.computeIfAbsent(symbol, s->s.getBytes(StandardCharsets.UTF_8));
    }
}
