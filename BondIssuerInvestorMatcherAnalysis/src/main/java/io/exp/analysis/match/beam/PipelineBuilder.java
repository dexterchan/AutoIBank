package io.exp.analysis.match.beam;

import org.apache.beam.sdk.Pipeline;

import javax.annotation.Nonnull;

public interface PipelineBuilder {
    @Nonnull
    Pipeline build(String[] args);
}
