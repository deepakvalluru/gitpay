package com.deepak.gitpay.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;

@Configuration
@Getter
public class XRPNetwork implements Network
{
   @Value("${networks.xrp.name}")
   private SupportedNetwork name;

   @Value("${networks.xrp.env}")
   private XRPNetwork.Env environment;

   @Value("${networks.xrp.wallet_seed}")
   private String walletSeed;

   @Value("${networks.xrp.amount}")
   private String amount;

   @Value("${networks.xrp.server}")
   private String server;

   public enum Env

   { DEVNET,TESTNET, LIVENET;}
}
