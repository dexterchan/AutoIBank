package io.exp.security.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BondTradeFactoryTest {

    @Test
    void createTrade() {
        AbstractTradeFactory tradeFactory = new BondTradeFactory();
        String securityId = "ISIN0123";
        double notional = 1000000;
        double price = 100;
        String currency = "USD";
        BidAsk bidAsk = BidAsk.BID;
        Trade trade = tradeFactory.createTrade(securityId, notional,price,currency,bidAsk);


        assertNotNull(trade);
        assertNotNull(trade.getAsset());
        assertAll(
                ()->{
                    assertEquals(trade.getAsset().getTradeType(),"BOND");
                },
                ()->{
            assertEquals(trade.getAsset().getNotional(), notional); },
                ()->{
            assertEquals(trade.getAsset().getPrice(), price); },
                ()->{
            assertEquals(trade.getAsset().getBidAsk(), bidAsk);
        });
        assertThrows(UnsupportedOperationException.class, ()->{ trade.getAsset().getSecurityGroup();});
    }
}