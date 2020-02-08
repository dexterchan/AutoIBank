package io.exp.security.model.avro;

import org.apache.beam.sdk.coders.AvroCoder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BondTradeFactoryImplTest {

    @Test
    void createTrade() {
        AbstractTradeFactory<BondTrade> tradeFactory = new BondTradeFactoryImpl();
        String securityId = "ISIN0123";
        double notional = 1000000;
        double price = 100;
        String currency = "USD";
        String cust = "ABCD";
        BidAsk bidAsk = BidAsk.BID;
        String TradeDate = "2018-01-04";
        BondTrade trade = tradeFactory.createTrade(securityId,cust,TradeDate, notional, price, currency, bidAsk);


        assertNotNull(trade);
        assertNotNull(trade.getAsset());
        assertAll(
                () -> {
                    assertEquals(trade.getTradeType(), "BOND");
                },
                () -> {
                    assertEquals(trade.getAsset().getNotional(), notional);
                },
                () -> {
                    assertEquals(trade.getAsset().getPrice(), price);
                },
                () -> {
                    assertEquals(trade.getAsset().getBidask(), bidAsk);
                });

    }

    @Test
    void test_AvroCoder() throws IOException {
        AbstractTradeFactory<BondTrade> tradeFactory = new BondTradeFactoryImpl();
        String securityId = "ISIN0123";
        double notional = 1000000;
        double price = 100;
        String currency = "USD";
        String cust = "ABCD";
        BidAsk bidAsk = BidAsk.BID;
        String TradeDate = "2018-01-04";
        BondTrade trade = tradeFactory.createTrade(securityId,cust,TradeDate, notional, price, currency, bidAsk);

        AvroCoder<BondTrade> coder = AvroCoder.of(BondTrade.class);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        coder.encode(trade, outStream);
        assertThat(outStream.size()).isGreaterThan(0);
        byte[] byteArray = outStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        BondTrade trade2=coder.decode(inputStream);
        assertAll(
                ()->{
                    assertEquals(trade.getId(), trade2.getId());
                },
                ()->{
                    assertNotNull(trade2.getAsset());
                },
                ()->{
                    assertEquals(trade.getTradeType(), trade2.getTradeType());
                }
                ,
                ()->{
                    assertThat(trade2.getAsset()).isEqualTo(trade.getAsset());
                }
                ,
                ()->{
                    assertEquals(trade.getTimestamp(), trade2.getTimestamp());
                }
        );
    }
}