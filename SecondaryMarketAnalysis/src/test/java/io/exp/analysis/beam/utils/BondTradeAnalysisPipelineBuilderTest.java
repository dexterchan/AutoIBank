package io.exp.analysis.beam.utils;

import io.exp.gateway.MarketGatewayInterface;
import io.exp.security.model.BondTrade;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BondTradeAnalysisPipelineBuilderTest {
    String [] arguments = null;

    @BeforeEach
    public void init(){
        arguments= new String[]{
                "--windowWidthMS=500",
                "--venue=fake",
                "--identifier=abcd"
        };
    }
    @Test
    void build() {
        BondTradeAnalysisPipelineBuilder pipelineBuilder = new BondTradeAnalysisPipelineBuilder();
        Pipeline pipeline = pipelineBuilder.build(arguments);

        MarketGatewayInterface marketGatewayInterface = pipelineBuilder.getMarketGatewayInterface();

        PCollection<BondTrade> pTrade = pipelineBuilder.getPTrade();
        //PAssert.that(pTrade).satisfies(new CheckTrade());
        pipeline.run().waitUntilFinish();
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