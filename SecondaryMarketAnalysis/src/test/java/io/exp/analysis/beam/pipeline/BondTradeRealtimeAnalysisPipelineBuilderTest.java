package io.exp.analysis.beam.pipeline;

import io.exp.analysis.beam.pipeline.BondTradeRealtimeAnalysisPipelineBuilder;
import io.exp.analysis.beam.pipeline.check.CheckBidAskAvg;
import io.exp.analysis.beam.pipeline.check.CheckBidAskTrade;
import io.exp.gateway.MarketGatewayInterface;
import io.exp.security.model.BidAsk;
import io.exp.security.model.BondTrade;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
@Slf4j
@Disabled
@Tag("integration")
class BondTradeRealtimeAnalysisPipelineBuilderTest {
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
    void testStreamingMarketData() throws Exception{
        BondTradeRealtimeAnalysisPipelineBuilder pipelineBuilder = new BondTradeRealtimeAnalysisPipelineBuilder();
        Pipeline pipeline = pipelineBuilder.build(arguments);

        MarketGatewayInterface marketGatewayInterface = pipelineBuilder.getMarketGatewayInterface();

        BondTradeRealtimeAnalysisPipelineBuilder.AnalysisProbes analysisProbes = pipelineBuilder.getAnalysisProbes();
        PCollection<BondTrade> pBidTrades = analysisProbes.pBidTrades;
        PCollection<BondTrade> pAskTrades = analysisProbes.pAskTrades;

        pBidTrades.apply("Check Bid Trades", ParDo.of(new CheckBidAskTrade(BidAsk.BID)));
        pAskTrades.apply("Check Ask Trades", ParDo.of(new CheckBidAskTrade(BidAsk.ASK)));

        PCollection<KV<String, Double>> pBidAvgPrice = analysisProbes.pBidAvgPrice;
        PCollection<KV<String, Double>> pAskAvgPrice = analysisProbes.pAskAvgPrice;
        pBidAvgPrice.apply("Check Bid Avg Price", ParDo.of(new CheckBidAskAvg(BidAsk.BID)));
        pAskAvgPrice.apply("Check Ask Avg Price", ParDo.of(new CheckBidAskAvg(BidAsk.ASK)));

        PipelineResult pipelineResult = pipeline.run();

        Thread.sleep(1000* 10);
        marketGatewayInterface.unsubscribe();
        //pipelineResult.waitUntilFinish();
        //pipelineResult.cancel();
        log.debug("End of testing");

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