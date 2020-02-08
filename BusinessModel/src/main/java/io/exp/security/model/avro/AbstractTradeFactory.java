package io.exp.security.model.avro;

public interface AbstractTradeFactory {
    Trade createTrade(String securityId, double notional, double price, String currency, BidAsk bidAsk);
}
