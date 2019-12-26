package io.exp.gateway;

import com.google.common.collect.Sets;
import io.exp.gateway.observe.Observer;
import io.exp.gateway.observe.Subject;
import io.exp.security.model.Trade;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class TradeDataSubject implements Subject<Trade> {
    private Set<Observer> observerSet = Sets.newConcurrentHashSet();
    @Getter @Setter
    boolean active;
    @Getter
    String dataName;

    @Override
    public void registerObserver(Observer<Trade> o) {
        if (!observerSet.contains(o)) {
            observerSet.add(o);
        }
    }

    @Override
    public void removeObserver(Observer<Trade> o) {
        observerSet.remove(o);
    }

    @Override
    public void notifyOservers(Trade msg) {

    }

    @Override
    public void notifyObservers(Throwable ex) {

    }



    @Override
    public Map<String, String> getDescription() {
        return null;
    }
}
