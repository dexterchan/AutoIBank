package io.exp.gateway.observe;

import com.google.common.collect.Sets;
import io.exp.security.model.Trade;
import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenericSubject implements Subject<Trade>{
    @Getter
    private String dataName;

    private Set<Observer> observerSet = Sets.newConcurrentHashSet();

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
        if (msg != null) {
            observerSet.forEach(o->{
                o.update(msg);
            });
        }
    }

    @Override
    public void notifyObservers(Throwable ex) {
        if (ex != null ) {
            observerSet.forEach(o->{
                o.throwError(ex);
            });
        }
    }

    @Override
    public Map<String, String> getDescription() {
        return null;
    }
}
