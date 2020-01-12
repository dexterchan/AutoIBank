package io.exp.analysis.beam.utils;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.StreamingOptions;

public interface AnalysisOptions extends PipelineOptions, StreamingOptions {
    @Description("Numeric value of window duration, in milli second")
    @Default.Integer(5000)
    int getWindowDuration();
    void setWindowDuration(int value);

    @Description("Numeric value of Sliding Window interval, in milli second")
    @Default.Integer(50)
    int getSlideWindowInterval();
    void setSlideWindowInterval(int value);

    String getVenue();
    void setVenue(String venue);

    String getIdentifier();
    void setIdentifier(String value);

    @Description("Numeric value of allowed data lateness, in minutes")
    @Default.Integer(120)
    Integer getAllowedLateness();
    void setAllowedLateness(Integer value);

    @Description("Kafka Input Topic")
    @Default.String("bondtrade")
    public String getKafkaInputTopic();
    void setKafkaInputTopic(String value);

    @Description("Kafka Server")
    @Default.String("localhost:9092")
    public String getKafkaServer();
    void setKafkaServer(String value);

    @Description("Bid Price Output Topic")
    @Default.String("projects/peer2peer/topics/autoibank_bidprice")
    public String getBidPriceOutputTopic();
    void setBidPriceOutputTopic(String value);

    @Description("Ask Price Output Topic")
    @Default.String("projects/peer2peer/topics/autoibank_askprice")
    public String getAskPriceOutputTopic();
    void setAskPriceOutputTopic(String value);


}
