package com.deepak.gitpay.service;

import com.deepak.gitpay.config.Config;
import com.deepak.gitpay.config.Network;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import io.xpring.payid.PayIdClient;
import io.xpring.payid.PayIdException;
import io.xpring.payid.generated.model.Address;
import io.xpring.xrpl.XrpException;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHPullRequestCommitDetail;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PayIdService
{
   private final Config config;
   private final PayIdClient payIdClient;
   private final XRPService xrpService;
   private final ETHService ethService;

   public static final String PAY_ID_PATTERN = "(\\S+\\$\\S+\\.\\S+)";
   public static final Pattern pattern = Pattern.compile( PAY_ID_PATTERN );
   public static final ObjectMapper objectMapper = new ObjectMapper();

   @Autowired
   PayIdService(Config config,
                XRPService xrpService,
                ETHService ethService) {
      this.config = config;
      this.payIdClient = new PayIdClient();
      this.xrpService = xrpService;
      this.ethService = ethService;
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public void sendPayment() throws XrpException, IOException, PayIdException {
      System.out.println("Github event: " + config.getGithubEvent() );

      GitHub gitHub = new GitHubBuilder().withOAuthToken( config.getGithubToken() ).build();

      GHEventPayload.PullRequest pullRequest = gitHub.parseEventPayload(new StringReader(config.getGithubEvent()), GHEventPayload.PullRequest.class);

      List<GHPullRequestCommitDetail> ghPullRequestCommitList = pullRequest.getPullRequest().listCommits().toList();

      if( ghPullRequestCommitList.isEmpty() )
      {
         System.out.println("NO COMMITS FOUND..NOTHING CAN BE DONE");
         return;
      }

      ArrayListMultimap<String, String> commitPayIdMap = ArrayListMultimap.create();
      Set<String> uniquePayIds = new HashSet<>();

      ghPullRequestCommitList
              .forEach( commit -> {
                 String commitId = commit.getSha();
                 System.out.println("Commit message: " + commit.getCommit().getMessage() );
                 Matcher matcher = pattern.matcher(commit.getCommit().getMessage());
                 while ( matcher.find() ) {
                    String payId = matcher.group();
                    if( !commitPayIdMap.get( commitId ).contains( payId ) )
                    {
                       commitPayIdMap.get(commitId).add( payId );
                       uniquePayIds.add(payId);
                    }
                 }
              });

      System.out.println("Printing all unique PayIDs: ");
      uniquePayIds.forEach(System.out::println);

      int commitsWithValidPayId = commitPayIdMap.keySet().size();

      System.out.println("Total XRP Amount in drops distributed = " + config.getXrpNetwork().getAmount());
      System.out.println("Total ETH Amount distributed = " + config.getEthNetwork().getAmount());

      BigInteger xrpAmountPerCommit = new BigInteger(config.getXrpNetwork().getAmount()).divide( BigInteger.valueOf( commitsWithValidPayId ) );
      BigDecimal ethAmountPerCommit = new BigDecimal(config.getEthNetwork().getAmount()).divide( BigDecimal.valueOf( commitsWithValidPayId ) );

      System.out.println("XRP Amount distributed per commit = " + xrpAmountPerCommit);
      System.out.println("ETH Amount distributed per commit = " + ethAmountPerCommit);

      for( String commitId : commitPayIdMap.keySet() )
      {
         List<String> payIdsForThisCommit = commitPayIdMap.get(commitId);
         for ( String payId : payIdsForThisCommit)
         {
            Optional<Network.SupportedNetwork> networkOptional = determineNetworkToUse( payId );

            if( !networkOptional.isPresent() )
            {
               System.out.println("payId " + payId +" is not associated with any network. Skipping..");
               continue;
            }

            if( Network.SupportedNetwork.XRPL.equals( networkOptional.get() ) )
            {
               xrpService.sendPayment(payId, xrpAmountPerCommit.divide(BigInteger.valueOf(payIdsForThisCommit.size())), commitId );
            }
            else if( Network.SupportedNetwork.ETH.equals( networkOptional.get() ) )
            {
               ethService.sendPayment(payId, String.valueOf( ethAmountPerCommit.divide(BigDecimal.valueOf(payIdsForThisCommit.size())) ), commitId );
            }
            else
            {
               System.out.println("Payment Network not supported yet - " + networkOptional.get() );
               continue;
            }
         }
      }

   }

   private Optional<Network.SupportedNetwork> determineNetworkToUse(String payId) throws PayIdException {

      Set<Network.SupportedNetwork> networksWithWallets = getSupportedNetworksThatHasValidWallets();
      if( networksWithWallets.isEmpty() )
      {
         return Optional.empty();
      }

      Set<Network.SupportedNetwork> payIdSupportedNetworks = getSupportedNetworksPresentInPayId(payId);

      if( payIdSupportedNetworks.isEmpty() )
      {
         return Optional.empty();
      }

      Set<Network.SupportedNetwork> intersection = new HashSet<>( networksWithWallets );
      intersection.retainAll( payIdSupportedNetworks );

      if( intersection.isEmpty() )
      {
         System.out.println("There is no common network between PayId supported Networks and given wallets");
         return Optional.empty();
      }

      return Optional.of( new ArrayList<>(intersection).get(0) );
   }

   private Set<Network.SupportedNetwork> getSupportedNetworksPresentInPayId(String payId) {
      List<Address> addresses;
      try {
         addresses = payIdClient.allAddressesForPayId(payId);
      } catch (PayIdException e) {
         System.out.println("Exception when getting addresses for the given payId: " + e.getMessage());
         return Collections.emptySet();
      }

      // TODO - Have to filter out the network and environment combination.
      Set<Network.SupportedNetwork> payIdSupportedNetworks = addresses
                                                               .stream()
                                                               .map(address -> Network.SupportedNetwork.valueOf(address.getPaymentNetwork()))
                                                               .collect(Collectors.toSet());
      return payIdSupportedNetworks;
   }

   private Set<Network.SupportedNetwork> getSupportedNetworksThatHasValidWallets() {
      Set<Network.SupportedNetwork> networksWithWallets = new HashSet<>();

      if(StringUtils.isNotBlank( config.getXrpNetwork().getWalletSeed() ) )
      {
         networksWithWallets.add( config.getXrpNetwork().getName() );
      }

      if( StringUtils.isNotBlank( config.getEthNetwork().getWalletSeed()) )
      {
         networksWithWallets.add( config.getEthNetwork().getName() );
      }

      if( StringUtils.isNotBlank( config.getBtcNetwork().getWalletSeed()) )
      {
         networksWithWallets.add( config.getBtcNetwork().getName() );
      }
      return networksWithWallets;
   }
}
