package io.exp.gateway.fake;


import io.exp.security.model.avro.BondTrade;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class FakeBondMarketGeneratorTest {
    static int MAX = 2;
    @Test
    void generateTrade() {
        int cnt = 0;
        double seedNotional = 1000000;
        double seedPrice = 100;
        String currency = "USD";
        String[] securityArray = FakeBondMarketGatewayProxy.createSecurityNames(5);//{"ISIN1234", "ISIN4324", "CISP23434"};
        FakeBondMarketGenerator fakeBondMarketGenerator = new FakeBondMarketGenerator(seedNotional, seedPrice, currency, seedNotional*0.1, seedPrice*0.1, securityArray);
        while (cnt<MAX){
            BondTrade trade = fakeBondMarketGenerator.generateTrade();
            System.out.println(trade.toString());
            assertAll(
                    ()->{
                        assertThat(trade.getAsset().getNotional()).isGreaterThan(seedNotional*FakeBondMarketGenerator.THRESHOLD);
                    },
                    ()->{
                        assertThat(trade.getAsset().getPrice()).isGreaterThan(seedPrice*FakeBondMarketGenerator.THRESHOLD);
                    },
                    ()->{
                        assertEquals(trade.getAsset().getCurrency(), currency);
                    },
                    ()->{
                        assertThat((Object)trade.getAsset().getSecurityId()).isIn((Object[])securityArray);
                    }
            );
            cnt++;
        }
    }
}