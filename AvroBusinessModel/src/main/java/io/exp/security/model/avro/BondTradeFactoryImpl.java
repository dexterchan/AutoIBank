package io.exp.security.model.avro;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BondTradeFactoryImpl implements AbstractTradeFactory<BondTrade> {
    @Override
    public BondTrade createTrade(String securityId, String cust, String tradeDate, double notional, double price, String currency, BidAsk bidAsk) {

        UUID uuid = UUID.randomUUID();

        BondTrade.Builder builder = BondTrade.newBuilder();

        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Matcher match = pattern.matcher(tradeDate);
        if (!match.matches()){
            throw new IllegalArgumentException("Trade date should be YYYY-MM-DD");
        }

        builder.setId(uuid.toString());
        builder.setTimestamp(Instant.now().toEpochMilli());
        builder.setTradeType("BOND");
        builder.setTradeDate(tradeDate);
        builder.setCust(cust);
        Asset.Builder assetBuilder = Asset.newBuilder();
        assetBuilder.setSecurityId(securityId);
        assetBuilder.setNotional(notional);
        assetBuilder.setPrice(price);
        assetBuilder.setCurrency(currency);
        assetBuilder.setBidask(bidAsk);
        builder.setAsset(assetBuilder.build());
        return builder.build();
    }
}
