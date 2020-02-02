package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("DUMMY")
@Service
public class DummyBondSecurityServiceImpl implements BondSecurityService {
    @Override
    public String[] getSecurities(String filterCriteria) {
        return new String[]{"BIDCISP691597243"};
    }
}
