package io.exp.security.model.avro;

import java.io.Serializable;

public  interface Asset extends Serializable {
    public  String getSecurityId();
    public  String getSecurityGroup();
    public  double getNotional();
    public  double getPrice();
    public  String getCurrency();
    public  BidAsk getBidAsk();
    public  String getTradeType();

}
