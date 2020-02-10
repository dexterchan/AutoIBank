package io.exp.analysis.match.beam.utils;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

public interface HKMAAnalysisOptions extends PipelineOptions {
    @Description("Historical Trade Avro file")
    @Default.String("/Users/dexter/sandbox/AutoIbank/analysis/sample/bondtrade.avro")
    public String getHTrade();
    void setHTrade(String value);

    @Description("Mongo Connection String")
    @Default.String("mongodb://mongoadmin:secret@localhost:27017")
    public String getMongoConnString();
    void setMongoConnString(String value);

    @Description("Mongo Database")
    @Default.String("hkma")
    public String getDataBase();
    void setDataBase(String value);

    @Description("Mongo Bond Security Collection")
    @Default.String("OutstandingGovBond")
    public String getSecurityCollection();
    void setSecurityCollection(String value);

    @Description("Original Maturity for matching")
    @Default.String("10Y")
    public String getOriginalMaturity();
    void setOriginalMaturity(String value);
}
