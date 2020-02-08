package io.exp.security.model.avro;

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BondAsset)){
            return false;
        }
        BondAsset bondAsset = (BondAsset)obj;
        if (Math.abs(this.notional-bondAsset.notional)>0.1){
            return false;
        }
        if (Math.abs(this.price - bondAsset.price)>0.001){
            return false;
        }
        if (!this.currency.equals(bondAsset.currency)){
            return false;
        }
        if(!this.securityId.equals(bondAsset.securityId)){
            return false;
        }
        if(this.bidAsk!=bondAsset.bidAsk){
            return false;
        }
        return true;
    }
}
