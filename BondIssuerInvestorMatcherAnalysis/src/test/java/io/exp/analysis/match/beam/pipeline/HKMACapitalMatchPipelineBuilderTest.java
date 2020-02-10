package io.exp.analysis.match.beam.pipeline;

import io.exp.analysis.match.beam.pipeline.check.CheckBondStatic;
import io.exp.analysis.match.beam.pipeline.check.CheckBondTrade;
import io.exp.analysis.match.beam.pipeline.check.CheckSecurityDocument;
import io.exp.analysis.match.beam.transform.HKMABondStaticPreProcess;
import io.exp.analysis.match.beam.utils.HKMAAnalysisOptions;
import io.exp.security.model.avro.BondStatic;
import io.exp.security.model.avro.BondTrade;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.values.PCollection;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HKMACapitalMatchPipelineBuilderTest {
    String[] arguments=null;
    @BeforeEach
    void init(){
        arguments = new String[]{"--securityCollection=OutstandingGovBond"};
    }

    @Test
    void inputBothBondTradeandBondStatic() {
        HKMACapitalMatchPipelineBuilder hkmaCapitalMatchPipelineBuilder = HKMACapitalMatchPipelineBuilder.builder().build();
        Pipeline pipeline = hkmaCapitalMatchPipelineBuilder.build(arguments);
        HKMAAnalysisOptions hkmaAnalysisOptions = hkmaCapitalMatchPipelineBuilder.getHkmaAnalysisOptions();
        PCollection<BondTrade> bondTrades = hkmaCapitalMatchPipelineBuilder.getBondTradePCollection();
        PCollection<Document> documents = hkmaCapitalMatchPipelineBuilder.getSecurityCollection();

        PCollection<BondStatic> bondStatic = documents.apply(new HKMABondStaticPreProcess(hkmaAnalysisOptions.getOriginalMaturity()));


        PAssert.that(bondTrades).satisfies(new CheckBondTrade());
        PAssert.that(documents).satisfies(new CheckSecurityDocument());
        PAssert.that(bondStatic).satisfies(new CheckBondStatic(hkmaAnalysisOptions.getOriginalMaturity()));
        pipeline.run().waitUntilFinish();
    }

}