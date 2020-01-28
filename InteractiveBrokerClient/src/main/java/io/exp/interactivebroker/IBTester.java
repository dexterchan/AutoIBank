package io.exp.interactivebroker;

import com.ib.client.Contract;
import com.ib.client.ContractLookuper;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import io.exp.interactivebroker.client.EWrapperImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

//Reference: http://interactivebrokers.github.io/tws-api/contract_details.html#broadtape_list
@Slf4j
public class IBTester {
    static AtomicInteger reqInt = new AtomicInteger();

    public static void lookupContract(EClientSocket eClientSocket ){
        eClientSocket.reqMatchingSymbols(reqInt.incrementAndGet(), "Evergrande");
        //get result in func symbolSamples

    }
    //Not working for Bond
    public static void getSecurityDetails(EClientSocket eClientSocket, int contId){
        //Contract contract = new Contract();
        //contract.conid(contId);

        Contract contract = new Contract();
        // enter CUSIP as symbol
        contract.symbol("XS1587867539");
        contract.symbol("912828C57");
        contract.secType("BOND");
        contract.currency("USD");

        eClientSocket.reqContractDetails(reqInt.incrementAndGet(), contract);
        //get result in func contractDetails, contractDetailsEnd
    }
    public static void getSecurityOptionParams(EClientSocket eClientSocket ){
        eClientSocket.reqSecDefOptParams(reqInt.incrementAndGet(), "IBM", "",/* "",*/ "STK", 8314);

    }

    public static void getMarketData(EClientSocket eClientSocket){
        //historicalData
        Contract contract = new Contract();
        // enter CUSIP as symbol
        contract.symbol("XS1587867539");
        contract.secType("BOND");
        contract.currency("USD");
        contract.exchange("SMART");
        //SMART BOND not working

        contract.symbol("MSFT");
        contract.secType("STK");
        contract.currency("USD");
        contract.exchange("SMART");
        // Specify the Primary Exchange attribute to avoid contract ambiguity
        // (there is an ambiguity because there is also a MSFT contract with primary exchange = "AEB")
        contract.primaryExch("ISLAND");


        int reqNum = reqInt.incrementAndGet();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String formattedDate = form.format(cal.getTime());
        eClientSocket.reqHistoricalData(reqNum, contract, formattedDate, "1 M", "1 day", "MIDPOINT", 1, 1, false, null);

        try {
            Thread.sleep(2000);
        }catch(Exception e){

        }finally {
            /*** Canceling historical data requests ***/
            eClientSocket.cancelHistoricalData(reqNum);
        }
    }


    public static void main(String[] args){
        EWrapperImpl eWrapper = new EWrapperImpl();
        EClientSocket eClientSocket = eWrapper.getM_client();
        //getSecurityOptionParams(eClientSocket);
        //lookupContract(eClientSocket);
        //getSecurityDetails(eClientSocket,242485503);
        getMarketData(eClientSocket);
        try {
            System.in.read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(e.getMessage());
        }
        eClientSocket.eDisconnect();
    }
}
