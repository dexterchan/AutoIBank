package io.exp.gateway;

import io.exp.gateway.observe.GenericSubject;
import io.exp.gateway.observe.Observer;
import io.exp.gateway.observe.Subject;
import io.exp.security.model.Trade;

import java.util.List;

public class MarketRegistry {

    public static  Subject<Trade> registerMarket(MarketGatewayInterface marketGatewayInterface, List<Observer<Trade> > observerList){
        Subject<Trade> subject = new GenericSubject();
        observerList.forEach( observer->{
            subject.registerObserver(observer);
        });
        marketGatewayInterface.subscribe(
                trade ->subject.notifyOservers(trade),
                throwable -> subject.notifyObservers(throwable)
        );
        return subject;
    }
}
