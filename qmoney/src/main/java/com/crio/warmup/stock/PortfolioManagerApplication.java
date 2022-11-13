
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
//import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
//import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
// import java.util.Collections;
// import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
// import java.util.stream.Collectors;
// import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication 
{
   public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException 
   {
    ObjectMapper objectMapper = new ObjectMapper();
     objectMapper.registerModule(new JavaTimeModule());
     List<String> symboList = new ArrayList<>();
     try
     {
       PortfolioTrade[] trades = objectMapper.readValue(Paths.get(getPath(args[0])+""+args[0]).toFile(), PortfolioTrade[].class);
       for(PortfolioTrade trade : trades)
       {
         symboList.add(trade.getSymbol());
       }
       
     }
     catch(Exception e)
     {
       e.printStackTrace();
     }
     return symboList;
   }

  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException 
  {
    RestTemplate rest =new RestTemplate();
    List<String> result = new ArrayList<>();
    List<PortfolioTrade> trade = readTradesFromJson(args[0]);
    List<TiingoCandle> stocks = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    List<TotalReturnsDto> SortedClosingPrice = new ArrayList<>();
    try
    {
      for(PortfolioTrade trades : trade)
      {
        String url = prepareUrl(trades ,LocalDate.parse(args[1], formatter),"65be3207288ba2235033ef65b8e7b8311b07033d");
        TiingoCandle [] responseList = rest.getForObject(url, TiingoCandle[].class);
        stocks = Arrays.asList(responseList);
        
        TotalReturnsDto TotalReturnsDtoObject = new TotalReturnsDto(trades.getSymbol(), stocks.get(stocks.size()-1).getClose());
        SortedClosingPrice.add(TotalReturnsDtoObject);

      }
    }
    catch(Exception e)
    {
      //e.printStackTrace();
      throw new RuntimeException();
    }
    Collections.sort(SortedClosingPrice , (o1,o2) -> o1.getClosingPrice().compareTo(o2.getClosingPrice()));
    for(TotalReturnsDto symbols  : SortedClosingPrice)
          result.add(symbols.getSymbol());
    return result;
  }


  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException 
  {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    List<PortfolioTrade> symboList = new ArrayList<>();
    try
    {
      PortfolioTrade[] trades = objectMapper.readValue(Paths.get(getPath(filename)+""+filename).toFile(), PortfolioTrade[].class);
      for(PortfolioTrade trade : trades)
      {
        symboList.add(trade);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return symboList;
  }
  private static void printJsonObject(Object object) throws IOException {
   Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
   ObjectMapper mapper = new ObjectMapper();
   logger.info(mapper.writeValueAsString(object));
 }

  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     String url = "https://api.tiingo.com/tiingo/daily/"+trade.getSymbol()+"/prices?startDate="+trade.getPurchaseDate()+"&endDate="+endDate+"&token="+token;
     return url;
  }
  public static String getPath(String filename)
  {
      String path = filename.equals("trades.json")?"src/main/resources/":"src/test/resources/";
      return path;
  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "src/main/resources/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5542c4ed";
    String functionNameFromTestFileInStackTrace = "mainReadFile()";
    String lineNumberFromTestFileInStackTrace = "29:1";


   return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
       toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
       lineNumberFromTestFileInStackTrace});
 }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));
    printJsonObject(mainReadQuotes(args));


  }
}

