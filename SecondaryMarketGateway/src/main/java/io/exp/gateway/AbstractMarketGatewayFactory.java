package io.exp.gateway;

public interface AbstractMarketGatewayFactory {
    MarketGatewayInterface createMarketGateway(String venue, String identifier);
}
