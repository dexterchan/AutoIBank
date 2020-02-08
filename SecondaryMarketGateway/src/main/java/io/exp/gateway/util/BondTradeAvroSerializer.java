package io.exp.gateway.util;

import io.exp.security.model.avro.BondTrade;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BondTradeAvroSerializer implements Serializer<BondTrade> {

    private final Class avroType;
    private final AvroCoder<BondTrade> coder;



    public BondTradeAvroSerializer() {
        this.avroType = BondTrade.class;
        this.coder = AvroCoder.of(BondTrade.class);
    }
    @Override
    public byte[] serialize(String topic, BondTrade data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] outArray=null;
        try {
            out.reset();
            coder.encode(data, out);
            outArray = out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Avro encoding failed.", e);
        }
        return outArray;
    }
}
