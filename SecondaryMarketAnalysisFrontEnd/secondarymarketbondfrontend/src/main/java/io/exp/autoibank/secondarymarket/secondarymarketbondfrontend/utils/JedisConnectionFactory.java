package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.utils;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class JedisConnectionFactory {
    private volatile JedisPool jedisPool;
    @Value( "${bondpricesvc.redisHost}" )
    private  String redisHost;

    @Value( "${bondpricesvc.redisPort}" )
    private  int redisPort;

    @Value( "${bondpricesvc.timeout}" )
    private  int timeout;

    private JedisPool getJedisPool(){
        if (jedisPool==null) {
            synchronized (this) {
                if (jedisPool == null){
                    GenericObjectPoolConfig jedisPoolConfig = new GenericObjectPoolConfig();
                    jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, timeout);;
                }
            }
        }
        return jedisPool;
    }
    public  Jedis getRedisConnection(){
        return getJedisPool().getResource();
    }
}
