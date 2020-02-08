package io.exp.security.model.avro;



public interface AbstractTradeFactory<T> {
    T createTrade(String securityId, String cust, String tradeDate, double notional, double price, String currency, BidAsk bidAsk);
}
