package io.exp.analysis.beam.utils;


import io.exp.security.model.avro.BondTrade;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
@Slf4j
public class BondTradeAvroDeserializer  implements Deserializer<BondTrade> {
    private final Class avroType;
    private final AvroCoder<BondTrade> coder;

    public BondTradeAvroDeserializer() {
        this.avroType = BondTrade.class;
        this.coder = AvroCoder.of(BondTrade.class);
    }

    @Override
    public BondTrade deserialize(String topic, byte[] data) {
        BondTrade obj=null;
        try{
            obj = coder.decode(new ByteArrayInputStream(data));
        }catch (IOException ioe){
            //throw new RuntimeException("Avro decoding failed.", ioe);
            log.error(ioe.getMessage());
        }
        return obj;
    }
}
