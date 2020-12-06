package com.deepak.gitpay.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class XRPNetwork implements Network
{
   @Value("${xrp.name}")
   private SupportedNetwork name;

   @Value("${xrp.env}")
   private XRPNetwork.Env environment;

   @Value("${xrp.wallet_seed}")
   private String walletSeed;

   @Value("${xrp.amount}")
   private String amount;

   @Value("${xrp.server}")
   private String server;

   public enum Env

   { DEVNET,TESTNET, LIVENET }
}
