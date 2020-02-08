package io.exp.security.model.avro;

import java.io.Serializable;

public interface Trade extends Serializable {
    Asset getAsset();
}
