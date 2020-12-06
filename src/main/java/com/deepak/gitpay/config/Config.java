package com.deepak.gitpay.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class Config
{
   @Autowired
   private XRPNetwork xrpNetwork;

   @Autowired
   private ETHNetwork   ethNetwork;

   @Autowired
   private BTCNetwork btcNetwork;
}
