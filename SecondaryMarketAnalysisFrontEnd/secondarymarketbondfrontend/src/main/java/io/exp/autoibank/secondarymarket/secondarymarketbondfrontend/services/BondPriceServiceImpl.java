package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services;


import io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.web.model.BondPriceDto;
import org.springframework.stereotype.Service;



public class BondPriceServiceImpl implements BondPriceService {

    @Override
    public BondPriceDto getBondPrice(String identifier) {
        BondPriceDto bondPriceDto = BondPriceDto.builder().identifier(identifier).bid(90).ask(100).build();

        return bondPriceDto;
    }
}
