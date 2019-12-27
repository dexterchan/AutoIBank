package io.exp.gateway.fake;

import io.exp.gateway.AbstractMarketGatewayFactory;
import io.exp.gateway.MarketGatewayInterface;
import io.exp.security.model.BondTrade;

public class FakeBondMarketGatewayFactory implements AbstractMarketGatewayFactory<BondTrade> {
    @Override
    public MarketGatewayInterface<BondTrade> createMarketGateway(String venue, String identifier) {
        return new FakeBondMarketGatewayProxy();
    }
}
