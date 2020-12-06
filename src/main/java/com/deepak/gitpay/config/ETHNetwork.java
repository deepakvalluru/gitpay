package com.deepak.gitpay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;

@Configuration
public class ETHNetwork implements Network{

   @Value("${networks.ether.name}")
   private SupportedNetwork name;

   @Value("${networks.ether.env}")
   private ETHNetwork.Env environment;

   @Value("${networks.ether.wallet_seed}")
   private String walletSeed;

   @Value("${networks.ether.amount}")
   private String amount;

   @Value("${networks.ether.server}")
   private String server;

   public enum Env

   { ROPSTEN,KOVAN, RINKEBY, GORLI;}

}
