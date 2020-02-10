package io.exp.analysis.match.beam.pipeline.check;

import io.exp.security.model.avro.BondStatic;
import io.exp.security.model.avro.BondTrade;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckBondJoin implements SerializableFunction<Iterable< KV<String, KV<BondTrade, BondStatic>> >, Void> {
    @Override
    public Void apply(Iterable<KV<String, KV<BondTrade, BondStatic>>> input) {
        int numOfTrade=0;
        for (KV<String, KV<BondTrade, BondStatic>> joinResult: input){
            String key = joinResult.getKey();
            KV<BondTrade, BondStatic> pair = joinResult.getValue();
            BondTrade bondTrade = pair.getKey();
            BondStatic bondStatic = pair.getValue();
            assertEquals(bondStatic.getIsinCode(), bondTrade.getAsset().getSecurityId());
            numOfTrade++;
        }
        assertThat(numOfTrade).isGreaterThan(0);

        return null;
    }
}
