package io.exp.gateway;

import io.exp.security.model.Trade;

import java.util.function.Consumer;

public interface MarketGatewayInterface {
    void subscribe(Consumer<Trade> tradeConsumer, Consumer<Throwable> throwableConsumer);
    void unsubscribe();
}
