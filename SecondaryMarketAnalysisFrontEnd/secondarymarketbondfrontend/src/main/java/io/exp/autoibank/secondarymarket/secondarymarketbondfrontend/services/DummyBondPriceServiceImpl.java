package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services;


import io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.web.model.BondPriceDto;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Service
@Primary
public class DummyBondPriceServiceImpl implements BondPriceService {

    @Override
    public BondPriceDto getBondPrice(String identifier) {
        BondPriceDto bondPriceDto = BondPriceDto.builder().identifier(identifier).bid(90).ask(100).build();

        return bondPriceDto;
    }
}
