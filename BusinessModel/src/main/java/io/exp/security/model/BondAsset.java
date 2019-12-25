package io.exp.security.model;

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
}
