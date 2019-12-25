package io.exp.security.model;

import java.util.Date;
import java.util.UUID;

public class BondTradeFactory implements AbstractTradeFactory {
    @Override
    public Trade createTrade(String securityId, double notional, double price, String currency, BidAsk bidAsk) {
        UUID uuid = UUID.randomUUID();
        Trade trade = new Trade();
        trade.id = uuid.toString();
        trade.timestamp = new Date();
        BondAsset bondAsset = new BondAsset();
        trade.tradeType = bondAsset.getTradeType();
        trade.asset = bondAsset;

        bondAsset.setSecurityId(securityId);
        bondAsset.setNotional(notional);
        bondAsset.setPrice(price);
        bondAsset.setCurrency(currency);
        bondAsset.setBidAsk(bidAsk);

        return trade;
    }
}
