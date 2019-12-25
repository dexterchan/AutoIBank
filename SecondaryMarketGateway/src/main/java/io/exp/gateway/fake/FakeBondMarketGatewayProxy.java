package io.exp.gateway.fake;

import io.exp.gateway.MarketGatewayInterface;
import io.exp.security.model.Trade;

import java.util.function.Consumer;

public class FakeBondMarketGatewayProxy implements MarketGatewayInterface {
    @Override
    public void subsribe(Consumer<Trade> tradeConsumer, Consumer<Throwable> throwableConsumer) {

    }
}
