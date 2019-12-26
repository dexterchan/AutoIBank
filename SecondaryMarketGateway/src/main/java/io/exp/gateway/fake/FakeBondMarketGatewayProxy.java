package io.exp.gateway.fake;

import io.exp.gateway.MarketGatewayInterface;
import io.exp.security.model.Trade;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FakeBondMarketGatewayProxy implements MarketGatewayInterface {
    private boolean isAlive = true;
    private int SLEEP_MS = 200;
    private FakeBondMarketGenerator fakeBondMarketGenerator = null;
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    public FakeBondMarketGatewayProxy(){
         double seedNotional = 1000000;
         double seedPrice = 100;
         String currency = "USD";
         double stdDevNtl = seedNotional* 0.1;
         double stdDevPrice = seedPrice * 0.1;
         String[] securityArray = new String[]{"ISIN1234", "ISIN4324", "CISP23434"};

         this.fakeBondMarketGenerator = new FakeBondMarketGenerator(seedNotional, seedPrice, currency, stdDevNtl, stdDevPrice, securityArray);
    }

    @Override
    public void subscribe(Consumer<Trade> tradeConsumer, Consumer<Throwable> throwableConsumer) {
        isAlive = true;
        executor.execute(()-> {
            while (isAlive) {
                Trade trade = this.fakeBondMarketGenerator.generateTrade();
                tradeConsumer.accept(trade);
                try {
                    Thread.sleep(SLEEP_MS);
                } catch (Exception ex) {
                }
            }
        });
    }

    @Override
    public void unsubscribe() {
        isAlive = false;
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
