package io.exp.gateway;

import java.util.Map;

public interface SecurityInterface {
    Map searchContract(String symbol, boolean byName, String secType);
    Map getContractDetails(String conid);
}
