package com.deepak.gitpay.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class Config
{
   @Autowired
   private XRPNetwork xrpNetwork;

   @Autowired
   private ETHNetwork ethNetwork;

   @Autowired
   private BTCNetwork btcNetwork;

   @Value("${github_event}")
   private String githubEvent;

   @Value("${github_token}")
   private String githubToken;
}
