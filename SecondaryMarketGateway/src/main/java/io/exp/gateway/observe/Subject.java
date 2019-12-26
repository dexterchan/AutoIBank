package io.exp.gateway.observe;

import java.util.Map;

public interface Subject <T> {
    void registerObserver( Observer<T> o);
    void removeObserver( Observer<T> o);
    void notifyOservers(T msg);
    void notifyObservers(Throwable ex);

    String getDataName();
    Map<String, String> getDescription();
}
