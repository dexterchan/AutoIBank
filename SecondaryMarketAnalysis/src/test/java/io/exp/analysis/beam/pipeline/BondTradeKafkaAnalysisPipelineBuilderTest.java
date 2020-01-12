package io.exp.analysis.beam.pipeline;

import io.exp.analysis.beam.datasink.DataSinkBuilder;
import io.exp.analysis.beam.datasink.RedisDataSinkBuilder;
import io.exp.analysis.beam.pipeline.check.CheckBidAskAvg;
import io.exp.analysis.beam.pipeline.check.CheckBidAskTrade;
import io.exp.security.model.BidAsk;
import io.exp.security.model.BondTrade;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("kafka")
class BondTradeKafkaAnalysisPipelineBuilderTest {
    private String [] arguments = null;
    private static String RedisHostname = "localhost";
    private static int RedisPort = 6379;
    @BeforeEach
    public void init(){
        arguments= new String[]{
                "--windowDuration=1000"
        };
    }
    @Test
    void debug_avrokafka_analysis2RedisOut() {
        BondTradeAnalysisPipelineBuilderInterface pipelineBuilder= new BondTradeKafkaAnalysisPipelineBuilder();
        Pipeline pipeline = pipelineBuilder.build(arguments);

        BondTradeRealtimeAnalysisPipelineBuilder.AnalysisProbes analysisProbes = pipelineBuilder.getAnalysisProbes();
        PCollection<BondTrade> pBidTrades = analysisProbes.pBidTrades;
        PCollection<BondTrade> pAskTrades = analysisProbes.pAskTrades;

        pBidTrades.apply("Check Bid Trades", ParDo.of(new CheckBidAskTrade(BidAsk.BID)));
        pAskTrades.apply("Check Ask Trades", ParDo.of(new CheckBidAskTrade(BidAsk.ASK)));

        PCollection<KV<String, Double>> pBidAvgPrice = analysisProbes.pBidAvgPrice;
        PCollection<KV<String, Double>> pAskAvgPrice = analysisProbes.pAskAvgPrice;
        pBidAvgPrice.apply("Check Bid Avg Price", ParDo.of(new CheckBidAskAvg(BidAsk.BID)));
        pAskAvgPrice.apply("Check Ask Avg Price", ParDo.of(new CheckBidAskAvg(BidAsk.ASK)));

        DataSinkBuilder bidDataSinkBuilder = new RedisDataSinkBuilder(RedisHostname, RedisPort, BidAsk.BID);
        bidDataSinkBuilder.build(pBidAvgPrice);

        DataSinkBuilder askDataSinkBuilder = new RedisDataSinkBuilder(RedisHostname, RedisPort, BidAsk.ASK);
        askDataSinkBuilder.build(pAskAvgPrice);

        PipelineResult pipelineResult = pipeline.run();
    }
}