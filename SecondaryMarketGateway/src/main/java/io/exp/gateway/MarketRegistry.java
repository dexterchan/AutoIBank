package io.exp.gateway;

import io.exp.gateway.observe.GenericSubject;
import io.exp.gateway.observe.Observer;
import io.exp.gateway.observe.Subject;
import io.exp.security.model.Trade;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MarketRegistry {
    private static ExecutorService executorService = Executors.newFixedThreadPool(10,new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    });
    public static <T>  Subject<T> registerMarket(MarketGatewayInterface<T> marketGatewayInterface, List<Observer<T> > observerList){
        Subject<T> subject = new GenericSubject();
        observerList.forEach( observer->{
            subject.registerObserver(observer);
        });
        executorService.execute(()->{
            marketGatewayInterface.subscribe(
                    trade ->subject.notifyOservers(trade),
                    throwable -> subject.notifyObservers(throwable)
            );
        });
        return subject;
    }
}
