package io.exp.analysis.match.beam.pipeline.check;

import io.exp.security.model.avro.BondStatic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.transforms.SerializableFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@RequiredArgsConstructor
public class CheckBondStatic implements SerializableFunction<Iterable<BondStatic>, Void> {
    private final String originalMaturity;

    @Override
    public Void apply(Iterable<BondStatic> input) {
        input.forEach(
                bondStatic->{
                    assertEquals(originalMaturity, bondStatic.getOriginalMaturity());
                }
        );
        return null;
    }
}
