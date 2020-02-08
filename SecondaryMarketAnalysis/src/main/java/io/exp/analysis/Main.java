package io.exp.analysis;

import io.exp.analysis.beam.datasink.DataSinkBuilder;
import io.exp.analysis.beam.datasink.RedisDataSinkBuilder;
import io.exp.analysis.beam.pipeline.BondTradeAnalysisPipelineBuilderInterface;
import io.exp.analysis.beam.pipeline.BondTradeKafkaAnalysisPipelineBuilder;
import io.exp.analysis.beam.pipeline.BondTradeRealtimeAnalysisPipelineBuilder;
import io.exp.analysis.beam.utils.AnalysisOptions;
import io.exp.security.model.avro.BidAsk;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

public class Main {
    public static void main(String[] arguments){
        BondTradeAnalysisPipelineBuilderInterface pipelineBuilder= new BondTradeKafkaAnalysisPipelineBuilder();
        Pipeline pipeline = pipelineBuilder.build(arguments);

        BondTradeRealtimeAnalysisPipelineBuilder.AnalysisProbes analysisProbes = pipelineBuilder.getAnalysisProbes();


        PCollection<KV<String, Double>> pBidAvgPrice = analysisProbes.getPBidAvgPrice();
        PCollection<KV<String, Double>> pAskAvgPrice = analysisProbes.getPAskAvgPrice();

        pBidAvgPrice.apply("Check Bid Avg Price", ParDo.of(new CheckBidAskAvg(BidAsk.BID)));
        pAskAvgPrice.apply("Check Ask Avg Price", ParDo.of(new CheckBidAskAvg(BidAsk.ASK)));

        AnalysisOptions analysisOptions = pipelineBuilder.getAnalysisOptions();

        DataSinkBuilder bidDataSinkBuilder = new RedisDataSinkBuilder(analysisOptions.getRedisHostname(), analysisOptions.getRedisPort(), BidAsk.BID);
        bidDataSinkBuilder.build(pBidAvgPrice);

        DataSinkBuilder askDataSinkBuilder = new RedisDataSinkBuilder(analysisOptions.getRedisHostname(), analysisOptions.getRedisPort(),  BidAsk.ASK);
        askDataSinkBuilder.build(pAskAvgPrice);

        PipelineResult pipelineResult = pipeline.run();
    }

}

@Slf4j
@Getter
@RequiredArgsConstructor
class CheckBidAskAvg extends DoFn<KV<String, Double>, Void > {
    private final BidAsk bidask;

    @ProcessElement
    public void processElement(@Element KV<String, Double> entry, OutputReceiver<Void> out){
        log.debug(String.format("%s: %s %f", this.bidask, entry.getKey(), entry.getValue()));
    }
}
