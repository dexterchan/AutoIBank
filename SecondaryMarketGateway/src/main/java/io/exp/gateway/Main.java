package io.exp.gateway;

import io.exp.gateway.fake.FakeMarketGatewayFactory;
import io.exp.gateway.observe.Observer;
import io.exp.security.model.Trade;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class Main {

    final static Observer<Trade> TradeObserverLogObserver = new Observer<Trade>() {
        @Override
        public void update(Trade msg) {
            log.debug(msg.toString());
        }

        @Override
        public void throwError(Throwable ex) {
            log.error(ex.getMessage());
        }

        @Override
        public String getDescription() {
            return "Log observer";
        }
    };
    public static void main(String []args){
        log.info("Running Gateway");
        AbstractMarketGatewayFactory marketGatewayFactory = new FakeMarketGatewayFactory();
        MarketGatewayInterface marketGatewayInterface = marketGatewayFactory.createMarketGateway("test", "ABCD");
        Observer<Trade> [] observers = new Observer[]{TradeObserverLogObserver};
        MarketRegistry.registerMarket(marketGatewayInterface, Arrays.asList(observers));


    }
}
