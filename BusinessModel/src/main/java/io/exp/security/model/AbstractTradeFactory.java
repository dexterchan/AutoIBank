package io.exp.security.model;

public interface AbstractTradeFactory {
    Trade createTrade(String securityId, double notional, double price, String currency, BidAsk bidAsk);
}
