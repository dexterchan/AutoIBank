package io.exp.analysis.beam.pipeline;

import com.google.common.collect.ImmutableMap;
import io.exp.analysis.beam.utils.AnalysisOptions;
import io.exp.analysis.beam.utils.BondTradeAvroDeserializer;
import io.exp.security.model.BondTrade;
import lombok.Getter;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;

public class BondTradeKafkaAnalysisPipelineBuilder implements BondTradeAnalysisPipelineBuilderInterface {

    @Getter
    AnalysisProbes analysisProbes=null;

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

        String kafkaServer = analysisOptions.getKafkaServer();
        String topic = analysisOptions.getKafkaInputTopic();
        AvroCoder<BondTrade> bondTradeAvroCoder= AvroCoder.of(BondTrade.class);;
        pipeline.getCoderRegistry().registerCoderForClass(BondTrade.class, bondTradeAvroCoder);
        pipeline.getCoderRegistry().registerCoderForClass(String.class, StringUtf8Coder.of());

        PCollection<BondTrade> pBondTrades = pipeline.apply(KafkaIO.<String, BondTrade>read()
                .withBootstrapServers(kafkaServer)
                .withTopic(topic)
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(BondTradeAvroDeserializer.class)
                .updateConsumerProperties(ImmutableMap.of("auto.offset.reset", (Object)"earliest","enable.auto.commit",(Object)"true","group.id",(Object)"testGroup"))
                .withoutMetadata() // PCollection<KV<Long, String>>
        ).apply(Values.<BondTrade>create())
                .apply(Window.<BondTrade>into(
                        SlidingWindows.of(Duration.millis(analysisOptions.getWindowDuration()))
                                .every(Duration.millis(analysisOptions.getSlideWindowInterval()))
                ));
        this.analysisProbes = BondTradeAnalysisPipelineBuilderInterface.prepareAnalysisTransform( pBondTrades);


        return pipeline;
    }

}
