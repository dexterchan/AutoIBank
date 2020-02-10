package io.exp.analysis.match.beam.transform;

import com.google.gson.Gson;
import io.exp.security.model.avro.BondStatic;
import io.exp.security.model.avro.FixFloat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.bson.Document;
import org.joda.time.Duration;
@Slf4j
@RequiredArgsConstructor
public class HKMABondStaticPreProcess extends PTransform<PCollection<Document>,PCollection<BondStatic> > {
    private final String originalMaturity;

    @Override
    public PCollection<BondStatic> expand(PCollection<Document> input) {

        PCollection<BondStatic> bondStatic = input.apply(
                "Transform MongoDoc to Bond Static",ParDo.of(
                        new DoFn<Document, BondStatic>() {
                            @ProcessElement
                            public void processElement(@Element Document c, OutputReceiver< BondStatic > out) {
                                BondStatic.Builder builder = BondStatic.newBuilder();
                                try {
                                    try {
                                        builder.setCoupon(Double.parseDouble(c.getString("coupon")));
                                        builder.setFixfloat(FixFloat.FLOAT);
                                    } catch (NumberFormatException nfe) {
                                        builder.setCoupon(0);
                                        builder.setFixfloat(FixFloat.FLOAT);
                                    }
                                    builder.setExpectedMaturityDate(c.getString("expected_maturity_date"));
                                    builder.setInstitutionalRetail(c.getString("institutional_retail"));
                                    builder.setIsinCode(c.getString("isin_code"));
                                    builder.setIssueNumber(c.getString("issue_number"));
                                    builder.setOriginalMaturity(c.getString("original_maturity"));

                                    Object outstandSize = c.get("outstanding_size");
                                    if (outstandSize instanceof Double)
                                        builder.setOutstandingSize((c.getDouble("outstanding_size")));
                                    else if (outstandSize instanceof Integer)
                                        builder.setOutstandingSize((c.getInteger("outstanding_size")));
                                    else {
                                        builder.setOutstandingSize(Double.parseDouble(c.getString("outstanding_size")));
                                    }
                                    try {
                                        builder.setStockCode(c.getString("stock_code"));
                                    }catch (Exception e){
                                        builder.setStockCode("NA");
                                    }
                                    BondStatic bondStatic = builder.build();
                                    out.output(bondStatic);
                                }catch(Exception ex){
                                    log.error("Conversion error:"+ex.getMessage());
                                    log.error(c.toJson());
                                }
                            }
                        }
                )
        );
        PCollection<BondStatic> filteredBondStatic = bondStatic.apply(
                Filter.by(new SerializableFunction<BondStatic, Boolean>() {
                    @Override
                    public Boolean apply(BondStatic input) {
                        return input.getOriginalMaturity().equals(originalMaturity);
                    }
                })
        );

        return filteredBondStatic;
    }
}
