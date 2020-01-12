package io.exp.gateway.fake;

import io.exp.gateway.MarketGatewayInterface;
import io.exp.security.model.BondTrade;
import io.exp.security.model.Trade;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
@Slf4j
public class FakeBondMarketGatewayProxy implements MarketGatewayInterface<BondTrade> {
    private static volatile boolean isAlive = true;
    private int SLEEP_MS = 50;
    private FakeBondMarketGenerator fakeBondMarketGenerator = null;
    private static ExecutorService executor = Executors.newFixedThreadPool(2,new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    });

    public FakeBondMarketGatewayProxy(){
         double seedNotional = 1000000;
         double seedPrice = 100;
         String currency = "USD";
         double stdDevNtl = seedNotional* 0.01;
         double stdDevPrice = seedPrice * 0.01;
         String[] securityArray = new String[]{"ISIN1234", "ISIN4324", "CISP23434"};

         this.fakeBondMarketGenerator = new FakeBondMarketGenerator(seedNotional, seedPrice, currency, stdDevNtl, stdDevPrice, securityArray);
    }

    @Override
    public boolean connect() {
        return true;
    }

    @Override
    public void subscribe(Consumer<BondTrade> tradeConsumer, Consumer<Throwable> throwableConsumer) {
        //isAlive = true;
        executor.execute(()-> {
            log.debug("Start reading market data");
            while (FakeBondMarketGatewayProxy.isAlive) {
                BondTrade bondTrade = this.fakeBondMarketGenerator.generateTrade();
                tradeConsumer.accept(bondTrade);
                try {
                    Thread.sleep(SLEEP_MS);
                } catch (Exception ex) {
                }
            }
            log.debug("Stop reading market data");
        });
    }

    @Override
    public void unsubscribe() {
        isAlive = false;
        log.debug("Stop subscription");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
