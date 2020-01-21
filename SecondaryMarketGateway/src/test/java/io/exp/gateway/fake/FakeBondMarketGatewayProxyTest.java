package io.exp.gateway.fake;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FakeBondMarketGatewayProxyTest {
    @Test
    public void testSecurityGen(){
        int numOfSec = 10;
        String [] securityName = FakeBondMarketGatewayProxy.createSecurityNames(numOfSec);
        assertThat(securityName).hasSize(numOfSec);
    }

}