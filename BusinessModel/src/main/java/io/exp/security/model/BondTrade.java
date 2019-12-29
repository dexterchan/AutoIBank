package io.exp.security.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Instant;

import java.io.Serializable;


@Slf4j
@Getter @Setter
public class BondTrade implements Trade, Serializable {
    String id;
    Instant timestamp;
    String tradeType;

    BondAsset asset;

    @Override
    public String toString() {
        Gson g = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                .create();
        return g.toJson(this);
    }
}
