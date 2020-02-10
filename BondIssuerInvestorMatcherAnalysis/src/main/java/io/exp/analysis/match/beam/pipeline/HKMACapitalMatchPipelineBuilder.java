package io.exp.analysis.match.beam.pipeline;

import io.exp.analysis.match.beam.PipelineBuilder;
import io.exp.analysis.match.beam.utils.HKMAAnalysisOptions;
import io.exp.security.model.avro.BondTrade;
import lombok.Builder;
import lombok.Getter;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.io.mongodb.MongoDbIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.values.PCollection;
import org.bson.Document;

import javax.annotation.Nonnull;

@Builder
@Getter
public class HKMACapitalMatchPipelineBuilder implements PipelineBuilder {

    private PCollection<BondTrade> bondTradePCollection;
    private PCollection<Document> securityCollection;
    private HKMAAnalysisOptions hkmaAnalysisOptions;
    private Pipeline pipeline;
    @Nonnull
    @Override
    public Pipeline build(String[] args) {
        PipelineOptionsFactory.register(HKMAAnalysisOptions.class);
        hkmaAnalysisOptions = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .withoutStrictParsing()
                .create()
                .as(HKMAAnalysisOptions.class);

        pipeline = Pipeline.create(hkmaAnalysisOptions);

        this.prepareBondTradeCollection();
        this.prepareSecurityCollection();

        return pipeline;
    }
    private void prepareSecurityCollection(){
        securityCollection=pipeline.apply(MongoDbIO.read()
                .withUri(hkmaAnalysisOptions.getMongoConnString())
                .withDatabase(hkmaAnalysisOptions.getDataBase())
                .withCollection(hkmaAnalysisOptions.getSecurityCollection()));
    }
    private void prepareBondTradeCollection(){
        bondTradePCollection =
                pipeline.apply(AvroIO.read(BondTrade.class).from(hkmaAnalysisOptions.getHTrade()));

    }

}
