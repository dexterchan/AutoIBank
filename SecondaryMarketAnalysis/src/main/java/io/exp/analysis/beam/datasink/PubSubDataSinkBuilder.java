package io.exp.analysis.beam.datasink;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.util.function.Function;


public class PubSubDataSinkBuilder implements DataSinkBuilder{
    private final String pubsubTopicName;

    public PubSubDataSinkBuilder(String pubsubTopicName){
        this.pubsubTopicName = pubsubTopicName;
    }

    @Override
    public void build( PCollection<KV<String, Double>> avgResult) {
        avgResult.apply(ParDo.of(
                convertToString
        )).apply(
                pubsubWrite.apply(this.pubsubTopicName)
        );
    }

    static Function< String, PubsubIO.Write<String>  > pubsubWrite = (pubsubTopicName) -> {
       return PubsubIO.writeStrings()
               .to(pubsubTopicName);
    };


    static DoFn<KV<String, Double>, String> convertToString = new DoFn<KV<String, Double>, String> () {
        @ProcessElement
        public void processElement(@Element KV<String, Double> e,OutputReceiver< String > out){
            String strVal = e.getKey()+":"+e.getValue().toString();
            out.output( strVal );
        }
    };

}
