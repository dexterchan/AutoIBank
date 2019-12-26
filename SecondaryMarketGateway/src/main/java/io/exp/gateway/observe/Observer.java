package io.exp.gateway.observe;

public interface Observer <T> {
    void update(T msg);
    void throwError(Throwable ex);
    String getDescription();
}