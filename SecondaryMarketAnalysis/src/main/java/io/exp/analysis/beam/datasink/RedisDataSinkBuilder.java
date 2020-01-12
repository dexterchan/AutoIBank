package io.exp.analysis.beam.datasink;

import io.exp.security.model.BidAsk;
import org.apache.beam.sdk.io.redis.RedisIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.util.function.Function;

public class RedisDataSinkBuilder implements DataSinkBuilder {
    protected final String hostname;
    protected final int port;
    protected final String prefix;
    public RedisDataSinkBuilder(String hostname, int port, BidAsk bidAsk){
        this.hostname = hostname;
        this.port = port;
        prefix = bidAsk.toString();
    }

    @Override
    public void build(PCollection<KV<String, Double>> avgResult) {
        avgResult.apply(
                ParDo.of(Convert2StrinPair.apply(this.prefix))
        ).apply(RedisIO.write().withMethod(RedisIO.Write.Method.SET).withEndpoint(hostname, port));
    }

    static Function < String, DoFn<KV<String, Double>, KV<String, String> >> Convert2StrinPair = (prefix)->
            new DoFn<KV<String, Double>, KV<String, String>>() {
        @ProcessElement
        public void processElement(@Element KV<String, Double> e,OutputReceiver< KV<String, String> > out){
               out.output(KV.of(String.format("%s%s",prefix, e.getKey()), String.valueOf(e.getValue())));
        }
    };
}
