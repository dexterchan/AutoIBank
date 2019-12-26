package io.exp.security.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
public class BondAsset implements Asset{

    protected String securityId;
    protected double notional;
    protected double price;
    protected String currency;
    protected BidAsk bidAsk;

    @Override
    public String getTradeType() {
        return "BOND";
    }

    @Override
    public String getSecurityGroup() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String toString() {
        Gson g = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                .create();
        return g.toJson(this);
    }

}
