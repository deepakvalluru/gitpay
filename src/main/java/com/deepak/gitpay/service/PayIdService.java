package com.deepak.gitpay.service;

import com.deepak.gitpay.config.Config;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import io.xpring.payid.PayIdException;
import io.xpring.xrpl.XrpException;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHPullRequestCommitDetail;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PayIdService
{
   private final Config config;
   private final XRPService xrpService;
   private final ETHService ethService;

   public static final String PAY_ID_PATTERN = "(\\S+\\$\\S+\\.\\S+)";
   public static final Pattern pattern = Pattern.compile( PAY_ID_PATTERN );
   public static final ObjectMapper objectMapper = new ObjectMapper();

   @Autowired
   PayIdService(Config config,
                XRPService xrpService, ETHService ethService) {
      this.config = config;
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
      }

      ArrayListMultimap<String, String> commitPayIdMap = ArrayListMultimap.create();
      AtomicInteger total = new AtomicInteger();
      Map<String, Integer> payIdMap = new HashMap<>();

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
                       total.getAndIncrement();
                       payIdMap.merge( payId, 1, Integer::sum );
                    }
                 }
              });

      System.out.println("Printing all unique PayIDs: ");
      payIdMap.keySet().forEach(System.out::println);

      System.out.println("Total XRP Amount in drops distributed = " + config.getXrpNetwork().getAmount());

      BigInteger amountPerPayIdPerCommit = new BigInteger(config.getXrpNetwork().getAmount()).divide( new BigInteger(total.toString()) );

      System.out.println("XRP Amount distributed per payId including duplicates = " + amountPerPayIdPerCommit);

      for( String commitId : commitPayIdMap.keySet() )
      {
         for ( String payId : commitPayIdMap.get( commitId ) )
         {
            xrpService.sendPayment(payId, amountPerPayIdPerCommit, commitId );
         }
      }


   }
}
