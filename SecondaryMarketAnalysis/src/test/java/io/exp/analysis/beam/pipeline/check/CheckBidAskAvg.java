package io.exp.analysis.beam.pipeline.check;

import io.exp.security.model.BidAsk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
@Slf4j
@RequiredArgsConstructor
public class CheckBidAskAvg extends DoFn<KV<String, Double>, Void > {
    private final BidAsk bidask;

    @ProcessElement
    public void processElement(@Element KV<String, Double> entry, OutputReceiver<Void> out){
        log.debug(String.format("%s: %s %f", this.bidask, entry.getKey(), entry.getValue()));
    }
}