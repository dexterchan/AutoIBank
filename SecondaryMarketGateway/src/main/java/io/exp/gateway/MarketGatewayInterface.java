package io.exp.gateway;

import java.io.Serializable;
import java.util.function.Consumer;

public interface MarketGatewayInterface<T> extends Serializable {
    boolean connect();
    void subscribe(Consumer<T> tradeConsumer, Consumer<Throwable> throwableConsumer);
    void unsubscribe();
}
