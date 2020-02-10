package io.exp.analysis.match.beam.model;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class BondTradeActivityData implements Serializable {
    private String cust;
    private String original_maturity;
    private double notional;
    private String isin_code;
}
