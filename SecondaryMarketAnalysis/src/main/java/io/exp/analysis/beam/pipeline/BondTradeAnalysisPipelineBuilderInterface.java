package io.exp.analysis.beam.pipeline;

import io.exp.analysis.beam.PipelineBuilder;
import io.exp.analysis.beam.pipeline.BondTradeRealtimeAnalysisPipelineBuilder;
import io.exp.security.model.BidAsk;
import io.exp.security.model.BondTrade;
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


    static class AnalysisProbes{
        @Getter
        PCollection<BondTrade> pBidTrades = null;
        @Getter
        PCollection<BondTrade> pAskTrades = null;
        @Getter
        PCollection<KV<String, Double>> pBidAvgPrice = null;
        @Getter
        PCollection<KV<String, Double>> pAskAvgPrice = null;
    }

    static Function<BidAsk, SerializableFunction<BondTrade, Boolean>> BidAskFilterFunc = (bidAsk)->
            new SerializableFunction<BondTrade, Boolean>() {
                @Override
                public Boolean apply(BondTrade bondTrade) {
                    return bondTrade.getAsset().getBidAsk()==bidAsk;
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
