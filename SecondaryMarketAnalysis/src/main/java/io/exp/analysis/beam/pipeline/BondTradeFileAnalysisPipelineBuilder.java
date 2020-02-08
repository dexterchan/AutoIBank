package io.exp.analysis.beam.pipeline;


import io.exp.analysis.beam.utils.AnalysisOptions;
import io.exp.security.model.avro.BondTrade;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.values.PCollection;

@Slf4j
public class BondTradeFileAnalysisPipelineBuilder implements BondTradeAnalysisPipelineBuilderInterface {

    @Getter
    AnalysisProbes analysisProbes=null;

    @Getter
    AnalysisOptions analysisOptions = null;

    public interface FileAnalysisOption extends AnalysisOptions {

        String getFileName();
        void setFileName(String fileName);

    }
    @Override
    public Pipeline build(String[] args) {
        PipelineOptionsFactory.register(FileAnalysisOption.class);
        FileAnalysisOption fileAnalysisOption = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .withoutStrictParsing()
                .create()
                .as(FileAnalysisOption.class);
        this.analysisOptions = fileAnalysisOption;
        fileAnalysisOption.setStreaming(false);
        Pipeline pipeline = Pipeline.create(fileAnalysisOption);
        PCollection<BondTrade> pBondTrades =
                pipeline.apply(AvroIO.read(BondTrade.class).from(fileAnalysisOption.getFileName()));

        this.analysisProbes = BondTradeAnalysisPipelineBuilderInterface.prepareAnalysisTransform( pBondTrades);

        return pipeline;
    }

}
