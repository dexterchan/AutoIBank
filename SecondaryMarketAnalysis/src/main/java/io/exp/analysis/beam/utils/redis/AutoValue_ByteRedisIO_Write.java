package io.exp.analysis.beam.utils.redis;

import org.apache.beam.sdk.io.redis.RedisConnectionConfiguration;
import org.apache.beam.sdk.transforms.SerializableFunction;

import javax.annotation.Nullable;


public class AutoValue_ByteRedisIO_Write<T> extends ByteRedisIO.Write<T> {
    private final RedisConnectionConfiguration connectionConfiguration;
    private final Method method;
    private final Long expireTime;
    private final SerializableFunction<T, BytesContainer> objToByteStreamFunc;

    public AutoValue_ByteRedisIO_Write(@Nullable RedisConnectionConfiguration connectionConfiguration, @Nullable Method method, @Nullable Long expireTime, @Nullable SerializableFunction<T, BytesContainer> objToByteStreamFunc) {
        this.connectionConfiguration = connectionConfiguration;
        this.method = method;
        this.expireTime = expireTime;
        this.objToByteStreamFunc = objToByteStreamFunc;
    }

    @Nullable
    RedisConnectionConfiguration connectionConfiguration() {

        return this.connectionConfiguration;
    }

    @Nullable
    Method method() {
        return this.method;
    }

    @Nullable
    Long expireTime() {
        return this.expireTime;
    }
    @Nullable
    SerializableFunction<T, BytesContainer> objToByteStreamFunc(){

        return this.objToByteStreamFunc;
    }

    @Override
    Builder builder() {

        return new AutoValue_ByteRedisIO_Write.Builder(this);
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AutoValue_ByteRedisIO_Write)) return false;
        final AutoValue_ByteRedisIO_Write<?> other = (AutoValue_ByteRedisIO_Write<?>) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$connectionConfiguration = this.connectionConfiguration;
        final Object other$connectionConfiguration = other.connectionConfiguration;
        if (this$connectionConfiguration == null ? other$connectionConfiguration != null : !this$connectionConfiguration.equals(other$connectionConfiguration))
            return false;
        final Object this$method = this.method;
        final Object other$method = other.method;
        if (this$method == null ? other$method != null : !this$method.equals(other$method)) return false;
        final Object this$expireTime = this.expireTime;
        final Object other$expireTime = other.expireTime;
        if (this$expireTime == null ? other$expireTime != null : !this$expireTime.equals(other$expireTime))
            return false;
        final Object this$objToByteStreamFunc = this.objToByteStreamFunc;
        final Object other$objToByteStreamFunc = other.objToByteStreamFunc;
        if (this$objToByteStreamFunc == null ? other$objToByteStreamFunc != null : !this$objToByteStreamFunc.equals(other$objToByteStreamFunc))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AutoValue_ByteRedisIO_Write;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $connectionConfiguration = this.connectionConfiguration;
        result = result * PRIME + ($connectionConfiguration == null ? 43 : $connectionConfiguration.hashCode());
        final Object $method = this.method;
        result = result * PRIME + ($method == null ? 43 : $method.hashCode());
        final Object $expireTime = this.expireTime;
        result = result * PRIME + ($expireTime == null ? 43 : $expireTime.hashCode());
        final Object $objToByteStreamFunc = this.objToByteStreamFunc;
        result = result * PRIME + ($objToByteStreamFunc == null ? 43 : $objToByteStreamFunc.hashCode());
        return result;
    }

    static final class Builder<T> extends ByteRedisIO.Write.Builder<T> {
        private RedisConnectionConfiguration connectionConfiguration;
        private Method method;
        private Long expireTime;
        private SerializableFunction<T, BytesContainer> objToByteStreamFunc;

        Builder() {
        }

        private Builder(ByteRedisIO.Write source) {
            this.connectionConfiguration = source.connectionConfiguration();
            this.method = source.method();
            this.expireTime = source.expireTime();
            this.objToByteStreamFunc = source.objToByteStreamFunc();
        }

        @Override
        ByteRedisIO.Write.Builder setConnectionConfiguration(RedisConnectionConfiguration connectionConfiguration) {
            this.connectionConfiguration = connectionConfiguration;
            return this;
        }

        @Override
        ByteRedisIO.Write.Builder setMethod(Method method) {
            this.method = method;
            return this;
        }

        @Override
        ByteRedisIO.Write.Builder setExpireTime(Long expireTimeMillis) {
            this.expireTime = expireTime;
            return this;
        }

        @Override
        ByteRedisIO.Write.Builder setObjToByteStreamFunc(SerializableFunction objToByteStreamFunc) {
            this.objToByteStreamFunc = objToByteStreamFunc;
            return this;
        }

        @Override
        ByteRedisIO.Write build() {
            return new AutoValue_ByteRedisIO_Write(this.connectionConfiguration,
                    this.method,
                    this.expireTime,
                    this.objToByteStreamFunc);
        }
    }

}
