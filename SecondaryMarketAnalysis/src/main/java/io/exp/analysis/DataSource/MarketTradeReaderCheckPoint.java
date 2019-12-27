package io.exp.analysis.DataSource;

import org.apache.beam.sdk.io.UnboundedSource;

import java.io.IOException;
import java.io.Serializable;

public class MarketTradeReaderCheckPoint implements UnboundedSource.CheckpointMark, Serializable {
    @Override
    public void finalizeCheckpoint() throws IOException {
    }
}
