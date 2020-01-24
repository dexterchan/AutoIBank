package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services;

import io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.utils.JedisConnectionFactory;
import io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.web.model.BondPriceDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedisBondPriceServiceImpl implements BondPriceService {
    //https://partners-intl.aliyun.com/help/doc-detail/98726.htm
    @Autowired
    JedisConnectionFactory jedisConnectionFactory;

    @Override
    public BondPriceDto getBondPrice(String identifier) {
        Jedis jedis = null;
        BondPriceDto bondPriceDto = null;
        try {
            jedis = jedisConnectionFactory.getRedisConnection();
            // Specific commands

            String bid = jedis.get(String.format("BID%s",identifier));
            String ask = jedis.get(String.format("ASK%s",identifier));
            Double bid_value = Optional.ofNullable(bid).map(d->Double.parseDouble(d)).orElse(Double.NaN);
            Double ask_value = Optional.ofNullable(ask).map(d->Double.parseDouble(d)).orElse(Double.NaN);
            bondPriceDto = BondPriceDto.builder().identifier(identifier)
                    .bid(bid_value)
                    .ask(ask_value).build();
            log.debug(bondPriceDto.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            //In JedisPool mode, the Jedis resource will be returned to the resource pool.
            Optional.ofNullable(jedis).ifPresent(j->j.close());
        }


        return bondPriceDto;
    }


}
