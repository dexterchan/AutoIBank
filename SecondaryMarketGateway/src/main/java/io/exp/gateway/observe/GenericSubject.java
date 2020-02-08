package io.exp.gateway.observe;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

public class GenericSubject<T> implements Subject<T>{
    @Getter
    private String dataName;

    private Set<Observer> observerSet = Sets.newConcurrentHashSet();

    @Override
    public void registerObserver(Observer<T> o) {
        if (!observerSet.contains(o)) {
            observerSet.add(o);
        }
    }

    @Override
    public void removeObserver(Observer<T> o) {
        observerSet.remove(o);
    }

    @Override
    public void notifyOservers(T msg) {
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
