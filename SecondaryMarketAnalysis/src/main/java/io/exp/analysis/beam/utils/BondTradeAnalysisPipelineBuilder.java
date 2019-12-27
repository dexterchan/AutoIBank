package io.exp.analysis.beam.utils;

import com.google.common.collect.Maps;
import io.exp.analysis.DataSource.MarketTradeUnboundedSource;
import io.exp.analysis.beam.PipelineBuilder;
import io.exp.gateway.AbstractMarketGatewayFactory;
import io.exp.gateway.MarketGatewayInterface;
import io.exp.gateway.fake.FakeBondMarketGatewayFactory;

import io.exp.security.model.BondTrade;
import lombok.Getter;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.io.UnboundedSource;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.values.PCollection;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public class BondTradeAnalysisPipelineBuilder implements PipelineBuilder, Serializable {

    private static String DEFAULT="DEFAULT";
    private static final Map<String, AbstractMarketGatewayFactory<BondTrade> > MARKET_GATEWAY_FACTORY_MAP = Maps.newHashMap();
    static{
        MARKET_GATEWAY_FACTORY_MAP.put("FAKE", new FakeBondMarketGatewayFactory());
        MARKET_GATEWAY_FACTORY_MAP.put(DEFAULT, new FakeBondMarketGatewayFactory());
    }

    @Getter
    PCollection<BondTrade> pTrade=null;

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

        Pipeline pipeline = Pipeline.create(analysisOptions);

        String venue = analysisOptions.getVenue().toUpperCase();
        String identifier = analysisOptions.getIdentifier().toUpperCase();

        String pipeLineName = String.format("%s_%s",venue, identifier);

        pTrade = pipeline.apply(pipeLineName, Read.from(getMarketDataSource(venue, identifier)));

        return pipeline;
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
