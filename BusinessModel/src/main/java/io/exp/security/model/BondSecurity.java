package io.exp.security.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

import java.io.Serializable;
import org.joda.time.Instant;

@Data
public class BondSecurity implements Serializable {
    protected String securityId;
    protected String seniority;
    protected String issuerId;
    protected Instant placementDate;
    protected Instant maturity;
    protected String currency;
    protected String frequency;
    protected double amount;
    protected double couponRate;

    @Override
    public String toString() {
        Gson g = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                .create();
        return g.toJson(this);
    }

}
