package com.deepak.gitpay.config;

public interface Network
{
   enum SupportedNetwork
   {
      XRPL("XRPL", 1),
      ETH("ETH", 2),
      BTC("BTC", 3),
      ILP("ILP", 4 ),
      INTERLEDGER("INTERLEDGER", 5);

      private String network;
      private int priority;

      SupportedNetwork(String network, int priority)
      {
         this.network = network;
         this.priority = priority;
      }

      public String getNetwork()
      {
         return this.network;
      }

      public int getPriority()
      {
         return this.priority;
      }

   }
}
