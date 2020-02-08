package io.exp.gateway.fake;

import io.exp.gateway.MarketGatewayInterface;
import io.exp.security.model.avro.BondTrade;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class FakeBondMarketGatewayProxy implements MarketGatewayInterface<BondTrade> {
    private static volatile boolean isAlive = true;
    private static final int numberOfSecurities = 10;
    private int SLEEP_MS = 50;
    private FakeBondMarketGenerator fakeBondMarketGenerator = null;
    private static ExecutorService executor = Executors.newFixedThreadPool(2,new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    });

    static String[] createSecurityNames(int numberOfSecurities){
        List<String> isin = IntStream.range(0,numberOfSecurities/2).boxed().map(
                i-> {
                    long number = ThreadLocalRandom.current().nextLong(1000000000);
                    return String.format("ISIN%09d", number);
                }
        ).collect(Collectors.toList());
        List<String> cisp = IntStream.range(0,numberOfSecurities/2).boxed().map(
                i-> {
                    long number = ThreadLocalRandom.current().nextLong(1000000000);
                    return String.format("CISP%09d", number);
                }
        ).collect(Collectors.toList());
        isin.addAll(cisp);
        return isin.toArray(String[]::new);
    }
    public FakeBondMarketGatewayProxy(){
         double seedNotional = 1000000;
         double seedPrice = 100;
         String currency = "USD";
         double stdDevNtl = seedNotional* 0.01;
         double stdDevPrice = seedPrice * 0.01;
         String[] securityArray = createSecurityNames(numberOfSecurities);

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
