package io.exp.analysis.beam.pipeline;

import io.exp.analysis.beam.PipelineBuilder;
import io.exp.analysis.beam.utils.AnalysisOptions;
import io.exp.security.model.avro.BidAsk;
import io.exp.security.model.avro.BondTrade;
import lombok.Getter;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.io.Serializable;
import java.util.function.Function;

public interface BondTradeAnalysisPipelineBuilderInterface extends PipelineBuilder, Serializable {

    public Pipeline build(String[] args);

    public AnalysisProbes getAnalysisProbes();

    public AnalysisOptions getAnalysisOptions();

    @Getter
    public static class AnalysisProbes{
        PCollection<BondTrade> pBidTrades = null;
        PCollection<BondTrade> pAskTrades = null;
        PCollection<KV<String, Double>> pBidAvgPrice = null;
        PCollection<KV<String, Double>> pAskAvgPrice = null;
    }

    static Function<BidAsk, SerializableFunction<BondTrade, Boolean>> BidAskFilterFunc = (bidAsk)->
            new SerializableFunction<BondTrade, Boolean>() {
                @Override
                public Boolean apply(BondTrade bondTrade) {
                    return bondTrade.getAsset().getBidask()==bidAsk;
                }
            };
    static AnalysisProbes prepareAnalysisTransform(PCollection<BondTrade>pAllTrades){
        AnalysisProbes probes = new AnalysisProbes();
        probes.pBidTrades = pAllTrades.apply(Filter.by(BidAskFilterFunc.apply(BidAsk.BID)));
        probes.pAskTrades = pAllTrades.apply(Filter.by(BidAskFilterFunc.apply(BidAsk.ASK)));

        probes.pBidAvgPrice = probes.pBidTrades.apply(new BondTradeRealtimeAnalysisPipelineBuilder.CalculateBondAvgPrice());
        probes.pAskAvgPrice = probes.pAskTrades.apply(new BondTradeRealtimeAnalysisPipelineBuilder.CalculateBondAvgPrice());


        return probes;
    }
}
