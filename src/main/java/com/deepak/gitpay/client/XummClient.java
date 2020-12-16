package com.deepak.gitpay.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.xpring.xrpl.Utils;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class XummClient
{

   private static final String XUMM_URL = "https://xumm.app/api/v1/platform/payload";

   RestTemplate restTemplate = new RestTemplate();

   private final ObjectMapper objectMapper = new ObjectMapper();

   public void callXumm(String destination, String amount)
   {
      try
      {
         if (Utils.isValidXAddress(destination))
         {
            destination = Utils.decodeXAddress(destination).address();
         }
         HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(getXummPaymentRequest( destination, amount )), getHttpHeaders());
         ResponseEntity<String> response = restTemplate.postForEntity(XUMM_URL, entity, String.class);
         System.out.println( "Xumm response : " + response.getBody());
      }
      catch ( Exception e)
      {
         e.printStackTrace();
      }

   }

   private XummPayment getXummPaymentRequest(String destination, String amount) {
      XummPayment paymentRequest = new XummPayment();
      Txjson txjson = Txjson.builder().transactionType("Payment").destination(destination).amount(amount).build();
      Options options = Options.builder().expire("1440").multisign("false").submit("true").build();
      paymentRequest.setTxjson(txjson);
      paymentRequest.setOptions(options);
      return paymentRequest;
   }


   private HttpHeaders getHttpHeaders() {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
      headers.setAccept(Stream.of(MediaType.ALL).collect(Collectors.toList()));
      headers.set("X-API-Key", "03039d35-cfea-48de-a998-98a908af7ffa");
      headers.set("X-API-Secret", "c1a9e60c-ecfa-4301-a5c2-9df124fb5eb0");
      return headers;
   }


   @Data
   @Builder
   public static class Txjson
   {
      @JsonProperty("TransactionType")
      public String transactionType;
      @JsonProperty("Destination")
      public String destination;
      @JsonProperty("Amount")
      public String amount;
   }

   @Data
   @Builder
   public static class Options
   {
      public String submit;
      public String multisign;
      public String expire;
   }

   @Data
   public class XummPayment
   {
      public Options options;
      public Txjson txjson;
   }
}
