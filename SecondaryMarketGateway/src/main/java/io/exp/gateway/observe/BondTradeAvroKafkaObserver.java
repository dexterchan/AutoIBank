package io.exp.gateway.observe;

import io.exp.gateway.util.BondTradeAvroSerializer;
import io.exp.security.model.BondTrade;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

@Slf4j
public class BondTradeAvroKafkaObserver extends AsyncObserver<BondTrade> {
    private final Producer<String, BondTrade> producer;
    private final String topic;
    public BondTradeAvroKafkaObserver(String bootstrapserver, String topic){
        Properties props = new Properties();
        this.topic = topic;
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapserver);
        props.put("acks", "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BondTradeAvroSerializer.class
                );
        producer = new KafkaProducer<>(props, new StringSerializer(), new BondTradeAvroSerializer());

        //props.put("schema.registry.url", "http://localhost:8081");
    }

    @Override
    public void asyncUpdate(BondTrade trade) {
        try{
            producer.send(new ProducerRecord<>(topic, trade.getId(), trade));
        }catch(KafkaException ke){
            log.error(ke.getMessage());
            throw ke;
        }
    }

    @Override
    public void asyncThrowError(Throwable ex) {

    }

    @Override
    public String getDescription() {
        return String.format("Observer writes Kafka topic %s as output", this.topic);
    }

    @Override
    protected void finalize() throws Throwable {
        producer.close();
    }
}
