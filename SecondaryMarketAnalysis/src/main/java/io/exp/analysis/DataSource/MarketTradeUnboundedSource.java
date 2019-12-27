package io.exp.analysis.DataSource;

import io.exp.gateway.MarketGatewayInterface;

import io.exp.security.model.BondTrade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.UnboundedSource;
import org.apache.beam.sdk.options.PipelineOptions;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

//https://github.com/apache/beam/blob/master/sdks/java/io/kinesis/src/main/java/org/apache/beam/sdk/io/kinesis/KinesisReaderCheckpoint.java
//https://github.com/apache/beam/blob/master/sdks/java/io/kinesis/src/main/java/org/apache/beam/sdk/io/kinesis/KinesisSource.java

@Slf4j
@RequiredArgsConstructor
public class MarketTradeUnboundedSource<T> extends UnboundedSource<T, UnboundedSource.CheckpointMark> implements Serializable {

    private final MarketGatewayInterface marketGatewayInterface;
    private final Class classCoder;

    @Override
    public List<? extends UnboundedSource<T, CheckpointMark>> split(int desiredNumSplits, PipelineOptions options) throws Exception {
        // TODO Auto-generated method stub
        return Arrays.asList(this);
    }

    @Override
    public UnboundedReader<T> createReader(PipelineOptions options, @Nullable CheckpointMark checkpointMark) throws IOException {
        return new MarketTradeUnboundedReader(this, marketGatewayInterface);
    }

    @Override
    public Coder<CheckpointMark> getCheckpointMarkCoder() {
        return AvroCoder.of(.class);
        //return AvroCoder.of(CheckpointMark.NoopCheckpointMark.class);
    }

    @Override
    public Coder<T> getOutputCoder() {
        return AvroCoder.of(classCoder);
    }
}
