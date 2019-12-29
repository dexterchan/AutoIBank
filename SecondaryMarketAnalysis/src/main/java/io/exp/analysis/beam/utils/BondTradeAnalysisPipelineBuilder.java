package io.exp.analysis.beam.utils;

import com.google.common.collect.Maps;
import io.exp.analysis.DataSource.MarketTradeUnboundedSource;
import io.exp.analysis.beam.PipelineBuilder;
import io.exp.gateway.AbstractMarketGatewayFactory;
import io.exp.gateway.MarketGatewayInterface;
import io.exp.gateway.fake.FakeBondMarketGatewayFactory;

import io.exp.security.model.BidAsk;
import io.exp.security.model.BondTrade;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.io.UnboundedSource;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.annotations.VisibleForTesting;
import org.joda.time.Duration;

import javax.annotation.Nonnull;
import java.io.Serializable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class BondTradeAnalysisPipelineBuilder implements PipelineBuilder, Serializable {

    private static String DEFAULT="DEFAULT";
    private static final Map<String, AbstractMarketGatewayFactory<BondTrade> > MARKET_GATEWAY_FACTORY_MAP = Maps.newHashMap();
    static{
        MARKET_GATEWAY_FACTORY_MAP.put("FAKE", new FakeBondMarketGatewayFactory());
        MARKET_GATEWAY_FACTORY_MAP.put(DEFAULT, new FakeBondMarketGatewayFactory());
    }

    @Getter
    PCollection<BondTrade> pAllTrades=null;

    @Getter
    PCollection<BondTrade> pBidTrades = null;
    @Getter
    PCollection<BondTrade> pAskTrades = null;

    @Getter
    MarketGatewayInterface<BondTrade> marketGatewayInterface=null;

    @Nonnull
    @Override
    public Pipeline build(String[] args) {
        PipelineOptionsFactory.register(AnalysisOptions.class);
        AnalysisOptions analysisOptions = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .withoutStrictParsing()
                .create()
                .as(AnalysisOptions.class);
        analysisOptions.setStreaming(true);

        Pipeline pipeline = Pipeline.create(analysisOptions);

        String venue = analysisOptions.getVenue().toUpperCase();
        String identifier = analysisOptions.getIdentifier().toUpperCase();

        String pipeLineName = String.format("%s_%s",venue, identifier);

        pAllTrades = pipeline.apply(pipeLineName, Read.from(getMarketDataSource(venue, identifier)));


        AnalysisProbes analysisProbes = prepareAnalysisTransform( pAllTrades);
        this.pBidTrades = analysisProbes.pBidTrades;
        this.pAskTrades = analysisProbes.pAskTrades;

        return pipeline;
    }
    private static class AnalysisProbes{
        PCollection<BondTrade> pBidTrades = null;
        PCollection<BondTrade> pAskTrades = null;
    }

    private static AnalysisProbes prepareAnalysisTransform(PCollection<BondTrade>pAllTrades ){
        AnalysisProbes probes = new AnalysisProbes();

        PCollection<BondTrade> pAllTradesTimeStamp = pAllTrades.apply(
                "AddEventTimestamps",
                WithTimestamps.of((BondTrade i) -> i.getTimestamp().plus(5000)));
        probes.pBidTrades = pAllTradesTimeStamp.apply(Filter.by(BidAskFilterFunc.apply(BidAsk.BID)));
        probes.pAskTrades = pAllTradesTimeStamp.apply(Filter.by(BidAskFilterFunc.apply(BidAsk.ASK)));

        return probes;
    }

    static Function<BidAsk,SerializableFunction<BondTrade, Boolean>  > BidAskFilterFunc = (bidAsk)->
         new SerializableFunction<BondTrade, Boolean>() {
            @Override
            public Boolean apply(BondTrade bondTrade) {
                return bondTrade.getAsset().getBidAsk()==bidAsk;
            }
        };


    @VisibleForTesting
    static class CalculateBondAvgPrice
            extends PTransform<PCollection<BondTrade>, PCollection<KV<String, Double>>> {
        private final Duration tradeWindowDuration;
        private final Duration allowedLateness;

        CalculateBondAvgPrice(Duration teamWindowDuration, Duration allowedLateness) {
            this.tradeWindowDuration = teamWindowDuration;
            this.allowedLateness = allowedLateness;
        }
        @Override
        public PCollection<KV<String, Double>> expand(PCollection<BondTrade> input) {
            return null;
        }
    }

    private UnboundedSource<BondTrade, ?> getMarketDataSource(String venue, String identifier){
        MarketGatewayInterface<BondTrade> marketGatewayInterface = this.getMarketGatewayInterface(venue, identifier);
        UnboundedSource<BondTrade, ?> marketSource = new MarketTradeUnboundedSource(marketGatewayInterface, BondTrade.class);
        return marketSource;
    }
    private MarketGatewayInterface<BondTrade> getMarketGatewayInterface(String venue, String identifier){
        AbstractMarketGatewayFactory<BondTrade> marketGatewayFactory= Optional.ofNullable(MARKET_GATEWAY_FACTORY_MAP.get(venue)).orElse(MARKET_GATEWAY_FACTORY_MAP.get(DEFAULT));
        marketGatewayInterface = marketGatewayFactory.createMarketGateway(venue, identifier);
        return marketGatewayInterface;
    }


}
