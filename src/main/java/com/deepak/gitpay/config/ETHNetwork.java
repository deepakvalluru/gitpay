package com.deepak.gitpay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ETHNetwork implements Network{

   @Value("${ether.name}")
   private SupportedNetwork name;

   @Value("${ether.env}")
   private ETHNetwork.Env environment;

   @Value("${ether.wallet_seed}")
   private String walletSeed;

   @Value("${ether.amount}")
   private String amount;

   @Value("${ether.server}")
   private String server;

   public enum Env

   { ROPSTEN,KOVAN, RINKEBY, GORLI }

}
