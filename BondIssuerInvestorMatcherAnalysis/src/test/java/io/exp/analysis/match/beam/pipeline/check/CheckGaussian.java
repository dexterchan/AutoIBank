package io.exp.analysis.match.beam.pipeline.check;

import io.exp.analysis.match.beam.model.Gaussian;
import lombok.Builder;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Builder
public class CheckGaussian implements SerializableFunction<Iterable<  KV<String, Gaussian> >, Void> {

    Map<String, Double> refMean;
    Map<String, Double> refVar;
    @Override
    public Void apply(Iterable<KV<String, Gaussian>> input) {

        //Check Mean
        int numCust = 0;
        for (KV<String, Gaussian> para: input) {
            String cust = para.getKey();
            Double Mean = para.getValue().getMean();
            Double Var = para.getValue().getVar();

            Double refMeanValue = refMean.get(cust);
            Double refVarValue = refVar.get(cust);
            assertEquals(refMeanValue, Mean, 1000);
            assertEquals(refVarValue, Var, 1000);
            numCust++;
        }
        assertThat(numCust).isGreaterThan(0);
        return null;
    }
}
