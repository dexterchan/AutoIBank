package io.exp.analysis.match.beam.transform.tradeactivity;

import io.exp.analysis.match.beam.model.BondTradeActivityData;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

public class GaussianTrainPerInvestor extends PTransform<PCollection<KV<String, BondTradeActivityData>>, PCollection< KV<String, BondTradeActivityData>  > > {
    @Override
    public PCollection<KV<String, BondTradeActivityData>> expand(PCollection<KV<String, BondTradeActivityData>> input) {
        return null;
    }
}
