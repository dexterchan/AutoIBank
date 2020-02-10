package io.exp.analysis.match.beam.transform.tradeactivity;

import io.exp.analysis.match.beam.model.BondTradeActivityData;
import io.exp.security.model.avro.BondStatic;
import io.exp.security.model.avro.BondTrade;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;


public class PreProcessGaussianTrain extends PTransform < PCollection< KV<String, KV<BondTrade, BondStatic>>> , PCollection<KV<String, BondTradeActivityData> > > {
    @Override
    public PCollection<KV<String, BondTradeActivityData>> expand(PCollection<KV<String, KV<BondTrade, BondStatic>>> input) {

        PCollection<KV <String, BondTradeActivityData> > extractTradeActivities = input.apply(
                "Extract trade activity for Gaussian training",
                ParDo.of(
                        new DoFn< KV<String, KV<BondTrade, BondStatic>> , KV<String, BondTradeActivityData> >() {
                            @ProcessElement
                            public void processElement(@Element KV<String, KV<BondTrade, BondStatic>> tradestaticJoin, OutputReceiver< KV<String, BondTradeActivityData> > out) {
                                BondTradeActivityData.BondTradeActivityDataBuilder bondTradeActivityDataBuilder = BondTradeActivityData.builder();
                                BondTrade bondTrade = tradestaticJoin.getValue().getKey();
                                BondStatic bondStatic = tradestaticJoin.getValue().getValue();

                                bondTradeActivityDataBuilder.cust(bondTrade.getCust());
                                bondTradeActivityDataBuilder.isin_code(bondStatic.getIsinCode());
                                bondTradeActivityDataBuilder.notional(bondTrade.getAsset().getNotional());
                                bondTradeActivityDataBuilder.original_maturity(bondStatic.getOriginalMaturity());

                                BondTradeActivityData bondTradeActivityData = bondTradeActivityDataBuilder.build();
                                out.output(KV.of(bondTradeActivityData.getCust(), bondTradeActivityData));
                            }
                        }
                )
        );
        return extractTradeActivities;
    }
}
