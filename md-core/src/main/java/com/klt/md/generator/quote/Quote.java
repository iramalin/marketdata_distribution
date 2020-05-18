package com.klt.md.generator.quote;

/**
 * Represents top of the book
 */
public class Quote {
    private final String symbol;
    private final double bid;
    private final double ask;

    public Quote(String symbol, double bid, double ask) {
        this.symbol = symbol;
        this.bid = bid;
        this.ask = ask;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "symbol=" + symbol +
                ", bid=" + bid +
                ", ask=" + ask +
                '}';
    }
}
