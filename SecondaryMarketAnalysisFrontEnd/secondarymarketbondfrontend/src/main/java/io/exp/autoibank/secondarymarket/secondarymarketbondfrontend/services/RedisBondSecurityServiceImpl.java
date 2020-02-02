package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services;

import io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.utils.JedisConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@Service
@Profile({"LIVE", "default"})
public class RedisBondSecurityServiceImpl implements BondSecurityService {
    @Autowired
    JedisConnectionFactory jedisConnectionFactory;
    @Override
    public String[] getSecurities(String filterCriteria) {
        Jedis jedis = null;
        try {
            jedis = jedisConnectionFactory.getRedisConnection();
            Set<String> securities = jedis.keys("[ASK|BID]*");
            Set<String> str = securities.stream().map(s->s.substring(3))
                    .collect(Collectors.toSet());
            return str.stream().toArray(String[]::new);
        }catch(Exception e){
            log.error(e.getMessage(), e);
        }finally{
            Optional.ofNullable(jedis).ifPresent(j->j.close());
        }
        return new String[0];
    }
}
