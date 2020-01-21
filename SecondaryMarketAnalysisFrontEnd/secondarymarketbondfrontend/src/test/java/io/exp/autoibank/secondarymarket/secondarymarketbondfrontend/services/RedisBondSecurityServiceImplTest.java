package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Tag("integration")
@Slf4j
class RedisBondSecurityServiceImplTest {
    @Autowired
    BondSecurityService bondSecurityService;

    @Test
    void getSecurities() {
        String [] securities = bondSecurityService.getSecurities("*");

        assertThat(securities).hasSizeGreaterThan(0);
        Arrays.asList(securities).forEach(security->log.debug(security));

    }
}