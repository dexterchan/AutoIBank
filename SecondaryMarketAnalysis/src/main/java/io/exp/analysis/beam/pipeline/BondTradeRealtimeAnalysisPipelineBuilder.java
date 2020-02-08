package io.exp.analysis.beam.pipeline;

import com.google.common.collect.Maps;
import io.exp.analysis.DataSource.MarketTradeUnboundedSource;
import io.exp.analysis.beam.utils.AnalysisOptions;
import io.exp.gateway.AbstractMarketGatewayFactory;
import io.exp.gateway.MarketGatewayInterface;
import io.exp.gateway.fake.FakeBondMarketGatewayFactory;

import io.exp.security.model.avro.BondTrade;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.io.UnboundedSource;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.annotations.VisibleForTesting;
import org.joda.time.Duration;

import javax.annotation.Nonnull;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class BondTradeRealtimeAnalysisPipelineBuilder implements BondTradeAnalysisPipelineBuilderInterface {
    static final Duration FIVE_MINUTES = Duration.standardMinutes(5);
    static final Duration TEN_MINUTES = Duration.standardMinutes(10);

    private static String DEFAULT="DEFAULT";
    private static final Map<String, AbstractMarketGatewayFactory<BondTrade> > MARKET_GATEWAY_FACTORY_MAP = Maps.newHashMap();
    static{
        MARKET_GATEWAY_FACTORY_MAP.put("FAKE", new FakeBondMarketGatewayFactory());
        MARKET_GATEWAY_FACTORY_MAP.put(DEFAULT, new FakeBondMarketGatewayFactory());
    }

    @Getter
    PCollection<BondTrade> pAllTrades=null;

    @Getter
    AnalysisProbes analysisProbes=null;

    @Getter
    MarketGatewayInterface<BondTrade> marketGatewayInterface=null;

    @Getter
    AnalysisOptions analysisOptions = null;

    @Nonnull
    @Override
    public Pipeline build(String[] args) {
        PipelineOptionsFactory.register(AnalysisOptions.class);
        analysisOptions = PipelineOptionsFactory.fromArgs(args)
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

        PCollection<BondTrade> pWindow = pAllTrades.apply(
                Window.<BondTrade>into(FixedWindows.of(Duration.millis ( analysisOptions.getWindowDuration())))
                        // We will get early (speculative) results as well as cumulative
                        // processing of late data.
                        .triggering(
                                AfterWatermark.pastEndOfWindow()
                                        .withEarlyFirings(
                                                AfterProcessingTime.pastFirstElementInPane()
                                                        .plusDelayOf(FIVE_MINUTES))
                                        .withLateFirings(
                                                AfterProcessingTime.pastFirstElementInPane()
                                                        .plusDelayOf(TEN_MINUTES)))
                        .withAllowedLateness(Duration.standardMinutes(analysisOptions.getAllowedLateness()))
                        .accumulatingFiredPanes()
        );
        //PCollection<BondTrade> pAllTradesTimeStamp = pWindow.apply("AddEventTimestamps", WithTimestamps.of((BondTrade i) -> i.getTimestamp()));

        this.analysisProbes = BondTradeAnalysisPipelineBuilderInterface.prepareAnalysisTransform( pWindow);

        return pipeline;
    }
    @VisibleForTesting
    static class CalculateBondAvgPrice
            extends PTransform<PCollection<BondTrade>, PCollection<KV<String, Double>>> {
        //private final Duration tradeWindowDuration;
        //private final Duration allowedLateness;
       /*
        CalculateBondAvgPrice(Duration teamWindowDuration, Duration allowedLateness) {
            this.tradeWindowDuration = teamWindowDuration;
            this.allowedLateness = allowedLateness;
        }*/
        @Override
        public PCollection<KV<String, Double>> expand(PCollection<BondTrade> input) {
            return input.apply(
                    MapElements.into(
                            TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.doubles()))
                            .via((BondTrade bondTrade) -> KV.of(bondTrade.getAsset().getSecurityId(), bondTrade.getAsset().getPrice())))
                    .apply(Mean.perKey());
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
