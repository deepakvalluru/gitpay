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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

      AtomicInteger total = new AtomicInteger();
      Map<String, Integer> payIdMap = new HashMap<>();

      ghPullRequestCommitList
              .forEach( commit -> {
                 System.out.println("Commit message: " + commit.getCommit().getMessage() );
                 Matcher matcher = pattern.matcher(commit.getCommit().getMessage());
                 while ( matcher.find() ) {
                    String payId = matcher.group();
                    total.getAndIncrement();
                    payIdMap.merge( payId, 1, Integer::sum );
                 }
      });

      System.out.println("Printing all unique PayIDs: ");
      payIdMap.keySet().forEach(System.out::println);

      System.out.println("Total XRP Amount in drops distributed = " + config.getXrpNetwork().getAmount());

      BigInteger amountPerPayId = new BigInteger(config.getXrpNetwork().getAmount()).divide( new BigInteger(total.toString()) );

      System.out.println("XRP Amount distributed per payId including duplicates = " + amountPerPayId);

      for ( String payId : payIdMap.keySet() )
      {
         XrplNetwork network = getXrplNetwork( config.getXrpNetwork().getEnvironment() );
         System.out.println("Using XRP Environment : " + network.getNetworkName());

         XrpPayIdClient xrpPayIdClient = new XrpPayIdClient( network );

         final Optional<String> xrpAddress = getXrpAddress(xrpPayIdClient, payId);

         if( !xrpAddress.isPresent() )
         {
            System.out.println("Skipping the payment because payid is not valid");
            continue;
         }

         XrpClient xrpClient = new XrpClient( config.getXrpNetwork().getServer(), network );

         XpringClient xpringClient = new XpringClient( xrpPayIdClient, xrpClient );

         BigInteger amountToBeSent = new BigInteger( payIdMap.get( payId ).toString() ).multiply( amountPerPayId );

         System.out.println("Sending xrp amount in drops: " + amountToBeSent +
                              " for " + payIdMap.get(payId) + " commits" +
                              " to payId: " + payId +
                              " and XRP Address: " + xrpAddress.get() );

         String transactionHash = xpringClient.send( amountToBeSent, payId, new Wallet(config.getXrpNetwork().getWalletSeed()));

         System.out.println("Transaction ID : " + transactionHash);

         xummClient.callXumm( xrpAddress.get(), config.getXrpNetwork().getAmount() );
      }

   }

   private Optional<String> getXrpAddress(XrpPayIdClient xrpPayIdClient, String payId)
   {
      Optional classicXrpAddress = Optional.empty();
      try {
         classicXrpAddress = Optional.of( xrpPayIdClient.xrpAddressForPayId(payId) );
         System.out.println( payId + " resolves to xrpAddress : " + classicXrpAddress.get() );
      } catch (PayIdException e) {
         System.out.println("Invalid Pay Id : " + payId + " because of " + e.getMessage() );
      }

      return classicXrpAddress;
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
