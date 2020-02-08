package io.exp.gateway.observe;

import io.exp.security.model.avro.BondTrade;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileConstants;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.beam.sdk.coders.AvroCoder;

import java.io.FileOutputStream;
import java.io.IOException;
@Slf4j
public class BondTradeAvroFileObserver extends AsyncObserver<BondTrade>{
    private FileOutputStream fileOutputStream;
    DataFileWriter<BondTrade> writer = null;
    Schema schema = null;

    public BondTradeAvroFileObserver(String fileName) throws Exception {
        fileOutputStream = new FileOutputStream(fileName);
        AvroCoder<BondTrade> coder = AvroCoder.of(BondTrade.class);
        DatumWriter<BondTrade> datumWriter =  new ReflectDatumWriter<>(coder.getSchema());
        schema = ReflectData.get().getSchema(BondTrade.class);
        writer = new DataFileWriter<>(datumWriter);
        writer.setCodec(CodecFactory.fromString(DataFileConstants.BZIP2_CODEC));
        writer.create(coder.getSchema(), fileOutputStream);
    }
    @Override
    public void asyncUpdate(BondTrade trade) {
        try {
            writer.append(trade);
            writer.sync();
        }catch(IOException ioe){
            log.error(ioe.getMessage());
        }
    }

    @Override
    public void asyncThrowError(Throwable ex) {
        log.error(ex.getMessage());
    }

    @Override
    public String getDescription() {
        return "Observer writes Avro File as output";
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        writer.close();
        this.fileOutputStream.close();
    }
}
