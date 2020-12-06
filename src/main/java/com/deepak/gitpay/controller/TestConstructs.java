package com.deepak.gitpay.controller;

import com.google.common.io.Files;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.xpring.common.XrplNetwork;
import io.xpring.xrpl.Wallet;
import io.xpring.xrpl.XrpClient;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TestConstructs {
   private static final String SEED = "shJHFR8ZKZ2m2kjRUJWTJNMwHhKXu";
   private static final String GRPC_URL = "test.xrp.xpring.io:50051";
   private static final String GITHUB_ACCESS_TOKEN = "e11ed12a3cbabaf38946959af433c9eed723e278";
   private XrpClient xrpClient;
   private Wallet wallet;

//   @PostConstruct
//   public void setup() throws Exception {
//      wallet = new Wallet(SEED);
//      xrpClient = new XrpClient(GRPC_URL, XrplNetwork.TEST);
//   }

   public Wallet getWallet() {
      return wallet;
   }

   public XrpClient getXrpClient() {
      return xrpClient;
   }

   static PrivateKey get(String filename) throws Exception {
      byte[] keyBytes = Files.toByteArray(new File(filename));

      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return kf.generatePrivate(spec);
   }

   static String createJWT(String githubAppId, long ttlMillis) throws Exception {
      //The JWT signature algorithm we will be using to sign the token
      SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;

      long nowMillis = System.currentTimeMillis();
      Date now = new Date(nowMillis);

      //We will sign our JWT with our private key
      Key signingKey = get("/Users/deepakvalluru/Documents/Workspace/gitpay/src/main/resources/pay-contributors-in-crypto.2020-11-25.private-key.der");

      //Let's set the JWT Claims
      JwtBuilder builder = Jwts.builder()
              .setIssuedAt(now)
              .setIssuer(githubAppId)
              .signWith(signingKey, signatureAlgorithm);

      //if it has been specified, let's add the expiration
      if (ttlMillis > 0) {
         long expMillis = nowMillis + ttlMillis;
         Date exp = new Date(expMillis);
         builder.setExpiration(exp);
      }

      //Builds the JWT and serializes it to a compact, URL-safe string
      return builder.compact();
   }

}
