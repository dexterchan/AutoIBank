package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Tag("integration")
class RedisBondSecurityServiceImplTest {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(RedisBondSecurityServiceImplTest.class);
    @Autowired
    BondSecurityService bondSecurityService;

    @Test
    void getSecurities() {
        String [] securities = bondSecurityService.getSecurities("*");

        assertThat(securities).hasSizeGreaterThan(0);
        Arrays.asList(securities).forEach(security->log.debug(security));

    }
}