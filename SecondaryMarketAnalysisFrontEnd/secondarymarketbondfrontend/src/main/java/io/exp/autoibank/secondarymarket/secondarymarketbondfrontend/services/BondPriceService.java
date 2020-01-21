package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services;


import io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.web.model.BondPriceDto;
import org.springframework.stereotype.Service;


public interface BondPriceService {
    BondPriceDto getBondPrice(String identifier);

}
