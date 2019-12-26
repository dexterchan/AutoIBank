package io.exp.gateway.fake;

import io.exp.gateway.AbstractMarketGatewayFactory;
import io.exp.gateway.MarketGatewayInterface;

public class FakeMarketGatewayFactory implements AbstractMarketGatewayFactory {
    @Override
    public MarketGatewayInterface createMarketGateway(String venue, String identifier) {
        return new FakeBondMarketGatewayProxy();
    }
}
