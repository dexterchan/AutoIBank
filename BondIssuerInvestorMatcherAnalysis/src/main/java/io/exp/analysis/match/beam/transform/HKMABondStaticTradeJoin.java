package io.exp.analysis.match.beam.transform;

import io.exp.security.model.avro.BondStatic;
import io.exp.security.model.avro.BondTrade;

import lombok.Builder;
import org.apache.beam.sdk.extensions.joinlibrary.Join;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

//Reference https://beam.apache.org/documentation/sdks/java-extensions/
public class HKMABondStaticTradeJoin  {

    public static PCollection<KV<String, KV<BondTrade, BondStatic>>> join(PCollection<BondTrade> bondTrades, PCollection<BondStatic> bondStatics) {

        PCollection<KV<String, BondTrade> > keyBondtrades = bondTrades.apply(
            "Bond Trade extract ISIN for join",
                ParDo.of(
                    new DoFn<BondTrade, KV<String, BondTrade>>() {
                        @ProcessElement
                        public void processElement(@Element BondTrade bondTrade, OutputReceiver< KV<String, BondTrade> > out) {
                            out.output(KV.of(bondTrade.getAsset().getSecurityId(), bondTrade));
                        }
                    }
            )
        );
        PCollection<KV<String, BondStatic> > keyBondStatics = bondStatics.apply(
                "Bond Static extract ISIN for join",
                ParDo.of(
                        new DoFn<BondStatic, KV<String, BondStatic>>() {
                            @ProcessElement
                            public void processElement(@Element BondStatic bondStatic, OutputReceiver< KV<String, BondStatic> > out) {
                                out.output(KV.of(bondStatic.getIsinCode(), bondStatic));
                            }
                        }
                )
        );

        return Join.innerJoin(keyBondtrades, keyBondStatics);
    }
}
