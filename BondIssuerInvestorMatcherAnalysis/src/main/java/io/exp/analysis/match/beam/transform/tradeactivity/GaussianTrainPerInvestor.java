package io.exp.analysis.match.beam.transform.tradeactivity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.exp.analysis.match.beam.model.BondTradeActivityData;
import io.exp.analysis.match.beam.model.Gaussian;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

//Reference Gaussian distribution
//https://en.wikipedia.org/wiki/Normal_distribution
public class GaussianTrainPerInvestor extends PTransform<PCollection<KV<String, BondTradeActivityData>>, PCollection< KV<String, Gaussian>  > > {
    @Override
    public PCollection<KV<String, Gaussian >> expand(PCollection<KV<String, BondTradeActivityData>> input) {

        //Find the mean and variance
        PCollection<KV<String, Double> > custTradeNtl = input.apply(
                "Notional Extract",ParDo.of(
                        new DoFn<KV<String, BondTradeActivityData>, KV<String, Double>>() {
                            @ProcessElement
                            public void processElement(@Element KV<String, BondTradeActivityData>rawCustData, OutputReceiver< KV<String, Double> > out ) {
                                BondTradeActivityData bondTradeActivityData = rawCustData.getValue();
                                out.output(KV.of(rawCustData.getKey(), bondTradeActivityData.getNotional()));
                            }
                        }
                )
        );

        //Get the mean
        PCollection<  KV<String, Double>  > custTradeNtlMean = custTradeNtl.apply(
                "Mean of Notional calculation per customer",Combine.globally(new SumDoubleFn())
        ).apply(
                "Linearize mean per customer result", ParDo.of(iteratableKV2KV)
        );

        //Get the sum of Ntl^2
        PCollection<KV<String, Double>> custTradeNtlSquare = custTradeNtl.apply(
            "Notional square calculation per customer",ParDo.of(
                    new DoFn<KV<String, Double>, KV<String, Double>>() {
                        @ProcessElement
                        public void processElement(@Element KV<String, Double> element, OutputReceiver<KV<String, Double>> out){
                            double ntl = element.getValue();
                            out.output(KV.of(element.getKey(), ntl * ntl));
                        }
                    }
            )
        ).apply(
                "Mean of notional square per customer",Combine.globally(new SumDoubleFn())
        ).apply(
                "Linearize notional square per customer result",ParDo.of(iteratableKV2KV)
        );

        final TupleTag<Double> meanTag = new TupleTag<>();
        final TupleTag<Double> sqTag = new TupleTag<>();

        PCollection<KV<String, CoGbkResult>> covarianceCalculation =
                KeyedPCollectionTuple.of(meanTag, custTradeNtlMean)
                        .and(sqTag, custTradeNtlSquare)
                        .apply(CoGroupByKey.create());

        PCollection<KV<String, Gaussian >> meanVarResult = covarianceCalculation.apply(
            ParDo.of(
                    new DoFn<KV<String, CoGbkResult>, KV<String, Gaussian>>() {
                        @ProcessElement
                        public void processElement(@Element KV<String, CoGbkResult> element, OutputReceiver<KV<String, Gaussian >> out ){
                            String name = element.getKey();
                            Iterable<Double> meanIter = element.getValue().getAll(meanTag);
                            Iterable<Double> sqIter = element.getValue().getAll(sqTag);

                            Double mean = meanIter.iterator().next();
                            Double sq = sqIter.iterator().next();
                            double var = sq - mean*mean;
                            Gaussian gaussian = Gaussian.builder().mean(mean).var(var).build();
                            out.output(KV.of(name, gaussian));
                        }
                    }
            )
        );
        return meanVarResult;
    }
    static final DoFn<Iterable<KV<String, Double>>, KV<String, Double>> iteratableKV2KV =
            new DoFn<Iterable<KV<String, Double>>, KV<String, Double>>() {
                @ProcessElement
                public void processElement(@Element Iterable<KV<String, Double>> lst, OutputReceiver<KV<String, Double>> out){
                    lst.forEach(
                            e->{
                                out.output(e);
                            }
                    );
                }
            };

    private static class Accumulator implements Serializable {
        Map<String, Double> sumMap = Maps.newHashMap();
        Map<String, Long> cntMap = Maps.newHashMap();

        synchronized void addInput(String key, Double value){
            synchronized (sumMap) {
                synchronized (cntMap) {
                    Double accumValue = Optional.ofNullable(sumMap.get(key)).orElse(0.0);
                    accumValue += value;
                    sumMap.put(key, accumValue);
                    long cnt = Optional.ofNullable(cntMap.get(key)).orElse((long) 0);
                    cnt++;
                    cntMap.put(key, cnt);
                }
            }
        }
        synchronized Map<String, Double> getSumMap(){
            return sumMap;
        }
        synchronized Map<String, Long> getCntMap(){
            return cntMap;
        }

        synchronized static Accumulator merge(Accumulator a, Accumulator b){
            Set<String> keySetA = a.sumMap.keySet();
            Set<String> keySetB = b.sumMap.keySet();
            Accumulator newAccum = new Accumulator();

            for (String Akey : keySetA){
                if (!keySetB.contains(Akey)) {
                    newAccum.sumMap.put(Akey, a.sumMap.get(Akey));
                    newAccum.cntMap.put(Akey, Optional.of(a.cntMap.get(Akey)).get());
                }else{
                    double aSum = a.sumMap.get(Akey);
                    long aCnt = Optional.of(a.cntMap.get(Akey)).get();
                    double bSum = b.sumMap.get(Akey);
                    long bCnt = Optional.of(b.cntMap.get(Akey)).get();
                    newAccum.sumMap.put(Akey, aSum + bSum);
                    newAccum.cntMap.put(Akey, aCnt + bCnt);
                }
            }
            for (String BnotA : keySetB){
                if (!keySetA.contains(BnotA)){
                    newAccum.sumMap.put(BnotA, b.sumMap.get(BnotA));
                    newAccum.cntMap.put(BnotA, Optional.of(b.cntMap.get(BnotA)).get());
                }
            }
            return newAccum;
        }


    }
    private static class SumDoubleFn extends Combine.CombineFn< KV<String, Double>, Accumulator, java.lang.Iterable< KV<String, Double> > > {
        @Override
        public Accumulator createAccumulator() {
            return new Accumulator();
        }

        @Override
        public Accumulator addInput(Accumulator accum, KV<String, Double> input) {
            accum.addInput(input.getKey(), input.getValue());
            return accum;
        }

        @Override
        public Accumulator mergeAccumulators(Iterable<Accumulator> accumulators) {
            Accumulator merged = createAccumulator();
            for (Accumulator accum : accumulators){
                merged = Accumulator.merge(merged, accum);
            }
            return merged;
        }

        @Override
        public Iterable<KV<String, Double>> extractOutput(Accumulator accumulator) {
            Map<String, Double> sumMap = accumulator.getSumMap();
            Map<String, Long> cntMap = accumulator.getCntMap();

            List<KV<String, Double>> outputList = Lists.newLinkedList();
            for (Map.Entry<String, Double> entry: sumMap.entrySet()){
                double cnt = Optional.of(cntMap.get(entry.getKey())).get();
                double mean = entry.getValue();
                outputList.add(KV.of(entry.getKey(), mean/cnt));
            }

            return outputList;
        }
    }
}
