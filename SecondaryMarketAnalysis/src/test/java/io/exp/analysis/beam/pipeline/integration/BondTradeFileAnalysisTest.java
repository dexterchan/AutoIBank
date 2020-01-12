package io.exp.analysis.beam.pipeline.integration;

import com.google.gson.Gson;
import io.exp.analysis.beam.datasink.DataSinkBuilder;
import io.exp.analysis.beam.datasink.RedisDataSinkBuilder;
import io.exp.analysis.beam.pipeline.BondTradeAnalysisPipelineBuilderInterface;
import io.exp.analysis.beam.pipeline.BondTradeFileAnalysisPipelineBuilder;
import io.exp.analysis.beam.pipeline.BondTradeRealtimeAnalysisPipelineBuilder;

import io.exp.security.model.BidAsk;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;

@Tag("integration")
public class BondTradeFileAnalysisTest {
    private String [] arguments = null;
    private static final String referenceBidRefJson = "resources/bid.bidAskAvg.json";
    private static final String referenceAskRefJson = "resources/ask.bidAskAvg.json";
    private static Map<String, Double> bidReference = null;
    private static Map<String, Double> askReference = null;

    private static String RedisHostname = "localhost";
    private static int RedisPort = 6379;
    @BeforeAll
    public static void initAll() throws Exception{
        Gson gson = new Gson();
        Reader bidReader = new FileReader(referenceBidRefJson);
        Reader askReader = new FileReader(referenceAskRefJson);
        bidReference = gson.fromJson(bidReader, Map.class);
        askReference = gson.fromJson(askReader, Map.class);
    }
    @BeforeEach
    public void init(){
        arguments= new String[]{
                "--fileName=resources/bondmarket.avro"};
    }

    @Test
    void test_avrofile_analysis2RedisOut() throws FileNotFoundException {
        BondTradeAnalysisPipelineBuilderInterface pipelineBuilder= new BondTradeFileAnalysisPipelineBuilder();
        Pipeline pipeline = pipelineBuilder.build(arguments);

        BondTradeRealtimeAnalysisPipelineBuilder.AnalysisProbes analysisProbes = pipelineBuilder.getAnalysisProbes();

        PCollection<KV<String, Double>> pBidAvgPrice = analysisProbes.getPBidAvgPrice();
        PCollection<KV<String, Double>> pAskAvgPrice = analysisProbes.getPAskAvgPrice();

        DataSinkBuilder bidDataSinkBuilder = new RedisDataSinkBuilder(RedisHostname, RedisPort, BidAsk.BID);
        bidDataSinkBuilder.build(pBidAvgPrice);

        DataSinkBuilder askDataSinkBuilder = new RedisDataSinkBuilder(RedisHostname, RedisPort, BidAsk.ASK);
        askDataSinkBuilder.build(pAskAvgPrice);

        PipelineResult pipelineResult = pipeline.run();
        pipelineResult.waitUntilFinish();
    }
}
