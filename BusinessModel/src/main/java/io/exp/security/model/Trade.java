package io.exp.security.model;

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

}
