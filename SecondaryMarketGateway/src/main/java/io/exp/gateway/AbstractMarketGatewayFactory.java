package io.exp.gateway;

public interface AbstractMarketGatewayFactory<T> {
    MarketGatewayInterface<T> createMarketGateway(String venue, String identifier);
}
