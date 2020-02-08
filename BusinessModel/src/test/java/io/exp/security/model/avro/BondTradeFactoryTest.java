package io.exp.security.model.avro;

import org.apache.beam.sdk.coders.AvroCoder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
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
        Trade trade = tradeFactory.createTrade(securityId, notional, price, currency, bidAsk);


        assertNotNull(trade);
        assertNotNull(trade.getAsset());
        assertAll(
                () -> {
                    assertEquals(trade.getAsset().getTradeType(), "BOND");
                },
                () -> {
                    assertEquals(trade.getAsset().getNotional(), notional);
                },
                () -> {
                    assertEquals(trade.getAsset().getPrice(), price);
                },
                () -> {
                    assertEquals(trade.getAsset().getBidAsk(), bidAsk);
                });
        assertThrows(UnsupportedOperationException.class, () -> {
            trade.getAsset().getSecurityGroup();
        });
    }

    @Test
    void test_AvroCoder() throws IOException {
        AbstractTradeFactory tradeFactory = new BondTradeFactory();
        String securityId = "ISIN0123";
        double notional = 1000000;
        double price = 100;
        String currency = "USD";
        BidAsk bidAsk = BidAsk.BID;
        BondTrade trade = (BondTrade)tradeFactory.createTrade(securityId, notional, price, currency, bidAsk);
        AvroCoder<BondTrade> coder = AvroCoder.of(BondTrade.class);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        coder.encode(trade, outStream);
        assertThat(outStream.size()).isGreaterThan(0);

        byte[] byteArray = outStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        BondTrade trade2=coder.decode(inputStream);
        assertAll(
                ()->{
                    assertEquals(trade.id, trade2.id);
                },
                ()->{
                    assertNotNull(trade2.getAsset());
                },
                ()->{
                    assertEquals(trade.tradeType, trade2.tradeType);
                }
                ,
                ()->{
                    assertThat(trade2.getAsset()).isEqualTo(trade.getAsset());
                }
                ,
                ()->{
                    assertEquals(trade.timestamp, trade2.timestamp);
                }
        );
    }
}