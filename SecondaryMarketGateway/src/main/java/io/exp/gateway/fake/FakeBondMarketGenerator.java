package io.exp.gateway.fake;

import io.exp.security.model.avro.AbstractTradeFactory;
import io.exp.security.model.avro.BidAsk;
import io.exp.security.model.avro.BondTrade;
import io.exp.security.model.avro.BondTradeFactoryImpl;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@RequiredArgsConstructor
public class FakeBondMarketGenerator implements Serializable{
    protected double seedNotional;
    protected double seedPrice;
    protected String currency;
    protected double stddevNtl;
    protected double stddevPrice;
    protected String [] securityArray;
    static AbstractTradeFactory tradeFactory = null;


    static double THRESHOLD = 0.001;
    static{
        tradeFactory = new BondTradeFactoryImpl();
    }

    public FakeBondMarketGenerator(double seedNotional, double seedPrice, String currency, double i, double v, String[] securityArray) {
        this.seedNotional=seedNotional;
        this.seedPrice=seedPrice;
        this.currency=currency;
        this.stddevNtl=i;
        this.stddevPrice=v;
        this.securityArray = securityArray;
    }

    static double randomValue(double mean, double stddev){
        return Math.max(THRESHOLD*mean, mean + ThreadLocalRandom.current().nextGaussian() * stddev);
    }
    public BondTrade generateTrade(){
        String security = securityArray[ThreadLocalRandom.current().nextInt(0, securityArray.length)];
        int bidAskInt = ThreadLocalRandom.current().nextInt(0,2);
        BidAsk bidask = bidAskInt==0?BidAsk.BID:BidAsk.ASK;
        double notional = randomValue(seedNotional, stddevNtl);
        double price = randomValue(bidask==BidAsk.ASK?seedPrice*1.05:seedPrice*0.95, stddevPrice);
        String cust = "ABCD";
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String tradeDate = LocalDateTime
                .ofInstant(Instant.now(), ZoneOffset.UTC)
                .format(formatter);
        BondTrade trade = (BondTrade)tradeFactory.createTrade(security, cust, tradeDate, notional, price, currency, bidask);
        return trade;
    }

}
