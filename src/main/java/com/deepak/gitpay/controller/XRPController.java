package com.deepak.gitpay.controller;

import com.deepak.gitpay.client.XummClient;
import com.deepak.gitpay.config.Config;
import com.deepak.gitpay.config.XRPNetwork;
import com.deepak.gitpay.service.XRPService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.xpring.common.XrplNetwork;
import io.xpring.payid.PayIdException;
import io.xpring.payid.XrpPayIdClient;
import io.xpring.xpring.XpringClient;
import io.xpring.xrpl.Wallet;
import io.xpring.xrpl.XrpClient;
import io.xpring.xrpl.XrpException;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHPullRequestCommitDetail;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class XRPController {

   private final XRPService xrpService;
   private final Config config;
   private final XummClient xummClient;

   public static final String PAY_ID_PATTERN = "(\\S+\\$\\S+\\.\\S+)";
   public static final Pattern pattern = Pattern.compile( PAY_ID_PATTERN );
   public static final ObjectMapper objectMapper = new ObjectMapper();

   @Autowired
   XRPController( XRPService xrpService,
                  Config config,
                  XummClient xummClient ) {
      this.xrpService = xrpService;
      this.config = config;
      this.xummClient = xummClient;
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   @GetMapping("/payid")
   public void getAddresses( ) throws PayIdException, XrpException, IOException {

      System.out.println("Github event: " + config.getGithubEvent() );

      GitHub gitHub = new GitHubBuilder().withOAuthToken( config.getGithubToken() ).build();

      GHEventPayload.PullRequest pullRequest = gitHub.parseEventPayload(new StringReader(config.getGithubEvent()), GHEventPayload.PullRequest.class);

      List<GHPullRequestCommitDetail> ghPullRequestCommitList = pullRequest.getPullRequest().listCommits().toList();

      if( ghPullRequestCommitList.isEmpty() )
      {
         System.out.println("NO COMMITS FOUND..NOTHING CAN BE DONE");
      }

      List<String> allPayIds = new ArrayList<>();

      ghPullRequestCommitList
              .forEach( commit -> {
                 System.out.println("Commit message: " + commit.getCommit().getMessage() );
                 Matcher matcher = pattern.matcher(commit.getCommit().getMessage());
                 while ( matcher.find() ) {
                    allPayIds.add(matcher.group());
                 }
      });

      System.out.println("Printing all PayIDs: ");
      allPayIds.forEach(System.out::println);

      for ( String payId : allPayIds )
      {
         XrplNetwork network = getXrplNetwork( config.getXrpNetwork().getEnvironment() );
         System.out.println("Using XRP Environment : " + network.getNetworkName());

         XrpPayIdClient xrpPayIdClient = new XrpPayIdClient( network );
         XrpClient xrpClient = new XrpClient( config.getXrpNetwork().getServer(), network );

         XpringClient xpringClient = new XpringClient( xrpPayIdClient, xrpClient );

         System.out.println("Sending xrp amount in drops: " + config.getXrpNetwork().getAmount() + " to payId: " + payId + " and XRP Address: " + xrpPayIdClient.xrpAddressForPayId(payId) );

         String transactionHash = xpringClient.send(new BigInteger(config.getXrpNetwork().getAmount()), payId, new Wallet(config.getXrpNetwork().getWalletSeed()));

         System.out.println("Transaction ID : " + transactionHash);

         xummClient.callXumm( xrpPayIdClient.xrpAddressForPayId(payId), config.getXrpNetwork().getAmount() );
      }

   }

   private XrplNetwork getXrplNetwork(XRPNetwork.Env env )
   {
      switch ( env )
      {
         case DEVNET:
            return XrplNetwork.DEV;
         case LIVENET:
            return XrplNetwork.MAIN;
         default:
            return XrplNetwork.TEST;
      }
   }

}
