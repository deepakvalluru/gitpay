//package com.deepak.gitpay.controller;
//
//import com.deepak.gitpay.model.AmountTransferRequest;
//import com.deepak.gitpay.model.AmountTransferResponse;
//import com.deepak.gitpay.service.XRPService;
//import org.apache.commons.codec.digest.HmacUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.kohsuke.github.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.io.StringReader;
//import java.math.BigInteger;
//import java.security.MessageDigest;
//import java.util.List;
//import java.util.Objects;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//@RestController
//public class GITController {
//   public static final String GIST_FILE_NAME = "XRPAddress";
//   private static final String GITHUB_USER_AGENT_PREFIX = "GitHub-Hookshot/";
//   private static final String SECRET_KEY = "A123456a";
//   private static final String PR_ACTION_CLOSED = "closed";
//   private static final String MASTER_BRANCH = "master";
//   private static final String MAIN_BRANCH = "main";
//   private static final BigInteger DEFAULT_XRP_TRANSFER_AMOUNT = BigInteger.valueOf(10000000);
//   private TestConstructs testConstructs;
//   private XRPService xrpService;
//   private static final Pattern pattern = Pattern.compile(".*(?i)XRPADDRESS-([$]{1}([\\w]+)[$]{1}) .*");
//
//   @Autowired
//   private Environment environment;
//
//   @Autowired
//   GITController(TestConstructs testConstructs, XRPService xrpService) {
//      this.testConstructs = testConstructs;
//      this.xrpService = xrpService;
//   }
//
//   @GetMapping(path = "/users/{userName}/repos")
//   public GHRepository getRepos(@PathVariable("userName") String userName) throws
//           IOException {
//
//      GitHub github = new GitHubBuilder().withOAuthToken("e11ed12a3cbabaf38946959af433c9eed723e278").build();
//      List<GHRepository> repositories = github.getUser(userName).getRepositories().values().stream().limit(1).collect(Collectors.toList());
//
//      System.out.println(repositories.get(0));
//      return repositories.get(0).getSource();
//   }
//
//   @GetMapping(path = "/users/{userName}/repos/{repoName}/pullrequests")
//   public List<GHPullRequest> getPullRequests(@PathVariable("userName") String userName,
//                                              @PathVariable("repoName") String repoName) throws IOException {
//
//      GitHub github = new GitHubBuilder().withOAuthToken("e11ed12a3cbabaf38946959af433c9eed723e278").build();
//      List<GHPullRequest> pullRequests = github.getRepository(userName + "/" + repoName).getPullRequests(GHIssueState.ALL);
//      System.out.println(pullRequests);
//      return pullRequests;
//   }
//
//   @GetMapping(path = "/pay")
//   public void payXRPBasedOnGists() throws IOException {
//      GitHub github = new GitHubBuilder().withOAuthToken("e11ed12a3cbabaf38946959af433c9eed723e278").build();
//      String xrpAddress = fetchXrpAddress( github.getMyself().getLogin());
//      if (xrpAddress != null) {
//         AmountTransferResponse response = xrpService.sendAmount(AmountTransferRequest.builder()
//                 .amount(BigInteger.valueOf(
//                         10000000))
//                 .destinationAddress(xrpAddress)
//                 .build());
//         System.out.println(response);
//      }
//
//   }
//
//   @PostMapping("/payload")
//   public ResponseEntity<String> receiveWebhookPayload(@RequestHeader("X-Hub-Signature") String signature,
//                                                       @RequestHeader("User-Agent") String userAgent,
//                                                       @RequestBody String payload) throws Exception {
//
//      HttpHeaders headers = new HttpHeaders();
//      System.out.println( payload );
//      if (Objects.isNull(userAgent) || !userAgent.startsWith(GITHUB_USER_AGENT_PREFIX)) {
//         return new ResponseEntity<>("Invalid request.", headers, HttpStatus.BAD_REQUEST);
//      }
//
//      if (signature == null) {
//         return new ResponseEntity<>("No signature given.", headers, HttpStatus.BAD_REQUEST);
//      }
//
//      if (!validateSignature(payload, signature)) {
//         return new ResponseEntity<>("Invalid signature.", headers, HttpStatus.UNAUTHORIZED);
//      }
//
//      GHEventPayload.PullRequest pullRequestWebHook = GitHub.offline().parseEventPayload(new StringReader(payload), GHEventPayload.PullRequest.class);
//
//      String jwtToken = TestConstructs.createJWT( "90219", 60000 );
//      GitHub githubApp = new GitHubBuilder().withJwtToken( jwtToken ).build();
//      GHAppInstallation appInstallation = githubApp.getApp().getInstallationById( 13207954 );
//      GHAppInstallationToken appInstallationToken = appInstallation.createToken().create();
//
//      GitHub github = new GitHubBuilder().withAppInstallationToken( appInstallationToken.getToken() ).build();
//      pullRequestWebHook = github.parseEventPayload(new StringReader(payload), GHEventPayload.PullRequest.class);
////      pullRequestWebHook.getPullRequest().wrapUp( github );
//
////      if (verifyIfPullRequestMergedToMaster(pullRequestWebHook)) {
//
//         String pullRequestUser = pullRequestWebHook.getPullRequest().getUser().getLogin();
//
//         String destinationXRPAddress = fetchXrpAddress(pullRequestUser);
//
//         if (destinationXRPAddress != null) {
//            AmountTransferRequest amountTransferRequest = AmountTransferRequest.builder()
//                    .amount(DEFAULT_XRP_TRANSFER_AMOUNT)
//                    .destinationAddress(destinationXRPAddress)
//                    .build();
//            AmountTransferResponse response = xrpService.sendAmount(amountTransferRequest);
//            System.out.println(response);
//            return ResponseEntity.ok("SUCCESS");
//         }
//         else {
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("UNABLE TO FIND ANY XRP ADDRESS IN ANY OF THE PUBLIC GISTS");
//         }
////      }
//
////      return ResponseEntity.accepted().body("NOT PAID OUT BECAUSE IT'S NOT A VALID PULL REQUEST");
//   }
//
//   private boolean validateSignature(String payload, String signature) {
//      Objects.nonNull(payload);
//      Objects.nonNull(signature);
//      String computed = String.format("sha1=%s", HmacUtils.hmacSha1Hex(SECRET_KEY, payload));
//      return MessageDigest.isEqual(signature.getBytes(), computed.getBytes());
//   }
//
//   private boolean verifyIfPullRequestMergedToMaster(GHEventPayload.PullRequest pullRequestWebHook) throws IOException {
//      return StringUtils.isNotBlank(pullRequestWebHook.getAction())
//              && PR_ACTION_CLOSED.equals(pullRequestWebHook.getAction())
//              && pullRequestWebHook.getPullRequest() != null
//              && pullRequestWebHook.getPullRequest().isMerged()
//              && (MAIN_BRANCH.equals(pullRequestWebHook.getPullRequest().getBase().getRef())
//              || MASTER_BRANCH.equals(pullRequestWebHook.getPullRequest().getBase().getRef()));
//   }
//
//   private String fetchXrpAddress( String userName) throws IOException {
//
//      // For some reason, I'm unable to access GISTS API from GitHub App. But I can make anonymous calls.
//      GitHub gitHub = new GitHubBuilder().build();
//      List<GHGist> gists = gitHub.getUser(userName).listGists().toList();
//      if (gists != null && !gists.isEmpty()) {
//         for (GHGist gist : gists) {
//            if (!gist.getFiles().isEmpty() && gist.getFiles().containsKey(GIST_FILE_NAME)) {
//               String xrpAddress = gitHub.getGist( gist.getGistId() )
//                       .getFiles().get(GIST_FILE_NAME)
//                       .getContent();
//               return xrpAddress;
//            }
//         }
//      }
//      return null;
//   }
//}
