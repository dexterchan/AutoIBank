package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.web.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BondPriceDtoTest {

    @Test
    void builder() {
        BondPriceDto bondPriceDto = BondPriceDto.builder().bid(100).ask(110).build();
        assertEquals(bondPriceDto.getAsk(), 110);
    }
}