package io.exp.analysis.beam.datasink;


import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

public interface DataSinkBuilder {

    public void build( PCollection<KV<String, Double>> avgResult);
}
