package io.exp.security.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@Getter
public class Trade {
    String id;
    Date timestamp;
    String tradeType;
    Asset asset;

    @Override
    public String toString() {
        Gson g = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                .create();
        return g.toJson(this);
    }
}
