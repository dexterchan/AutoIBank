package io.exp.security.model;

public interface Asset {
    String getSecurityId();
    String getSecurityGroup();
    double getNotional();
    double getPrice();
    String getCurrency();
    BidAsk getBidAsk();
    String getTradeType();

}
