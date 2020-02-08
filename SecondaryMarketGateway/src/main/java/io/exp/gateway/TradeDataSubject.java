package io.exp.gateway;

import com.google.common.collect.Sets;
import io.exp.gateway.observe.Observer;
import io.exp.gateway.observe.Subject;
import io.exp.security.model.avro.BondTrade;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;


public class TradeDataSubject implements Subject<BondTrade> {
    private Set<Observer> observerSet = Sets.newConcurrentHashSet();
    @Getter @Setter
    boolean active;
    @Getter
    String dataName;

    @Override
    public void registerObserver(Observer<BondTrade> o) {
        if (!observerSet.contains(o)) {
            observerSet.add(o);
        }
    }

    @Override
    public void removeObserver(Observer<BondTrade> o) {
        observerSet.remove(o);
    }

    @Override
    public void notifyOservers(BondTrade msg) {

    }

    @Override
    public void notifyObservers(Throwable ex) {

    }



    @Override
    public Map<String, String> getDescription() {
        return null;
    }
}
