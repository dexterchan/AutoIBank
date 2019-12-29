package io.exp.analysis.beam.utils;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.StreamingOptions;

public interface AnalysisOptions extends PipelineOptions, StreamingOptions {
    @Description("Numeric value of fixed window duration, in mini-second")
    @Default.Integer(500)
    int getWindowDuration();
    void setWindowDuration(int value);

    String getVenue();
    void setVenue(String venue);

    String getIdentifier();
    void setIdentifier(String value);

    @Description("Numeric value of allowed data lateness, in minutes")
    @Default.Integer(120)
    Integer getAllowedLateness();
    void setAllowedLateness(Integer value);

}
