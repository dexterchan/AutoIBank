package io.exp.gateway;

import io.exp.security.model.Trade;

import java.util.function.Consumer;

public interface MarketGatewayInterface {
    void subsribe(Consumer<Trade> tradeConsumer, Consumer<Throwable> throwableConsumer);
}
