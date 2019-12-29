package io.exp.analysis.beam.utils;

import io.exp.gateway.MarketGatewayInterface;
import io.exp.security.model.BidAsk;
import io.exp.security.model.BondTrade;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
@Slf4j
class BondTradeAnalysisPipelineBuilderTest {
    String [] arguments = null;

    @BeforeEach
    public void init(){
        arguments= new String[]{
                "--windowDuration=500",
                "--venue=fake",
                "--identifier=abcd"
        };
    }
    @Test
    void build() throws Exception{
        BondTradeAnalysisPipelineBuilder pipelineBuilder = new BondTradeAnalysisPipelineBuilder();
        Pipeline pipeline = pipelineBuilder.build(arguments);

        MarketGatewayInterface marketGatewayInterface = pipelineBuilder.getMarketGatewayInterface();

        PCollection<BondTrade> pBidTrades = pipelineBuilder.getPBidTrades();
        PCollection<BondTrade> pAskTrades = pipelineBuilder.getPAskTrades();
        //PAssert.that(pTrade).satisfies(new CheckTrade());

        pBidTrades.apply("Check Bid Trades", ParDo.of(new CheckBidAskTrade(BidAsk.BID)));
        pAskTrades.apply("Check Ask Trades", ParDo.of(new CheckBidAskTrade(BidAsk.ASK)));
        PipelineResult pipelineResult = pipeline.run();

        Thread.sleep(1000* 10);
        marketGatewayInterface.unsubscribe();
        //pipelineResult.waitUntilFinish();
        //pipelineResult.cancel();
        log.debug("End of testing");

    }

    static class CheckBidAskTrade extends DoFn<BondTrade, Void> {
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

    static class CheckTrade implements SerializableFunction<Iterable<BondTrade>, Void>{
        @Override
        public Void apply(Iterable<BondTrade> input) {
            for (BondTrade trade: input){
                System.out.println(trade.toString());
            }
            return null;
        }
    }
}