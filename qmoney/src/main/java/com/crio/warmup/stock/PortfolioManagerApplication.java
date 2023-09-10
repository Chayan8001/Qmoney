
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
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
      List<String> symboList = new ArrayList<>();
      try
      {
        List<PortfolioTrade> trades = readTradesFromJson(args[0]);
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
  public static List<Candle> readTiingoApi(PortfolioTrade trades,String date)
  {
    RestTemplate rest =new RestTemplate();
    List<Candle> data = new ArrayList<>();
    try
    {
      String url = prepareUrl(trades ,getDate(date),getToken());
        Candle [] responseList = rest.getForObject(url, TiingoCandle[].class);
        data = Arrays.asList(responseList);

    }
    catch(Exception e)
    {
      //e.printStackTrace();
      throw new RuntimeException();
    }
    return data;
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException 
  {
    List<String> result = new ArrayList<>();
    List<PortfolioTrade> trade = readTradesFromJson(args[0]);
    List<Candle> stocks = new ArrayList<>();
    List<TotalReturnsDto> SortedClosingPrice = new ArrayList<>();

    for(PortfolioTrade trades : trade)
    {
      stocks = readTiingoApi(trades , args[1]);
      TotalReturnsDto TotalReturnsDtoObject = new TotalReturnsDto(trades.getSymbol(), stocks.get(stocks.size()-1).getClose());
      SortedClosingPrice.add(TotalReturnsDtoObject);

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
 // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

 public static LocalDate getDate(String date)
 {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    return LocalDate.parse(date , formatter);
 }

  public static String getToken()
  {
      String token = "65be3207288ba2235033ef65b8e7b8311b07033d";
      return token;
  }
  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) 
  {
    return candles.get(0).getOpen();
  }


 public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size()-1).getClose();
 }


 public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) 
 {
   return readTiingoApi(trade, endDate.toString());
 }

 public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
    throws IOException, URISyntaxException {
    List<PortfolioTrade> trade = readTradesFromJson(args[0]);
    double purchasePrice = 0.0;
    double sellingPrice = 0.0;
    List<AnnualizedReturn> annualizedReturn = new ArrayList<>();
    List<Candle> candles = new ArrayList<>();
    for(PortfolioTrade trades : trade)
    {
      candles = readTiingoApi(trades , args[1]);
      purchasePrice = getOpeningPriceOnStartDate(candles);
      sellingPrice = getClosingPriceOnEndDate(candles);
      annualizedReturn.add(calculateAnnualizedReturns(getDate(args[1]),trades , purchasePrice , sellingPrice));
    }
    Collections.sort(annualizedReturn , (o1,o2) -> o2.getAnnualizedReturn().compareTo(o1.getAnnualizedReturn()));
    return annualizedReturn;
 }

 // TODO: CRIO_TASK_MODULE_CALCULATIONS
 //  Return the populated list of AnnualizedReturn for all stocks.
 //  Annualized returns should be calculated in two steps:
 //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
 //      1.1 Store the same as totalReturns
 //   2. Calculate extrapolated annualized returns by scaling the same in years span.
 //      The formula is:
 //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
 //      2.1 Store the same as annualized_returns
 //  Test the same using below specified command. The build should be successful.
 //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

 public static double getYear(LocalDate startDate , LocalDate endDate)
 {
    double year = startDate.until(endDate, ChronoUnit.DAYS)/365.24;
    return year;
 }

 public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
     PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      double total_returns = (sellPrice - buyPrice)/buyPrice;
      double total_num_years = getYear(trade.getPurchaseDate() , endDate);
      double annualized_returns = Math.pow((1 + total_returns),(1 /(double)total_num_years))-1;
     return new AnnualizedReturn(trade.getSymbol(),annualized_returns,total_returns);
 }



  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));
    printJsonObject(mainReadQuotes(args));
    printJsonObject(mainCalculateSingleReturn(args));


  }
}

