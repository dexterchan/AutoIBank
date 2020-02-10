package io.exp.analysis.match.beam.pipeline.check;

import io.exp.security.model.avro.BondTrade;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;

@Slf4j
public class CheckBondTrade implements SerializableFunction<Iterable<BondTrade>, Void> {

    @Override
    public Void apply(Iterable<BondTrade> input) {
        input.forEach(
                bondTrade->{
                    log.debug(bondTrade.toString());
                }
        );
        return null;
    }
}
