package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services;

import io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.web.model.BondPriceDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;




@SpringBootTest
@Tag("integration")
class RedisBondPriceServiceImplTest {

    @Autowired
    private BondPriceService bondPriceService;

    @Test
    void happyPathGetBondPrice() {
        String identifier ="CISP23434";

        BondPriceDto bondPriceDto = bondPriceService.getBondPrice(identifier);
        assertAll(
                ()->assertNotNull(bondPriceDto),
                ()->assertNotEquals(bondPriceDto.getBid(), Double.NaN),
                ()->assertNotEquals(bondPriceDto.getAsk(), Double.NaN),
                ()->assertNotEquals(bondPriceDto.getBid(), bondPriceDto.getAsk(), 0.00001)
        );
    }

    @Test
    void bondNotFound(){
        String identifier ="ABCD";
        BondPriceDto bondPriceDto = bondPriceService.getBondPrice(identifier);

        assertAll(
                ()->assertNotNull(bondPriceDto),
                ()->assertEquals(bondPriceDto.getBid(), Double.NaN),
                ()->assertEquals(bondPriceDto.getAsk(), Double.NaN)
        );
    }


}