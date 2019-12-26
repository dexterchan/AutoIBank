package io.exp.gateway.observe;

import com.google.common.collect.Queues;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public abstract class AsyncObserver<T> implements Observer<T> {
    BlockingQueue asyncQueue = Queues.newLinkedBlockingQueue();
    BlockingDeque asyncErrQueue = Queues.newLinkedBlockingDeque();
    ExecutorService executor = Executors.newFixedThreadPool(2);

    @Getter @Setter
    private boolean isAlive=true;

    @Override
    public void update(T msg){
        try {
            asyncQueue.put(msg);
            executor.execute(()->{
                while (isAlive) {
                    try {
                        T rmsg = (T) asyncQueue.take();
                        this.asyncUpdate(rmsg);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throwError(e);
                    }
                }
            });
        }catch(InterruptedException e){
            log.error(e.getMessage());
            throwError(e);
        }
    }

    @Override
    public void throwError(Throwable ex) {
        try {
            this.asyncErrQueue.put(ex);
            executor.execute(()->{
                while (isAlive) {
                    try {
                        Throwable t = (Throwable) asyncErrQueue.take();
                        this.asyncThrowError(t);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        this.asyncThrowError(e);
                    }
                }
            });
        }catch(InterruptedException e){
            log.error(e.getMessage());
            this.asyncThrowError(e);
        }
    }

    public abstract void asyncUpdate(T msg);

    public abstract void asyncThrowError( Throwable ex);


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.setAlive(false);
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

