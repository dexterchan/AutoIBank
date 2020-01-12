package io.exp.analysis.beam.pipeline.check;

import io.exp.security.model.BidAsk;
import io.exp.security.model.BondTrade;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.transforms.DoFn;

import static org.assertj.core.api.Assertions.assertThat;
@Slf4j
public class CheckBidAskTrade extends DoFn<BondTrade, Void> {
    BidAsk bidask=null;
    public CheckBidAskTrade(BidAsk bidAsk){
        this.bidask = bidAsk;
    }
    @ProcessElement
    public void processElement(@Element BondTrade bondTrade, OutputReceiver<Void> out) throws Exception {
        log.debug(bondTrade.toString());
        assertThat(bondTrade.getAsset().getBidAsk()).isEqualTo(this.bidask);
    }
}
