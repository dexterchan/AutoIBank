package io.exp.analysis.beam.utils.redis;

import static org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Preconditions.checkArgument;
import com.google.auto.value.AutoValue;
import lombok.Builder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.io.redis.RedisConnectionConfiguration;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;

import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import javax.annotation.Nullable;

@Slf4j
@Builder
public class ByteRedisIO<T > {




    /** Write data to a Redis server. */
    public static <T> ByteRedisIO.Write write() {
        return new AutoValue_ByteRedisIO_Write.Builder()
                .setConnectionConfiguration(RedisConnectionConfiguration.create())
                .setMethod(ByteRedisIO.Write.Method.APPEND)
                .build();
    }

    // A {@link PTransform} to write to a Redis server
    @AutoValue
    public abstract static class Write <T> extends PTransform<PCollection<KV<String, T >>, PDone> {

        /** Determines the method used to insert data in Redis. */
        public enum Method {

            /**
             * Use APPEND command. If key already exists and is a string, this command appends the value
             * at the end of the string.
             */
            APPEND,

            /** Use SET command. If key already holds a value, it is overwritten. */
            SET,

            /**
             * Use LPUSH command. Insert value at the head of the list stored at key. If key does not
             * exist, it is created as empty list before performing the push operations. When key holds a
             * value that is not a list, an error is returned.
             */
            LPUSH,

            /**
             * Use RPUSH command. Insert value at the tail of the list stored at key. If key does not
             * exist, it is created as empty list before performing the push operations. When key holds a
             * value that is not a list, an error is returned.
             */
            RPUSH,

            /** Use SADD command. Insert value in a set. Duplicated values are ignored. */
            SADD,

            /** Use PFADD command. Insert value in a HLL structure. Create key if it doesn't exist */
            PFADD,

            /** Use INCBY command. Increment counter value of a key by a given value. */
            INCRBY,

            /** Use DECRBY command. Decrement counter value of a key by given value. */
            DECRBY,
        }

        @Nullable
        abstract RedisConnectionConfiguration connectionConfiguration();

        @Nullable
        abstract Method method();

        @Nullable
        abstract Long expireTime();

        @Nullable
        abstract SerializableFunction<T, BytesContainer> objToByteStreamFunc();

        abstract Builder builder();

        @AutoValue.Builder
        abstract static class Builder<T> {

            abstract Builder setConnectionConfiguration(
                    RedisConnectionConfiguration connectionConfiguration);

            abstract Builder setMethod(Method method);

            abstract Builder setExpireTime(Long expireTimeMillis);

            abstract Builder setObjToByteStreamFunc(SerializableFunction<T, BytesContainer> ObjToByteStreamFunc);


            abstract Write build();
        }

        public Write withEndpoint(String host, int port) {
            checkArgument(host != null, "host can not be null");
            checkArgument(port > 0, "port can not be negative or 0");
            return builder()
                    .setConnectionConfiguration(connectionConfiguration().withHost(host).withPort(port))
                    .build();
        }

        public Write withAuth(String auth) {
            checkArgument(auth != null, "auth can not be null");
            return builder().setConnectionConfiguration(connectionConfiguration().withAuth(auth)).build();
        }

        public Write withTimeout(int timeout) {
            checkArgument(timeout >= 0, "timeout can not be negative");
            return builder()
                    .setConnectionConfiguration(connectionConfiguration().withTimeout(timeout))
                    .build();
        }

        public Write withConnectionConfiguration(RedisConnectionConfiguration connection) {
            checkArgument(connection != null, "connection can not be null");
            return builder().setConnectionConfiguration(connection).build();
        }

        public Write withMethod(Method method) {
            checkArgument(method != null, "method can not be null");
            return builder().setMethod(method).build();
        }

        public Write withExpireTime(Long expireTimeMillis) {
            checkArgument(expireTimeMillis != null, "expireTimeMillis can not be null");
            checkArgument(expireTimeMillis > 0, "expireTimeMillis can not be negative or 0");
            return builder().setExpireTime(expireTimeMillis).build();
        }

        public Write withObjToByteStreamFunc(SerializableFunction<T, BytesContainer>  func){
            checkArgument(func!=null, "ObjToByteStreamFunc is required");
            return builder().setObjToByteStreamFunc(func).build();
        }

        @Override
        public PDone expand(PCollection<KV<String, T>> input) {
            checkArgument(connectionConfiguration() != null, "withConnectionConfiguration() is required");
            checkArgument(objToByteStreamFunc()!=null, "ObjToByteStreamFunc is required");

            input.apply(ParDo.of(new ToByteArray<>(objToByteStreamFunc())))
                    .apply(ParDo.of(new WriteFn(this)));
            return PDone.in(input.getPipeline());
        }
        @RequiredArgsConstructor
        private static class ToByteArray <T> extends DoFn< KV <String, T>,  KV<String, BytesContainer> >{
            private final SerializableFunction<T, BytesContainer> obj2ByteFunc;
            @ProcessElement
            public void processElement(@Element KV <String, T> element, OutputReceiver< KV<String, BytesContainer> > out) {
                BytesContainer bytesContainer = obj2ByteFunc.apply(element.getValue());
                out.output(KV.of(element.getKey(), bytesContainer));
            }
        }
        private static class WriteFn extends DoFn<KV<String, BytesContainer>, Void> {

            private static final int DEFAULT_BATCH_SIZE = 1000;

            private final Write spec;

            private transient Jedis jedis;
            private transient Pipeline pipeline;

            private int batchCount;

            public WriteFn(Write spec) {
                this.spec = spec;
            }

            @Setup
            public void setup() {
                jedis = spec.connectionConfiguration().connect();
            }

            @StartBundle
            public void startBundle() {
                pipeline = jedis.pipelined();
                pipeline.multi();
                batchCount = 0;
            }

            @ProcessElement
            public void processElement(ProcessContext processContext) {

                KV<String, BytesContainer> record = processContext.element();

                writeRecord(record);

                batchCount++;

                if (batchCount >= DEFAULT_BATCH_SIZE) {
                    pipeline.exec();
                    pipeline.sync();
                    pipeline.multi();
                    batchCount = 0;
                }
            }

            private void writeRecord(KV<String, BytesContainer> record) {
                Method method = spec.method();
                Long expireTime = spec.expireTime();

                 if (Method.SET == method) {
                    writeUsingSetCommand(record, expireTime);
                 }
            }



            private void writeUsingSetCommand(KV<String, BytesContainer> record, Long expireTime) {
                String key = record.getKey();
                BytesContainer value = record.getValue();
                byte[] byteValue = value.getData();
                if (expireTime != null) {
                    pipeline.psetex(key.getBytes(), expireTime, byteValue);
                } else {
                    pipeline.set(key.getBytes(), byteValue);
                }
                log.debug(String.format("write record %s into redis",key ));
            }

            private void setExpireTimeWhenRequired(String key, Long expireTime) {
                if (expireTime != null) {
                    pipeline.pexpire(key, expireTime);
                }
            }

            @FinishBundle
            public void finishBundle() {
                if (pipeline.isInMulti()) {
                    pipeline.exec();
                    pipeline.sync();
                }
                batchCount = 0;
            }

            @Teardown
            public void teardown() {
                jedis.close();
            }
        }
    }
}
