package com.deepak.gitpay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BTCNetwork implements Network{

   @Value("${bitcoin.name}")
   private SupportedNetwork name;

   @Value("${bitcoin.env : TESTNET}")
   private BTCNetwork.Env environment;

   @Value("${bitcoin.wallet_seed}")
   private String walletSeed;

   @Value("${bitcoin.amount}")
   private String amount;

   @Value("${bitcoin.server}")
   private String server;

   public enum Env

   { TESTNET, MAINNET }
}
