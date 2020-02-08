package io.exp.gateway;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.exp.security.model.avro.BidAsk;
import io.exp.security.model.avro.BondTrade;
import lombok.extern.slf4j.Slf4j;

import org.apache.beam.sdk.io.AvroSource;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.commons.cli.*;

import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


/********
 * Reference: https://www.programcreek.com/java-api-examples/?api=org.apache.beam.sdk.coders.AvroCoder
 */
@Slf4j
public class BondTradeAvroReaderMain {

    public static Supplier<Options> createOptions = ()->{
        // create the Options
        Options options = new Options();
        options.addOption("i","inputFile", true, "Trade input from a avro file" );
        options.addOption(Option.builder()
                .hasArg(true)
                .argName("outputFile")
                .longOpt("o")
                .desc("Bid Ask output to json file")
                .build());
        return options;
    };

    public static void main(String []args) throws Exception{
        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = commandLineParser.parse(createOptions.get(), args);
        String fileName = commandLine.getOptionValue("inputFile");
        String bidAskOutPutFile = commandLine.getOptionValue("outputFile","bidAskAvg.json");

        AvroSource<BondTrade> source = AvroSource.from(fileName).withSchema(BondTrade.class);

        List<BondTrade> res = new ArrayList<>();
        try (BoundedSource.BoundedReader<BondTrade> reader = source.createReader(null)) {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                boolean shouldStart = (i == 0 );
                boolean more = shouldStart ? reader.start() : reader.advance();

                if (!more) {
                    break;
                }
                res.add(reader.getCurrent());
            }
        }
        log.debug(String.format("Number of record: %d", res.size()));
        res.stream().forEach((bondTrade)->{
            log.debug(bondTrade.toString());
        });
        Map<String, Double> BidPriceAvg = getAvgBidAskPrice(res, BidAsk.BID);
        Map<String, Double> AskPriceAvg = getAvgBidAskPrice(res, BidAsk.ASK);


        Gson gson  = new Gson();
        Writer writer = new FileWriter(String.format("bid.%s",bidAskOutPutFile));
        gson.toJson(BidPriceAvg, writer);
        writer.close();
        writer = new FileWriter(String.format("ask.%s",bidAskOutPutFile));
        gson.toJson(AskPriceAvg, writer);
        writer.close();


    }

    static Map <String, Double> getAvgBidAskPrice(List<BondTrade> bondTrades, BidAsk bidask){
        Map<String, Double> AvgBidAskPriceMap = Maps.newHashMap();

        Map<String, Integer> NumberOfTradePerSec = Maps.newHashMap();
        Map<String, Double> SumOfPricePerSec = Maps.newHashMap();
        bondTrades.stream().filter(
                bondTrade->bondTrade.getAsset().getBidask() == bidask
        ).forEach((bondTrade)->{
            String SecId = bondTrade.getAsset().getSecurityId();
            Double price = bondTrade.getAsset().getPrice();

            NumberOfTradePerSec.merge(SecId, 1 , (accum, one)->accum + 1);
            SumOfPricePerSec.merge(SecId, price, (accum, one)-> accum + price);
        });
        SumOfPricePerSec.forEach(
                (secid, Price)->{
                    int numOfTrade = NumberOfTradePerSec.get(secid);
                    AvgBidAskPriceMap.put(secid, Price / numOfTrade);
                }
        );
        return AvgBidAskPriceMap;
    }
}
