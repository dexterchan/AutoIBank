package io.exp.analysis.DataSource;

import com.google.common.collect.Queues;
import io.exp.gateway.MarketGatewayInterface;
import io.exp.gateway.MarketRegistry;
import io.exp.gateway.observe.Observer;
import io.exp.gateway.observe.Subject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.io.UnboundedSource;
import org.joda.time.Duration;
import org.joda.time.Instant;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
public class MarketTradeUnboundedReader<T> extends UnboundedSource.UnboundedReader<T> implements Observer<T> {

    private final UnboundedSource<T, ?> marketSource;
    private final MarketGatewayInterface marketGatewayInterface;
    private Instant currentTimestamp;
    private Subject<T> tradeSubject;

    private T currentTrade;
    /**
     * This queue is provided to us to
     * receive trades from Exchanges so that it can be polled as nextTuple()
     */
    private final Queue<T> tradeQueue = Queues.newLinkedBlockingQueue();

    @Override
    public boolean start() throws IOException {
        log.info("Running market data subscription from Apache Beam");
        Observer<T> [] observers = new Observer[]{this, TradeObserverLogObserver};
        if (!this.marketGatewayInterface.connect()){
            return false;
        }
        tradeSubject = MarketRegistry.registerMarket(this.marketGatewayInterface, Arrays.asList(observers));
        return advance();
    }

    @Override
    public boolean advance() throws IOException {
        currentTrade = tradeQueue.poll();
        currentTimestamp = Instant.now();
        return (currentTrade != null);
    }

    @Override
    public Instant getWatermark() {
        return currentTimestamp.minus(new Duration(1));
    }

    @Override
    public UnboundedSource.CheckpointMark getCheckpointMark() {
        return new UnboundedSource.CheckpointMark() {
            @Override
            public void finalizeCheckpoint() throws IOException {}
        };
    }

    @Override
    public UnboundedSource<T, ?> getCurrentSource() {
        return this.marketSource;
    }

    @Override
    public void update(T msg) {
        tradeQueue.offer(msg);
    }

    @Override
    public void throwError(Throwable ex) {
        log.error(ex.getMessage());
    }

    @Override
    public String getDescription() {
        return "Apache Beam Observer";
    }

    @Override
    public T getCurrent() throws NoSuchElementException {
        if (currentTrade == null) {
            throw new NoSuchElementException();
        }
        return currentTrade;
    }

    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
        if (currentTrade == null) {
            throw new NoSuchElementException();
        }
        return currentTimestamp;
    }

    @Override
    public void close() throws IOException {
        marketGatewayInterface.unsubscribe();
    }

    final Observer<T> TradeObserverLogObserver = new Observer<T>() {
        @Override
        public void update(T msg) {
            log.debug(msg.toString());
        }

        @Override
        public void throwError(Throwable ex) {
            log.error(ex.getMessage());
        }

        @Override
        public String getDescription() {
            return "Log observer";
        }
    };
}
