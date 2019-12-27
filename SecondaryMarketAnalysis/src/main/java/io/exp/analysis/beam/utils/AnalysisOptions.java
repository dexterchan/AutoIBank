package io.exp.analysis.beam.utils;

import org.apache.beam.sdk.options.PipelineOptions;

public interface AnalysisOptions extends PipelineOptions {
    int getWindowWidthMS();
    void setWindowWidthMS(int value);

    String getVenue();
    void setVenue(String venue);

    String getIdentifier();
    void setIdentifier(String value);

}
