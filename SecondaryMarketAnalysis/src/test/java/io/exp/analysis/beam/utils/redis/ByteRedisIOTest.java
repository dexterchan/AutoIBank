package io.exp.analysis.beam.utils.redis;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.exp.security.model.BondSecurity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.redis.RedisConnectionConfiguration;
import org.apache.beam.sdk.io.redis.RedisIO;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.jupiter.api.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.*;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

//Reference: https://www.alibabacloud.com/help/doc-detail/26369.htm
@Tag("integration")
@Slf4j
class ByteRedisIOTest {
    private RedisConnectionConfiguration redisConnectionConfiguration = null;
    private Jedis jedis = null;

    private static List<BondSecurity> bondSecurityList = null;

    static String hostname = "localhost";
    static int port = 6379;

    @BeforeAll
    public static void initStatic() {
        bondSecurityList = Lists.newLinkedList();
        BondSecurity bondSecurity = new BondSecurity();
        bondSecurity.setSecurityId("XS1587867539");
        bondSecurity.setCouponRate(12.3);
        bondSecurity.setCurrency("USD");
        bondSecurity.setAmount(1000000000);
        bondSecurity.setFrequency("semiannual");
        bondSecurity.setIssuerId("Evergrande");
        //bondSecurity.setPlacementDate(new GregorianCalendar(2020, 5, 20).getTime());
        bondSecurity.setPlacementDate(Instant.parse("2020-05-20T00:00:00Z"));
        //bondSecurity.setMaturity(new GregorianCalendar(2024, 5, 20).getTime());
        bondSecurity.setMaturity(Instant.parse("2024-05-20T00:00:00Z"));
        bondSecurity.setSeniority("Secured");
        bondSecurityList.add(bondSecurity);

        bondSecurity = new BondSecurity();
        bondSecurity.setSecurityId("XS1627599142");
        bondSecurity.setCouponRate(12.3);
        bondSecurity.setCurrency("USD");
        bondSecurity.setAmount(1000000000);
        bondSecurity.setFrequency("semiannual");
        bondSecurity.setIssuerId("Evergrande");
        bondSecurity.setPlacementDate(Instant.parse("2017-06-28T00:00:00Z"));
        bondSecurity.setMaturity(Instant.parse("2021-06-28T00:00:00Z"));
        bondSecurity.setSeniority("Secured");
        bondSecurityList.add(bondSecurity);

    }

    @BeforeEach
    public void init() {
        redisConnectionConfiguration = RedisConnectionConfiguration.create();
        redisConnectionConfiguration.withHost("localhost");
        redisConnectionConfiguration.withPort(6379);
        jedis = redisConnectionConfiguration.connect();

    }

    @Test
    public void testRedisPipeline() {
        Pipeline pipeline = jedis.pipelined();
        pipeline.multi(); //redis transaction
        String sampleKey = "TestBinaryText";
        String sampleText = "Hello world!";
        pipeline.set(sampleKey.getBytes(), sampleText.getBytes());
        pipeline.exec(); //execute transaction
        pipeline.sync();
        byte[] out = jedis.get(sampleKey.getBytes());
        assertArrayEquals(sampleText.getBytes(), out);
    }

    @Test
    public void testAvro() {
        for (BondSecurity sec : bondSecurityList) {
            log.debug("Check security" + sec.getSecurityId());
            String securityId = sec.getSecurityId();
            BytesContainer bytesContainer = objToByteStreamFunc.apply(sec);
            BondSecurity rBondSec = bondSecurityDeserializeFunc.apply(bytesContainer.getData());
            assertEquals(sec, rBondSec);
        }
    }

    @Test
    public void testRedisAvro() {
        Map<String, Double> issueHistory = Maps.newHashMap();
        issueHistory.put("senority1", 1000000.0);
        issueHistory.put("senority2", 2000000.0);
        SampleAggResult sampleAggResult = SampleAggResult.builder()
                .identifier("SampleAgg")
                .issueHistory(issueHistory)
                .bytesContainer(BytesContainer.of("ABCD".getBytes())).build();


        Pipeline pipeline = jedis.pipelined();
        pipeline.multi(); //redis transaction
        String sampleKey = "TestBinaryText2";
        String sampleText = "Hello world!";
        pipeline.set(sampleKey.getBytes(), sampleText.getBytes());

        byte[] data = avroSerializeFunc.apply(sampleAggResult).getData();
        pipeline.set(sampleAggResult.getIdentifier().getBytes(), data);
        pipeline.exec(); //execute transaction
        pipeline.sync();

        byte[] sampleTextRetrieve = jedis.get(sampleKey.getBytes());
        assertArrayEquals(sampleText.getBytes(), sampleTextRetrieve);
        byte[] sampleAggRetrieve = jedis.get(sampleAggResult.getIdentifier().getBytes());
        InputStream inputStream = new ByteArrayInputStream(sampleAggRetrieve);
        SampleAggResult retrievedAggResult = avroDeserializeFunc.apply(inputStream);

        assertEquals(sampleAggResult, retrievedAggResult);
    }

    static DoFn<BondSecurity, KV<String, BondSecurity>> Convert2BondSecPair =
            new DoFn<BondSecurity, KV<String, BondSecurity>>() {
                @ProcessElement
                public void processElement(@Element BondSecurity e, OutputReceiver<KV<String, BondSecurity>> out) {
                    out.output(KV.of(e.getSecurityId(), e));
                }
            };

    /*static DoFn<BondSecurity, KV<String, String>> Convert2BondSecStringPair =
            new DoFn<BondSecurity, KV<String, String>>() {
                @ProcessElement
                public void processElement(@Element BondSecurity e, OutputReceiver<KV<String, String>> out) {
                    out.output(KV.of(e.getSecurityId(), e.toString()));
                }
            };*/
    static SerializableFunction<BondSecurity, BytesContainer> objToByteStreamFunc = (bondSec) -> {
        AvroCoder<BondSecurity> avroCoder = AvroCoder.of(BondSecurity.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.reset();
            avroCoder.encode(bondSec, out);
            byte[] bytes = out.toByteArray();

            return BytesContainer.of(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Avro encoding failed.", e);
        }
    };

    @Test
    public void testRedisBeam() {
        org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create();
        PCollection<BondSecurity> input = p.apply(Create.of(bondSecurityList));

        input.apply(ParDo.of(Convert2BondSecPair))
                .apply(redisWrite);

        /*input.apply(ParDo.of(Convert2BondSecStringPair))
            .apply(redisStrWrite);*/
        p.run().waitUntilFinish();
        log.debug("Trigger the check result");

        Jedis jedis = new Jedis(hostname, port);
        try {
            for (BondSecurity sec : bondSecurityList) {
                log.debug("Check security" + sec.getSecurityId());
                String securityId = sec.getSecurityId();
                byte[] rawData = jedis.get(securityId.getBytes());
                BondSecurity bondSecurity = bondSecurityDeserializeFunc.apply(rawData);
                assertEquals(sec, bondSecurity);
            }
        } finally {
            jedis.close();
        }
    }

    static ByteRedisIO.Write<BondSecurity> redisWrite = ByteRedisIO.write()
            .withMethod(ByteRedisIO.Write.Method.SET)
            .withObjToByteStreamFunc(objToByteStreamFunc)
            .withEndpoint(hostname, port);

    static RedisIO.Write redisStrWrite = RedisIO.write()
            .withMethod(RedisIO.Write.Method.SET)
            .withEndpoint(hostname, port);

    Function<byte[], BondSecurity> bondSecurityDeserializeFunc = (rawData) -> {
        AvroCoder<BondSecurity> avroCoder = AvroCoder.of(BondSecurity.class);
        InputStream inputStream = new ByteArrayInputStream(rawData);
        try {
            return avroCoder.decode(inputStream);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    };

    Function<InputStream, SampleAggResult> avroDeserializeFunc = (inputStream) -> {
        AvroCoder<SampleAggResult> avroCoder = AvroCoder.of(SampleAggResult.class);
        try {
            SampleAggResult obj = avroCoder.decode(inputStream);
            return obj;
        } catch (IOException ioe) {
            log.error(ioe.getMessage());
            throw new RuntimeException("Avro decoding failed.", ioe);
        }
    };
    Function<SampleAggResult, BytesContainer> avroSerializeFunc = (data) -> {
        AvroCoder<SampleAggResult> avroCoder = AvroCoder.of(SampleAggResult.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.reset();
            avroCoder.encode(data, out);
            return BytesContainer.of(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Avro encoding failed.", e);
        }
    };


    @After
    public void close() {
        jedis.close();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class SampleAggResult implements Serializable {
        String identifier;
        Map<String, Double> issueHistory;
        BytesContainer bytesContainer;

        SampleAggResult(String identifier, Map<String, Double> issueHistory, BytesContainer bytesContainer) {
            this.identifier = identifier;
            this.issueHistory = issueHistory;
            this.bytesContainer = bytesContainer;
        }

        public static SampleAggResultBuilder builder() {
            return new SampleAggResultBuilder();
        }


        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof SampleAggResult)) return false;
            final SampleAggResult other = (SampleAggResult) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$identifier = this.getIdentifier();
            final Object other$identifier = other.getIdentifier();
            if (this$identifier == null ? other$identifier != null : !this$identifier.equals(other$identifier))
                return false;
            final Map<String, Double> this$issueHistory = this.getIssueHistory();
            final Map<String, Double> other$issueHistory = other.getIssueHistory();
            if (this$issueHistory.size() != other$issueHistory.size()) {
                return false;
            }
            for (Map.Entry<String, Double> entry : this$issueHistory.entrySet()) {
                boolean b = Optional.ofNullable(other$issueHistory.get(entry.getKey()))
                        .map(
                                value -> Math.abs(value - entry.getValue()) < 0.0001
                        ).orElse(false);
                ;
                if (!b)
                    return b;
            }
            if (this.bytesContainer.getData().length != other.bytesContainer.getData().length) {
                return false;
            }
            for (int i = 0; i < this.bytesContainer.getData().length; i++) {
                if (this.bytesContainer.getData()[i] != other.bytesContainer.getData()[i]) {
                    return false;
                }
            }
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof SampleAggResult;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $identifier = this.getIdentifier();
            result = result * PRIME + ($identifier == null ? 43 : $identifier.hashCode());
            final Object $issueHistory = this.getIssueHistory();
            result = result * PRIME + ($issueHistory == null ? 43 : $issueHistory.hashCode());
            return result;
        }

        public static class SampleAggResultBuilder {
            private String identifier;
            private Map<String, Double> issueHistory;
            private BytesContainer bytesContainer;

            SampleAggResultBuilder() {
            }

            public SampleAggResultBuilder identifier(String identifier) {
                this.identifier = identifier;
                return this;
            }

            public SampleAggResultBuilder issueHistory(Map<String, Double> issueHistory) {
                this.issueHistory = issueHistory;
                return this;
            }

            public SampleAggResultBuilder bytesContainer(BytesContainer bytesContainer) {
                this.bytesContainer = bytesContainer;
                return this;
            }

            public SampleAggResult build() {
                return new SampleAggResult(identifier, issueHistory, bytesContainer);
            }

            public String toString() {
                return "ByteRedisIOTest.SampleAggResult.SampleAggResultBuilder(identifier=" + this.identifier + ", issueHistory=" + this.issueHistory + ", bytesContainer=" + this.bytesContainer + ")";
            }
        }
    }

}