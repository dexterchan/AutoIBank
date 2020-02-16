package io.exp.analysis.match.beam.pipeline;

import com.google.common.collect.Maps;
import io.exp.analysis.match.beam.model.BondTradeActivityData;
import io.exp.analysis.match.beam.model.Gaussian;
import io.exp.analysis.match.beam.pipeline.check.*;
import io.exp.analysis.match.beam.transform.HKMABondStaticPreProcess;
import io.exp.analysis.match.beam.transform.HKMABondStaticTradeJoin;
import io.exp.analysis.match.beam.transform.tradeactivity.GaussianTrainPerInvestor;
import io.exp.analysis.match.beam.transform.tradeactivity.PreProcessGaussianTrain;
import io.exp.analysis.match.beam.utils.HKMAAnalysisOptions;
import io.exp.security.model.avro.BondStatic;
import io.exp.security.model.avro.BondTrade;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class HKMACapitalMatchPipelineBuilderTest {
    static String[] arguments=null;
    static HKMACapitalMatchPipelineBuilder hkmaCapitalMatchPipelineBuilder;
    static Pipeline pipeline = null;

    static Map<String, Double> custTotal = Maps.newHashMap();
    static Map<String, Double> cust10Y = Maps.newHashMap();
    static Map<String, Double> cust10YMean = Maps.newHashMap();
    static Map<String, Double> cust10YVar = Maps.newHashMap();
    static Map<String, Long> cust10YCnt = Maps.newHashMap();
    @BeforeAll
    static void startInit() throws  Exception{

        hkmaCapitalMatchPipelineBuilder = HKMACapitalMatchPipelineBuilder.builder().build();
        arguments = new String[]{"--securityCollection=OutstandingGovBond"};
        pipeline = hkmaCapitalMatchPipelineBuilder.build(arguments);
        prepareReferenceAnswer();
    }


    @BeforeEach
    void init() throws Exception{
    }

    static void prepareReferenceAnswer() throws Exception{
        //hard code from HKMA result
        //
        String [] Static10Y = new String[] {
                "HK0000474293",
                "HK0000345436",
                "HK0000280708",
                "HK0000209236",
                "HK0000135431",
                "HK0000085537",
                "HK0000059292"
                };
        Set<String> static10YSet = Arrays.asList(Static10Y).stream().collect(Collectors.toSet());
        // Deserialize Users from disk
        DatumReader<BondTrade> userDatumReader = new SpecificDatumReader<BondTrade>(BondTrade.class);
        File file = new File(hkmaCapitalMatchPipelineBuilder.getHkmaAnalysisOptions().getHTrade());
        DataFileReader<BondTrade> dataFileReader = new DataFileReader<BondTrade>(file, userDatumReader);
        BondTrade bondTrade=null;
        while (dataFileReader.hasNext()) {
            bondTrade = dataFileReader.next(bondTrade);
            String isin = bondTrade.getAsset().getSecurityId();
            double amt = bondTrade.getAsset().getNotional();
            String cust = bondTrade.getCust();
            custTotal.put(cust, amt + Optional.ofNullable(custTotal.get(cust)).orElse(0.0));

            if (static10YSet.contains(isin)){
                cust10Y.put(cust, amt+Optional.ofNullable(cust10Y.get(cust)).orElse(0.0));
                cust10YCnt.put(cust, Optional.ofNullable(cust10YCnt.get(cust)).map(cnt->cnt + 1).orElse((long)1));
                cust10YMean.put(cust, Optional.ofNullable(cust10YMean.get(cust)).map(mean->mean+amt).orElse(amt));
                cust10YVar.put(cust, Optional.ofNullable(cust10YVar.get(cust)).map(var->var + amt*amt).orElse(amt*amt));
            }
        }
        //calculate mean
        for (Map.Entry<String, Double> entry : cust10YMean.entrySet()){
            long population = cust10YCnt.get(entry.getKey());
            cust10YMean.put(entry.getKey(), entry.getValue()/population);
        }

        for (Map.Entry<String, Double> entry: cust10YVar.entrySet()){
            long population = cust10YCnt.get(entry.getKey());
            double value = entry.getValue()/population;
            double mean = cust10YMean.get(entry.getKey());
            value -= mean*mean;
            cust10YVar.put(entry.getKey(), value);
        }
    }



    @Test
    void inputBothBondTradeandBondStatic() {
        HKMAAnalysisOptions hkmaAnalysisOptions = hkmaCapitalMatchPipelineBuilder.getHkmaAnalysisOptions();
        PCollection<BondTrade> bondTrades = hkmaCapitalMatchPipelineBuilder.getBondTradePCollection();
        PCollection<Document> documents = hkmaCapitalMatchPipelineBuilder.getSecurityCollection();

        PCollection<BondStatic> bondStatic = documents.apply(new HKMABondStaticPreProcess(hkmaAnalysisOptions.getOriginalMaturity()));

        PCollection<KV<String, KV<BondTrade, BondStatic>>> bondTradeBondStaticJoin=HKMABondStaticTradeJoin.join(bondTrades, bondStatic);

        PCollection<KV<String, BondTradeActivityData>> gaussianTrainData = bondTradeBondStaticJoin.apply(new PreProcessGaussianTrain());

        PCollection<KV<String, Gaussian>> meanVarPerCust = gaussianTrainData.apply(new GaussianTrainPerInvestor());

        CheckGaussian checkGaussian = CheckGaussian.builder()
                    .refMean(cust10YMean)
                    .refVar(cust10YVar).build();


        PAssert.that(bondTrades).satisfies(new CheckBondTrade());
        PAssert.that(documents).satisfies(new CheckSecurityDocument());
        PAssert.that(bondStatic).satisfies(new CheckBondStatic(hkmaAnalysisOptions.getOriginalMaturity()));
        PAssert.that(bondTradeBondStaticJoin).satisfies(new CheckBondJoin());
        PAssert.that(meanVarPerCust).satisfies(checkGaussian);

        pipeline.run().waitUntilFinish();
    }

}