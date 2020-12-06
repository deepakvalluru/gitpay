package com.deepak.gitpay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class BTCNetwork implements Network{

   @Value("${networks.bitcoin.name}")
   private SupportedNetwork name;

   @Value("${networks.bitcoin.env : TESTNET}")
   private BTCNetwork.Env environment;

   @Value("${networks.bitcoin.wallet_seed}")
   private String walletSeed;

   @Value("${networks.bitcoin.amount}")
   private String amount;

   @Value("${networks.bitcoin.server}")
   private String server;

   public enum Env

   { TESTNET, MAINNET;}
}
