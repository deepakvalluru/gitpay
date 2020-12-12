package com.deepak.gitpay;

import com.deepak.gitpay.controller.XRPController;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.xpring.payid.generated.model.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
public class GitPayApplication {

   @Autowired
   XRPController xrpController;

   @Autowired
   private ConfigurableApplicationContext context;

   public static void main(String[] args) {
      SpringApplication.run(GitPayApplication.class, args);
   }

   @Bean
   public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
      return args -> {

         System.out.println("Running the application");
         List<Address> addresses = xrpController.getAddresses("deepakvalluru$ripplex.money");
         addresses.forEach(System.out::println);
         SpringApplication.exit( context );
      };
   }

   @Bean
   public ObjectMapper objectMapper() {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      mapper.getSerializerProvider().setNullKeySerializer(new MyDtoNullKeySerializer());
      return mapper;
   }

   class MyDtoNullKeySerializer extends JsonSerializer<Object> {
      @Override
      public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused) throws IOException {
         jsonGenerator.writeFieldName("");
      }
   }

}
