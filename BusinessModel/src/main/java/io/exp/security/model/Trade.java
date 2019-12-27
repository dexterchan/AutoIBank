package io.exp.security.model;

import java.io.Serializable;

public interface Trade extends Serializable {
    Asset getAsset();
}
