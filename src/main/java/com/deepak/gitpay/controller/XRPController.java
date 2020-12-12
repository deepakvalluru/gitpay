package com.deepak.gitpay.controller;

import com.deepak.gitpay.config.Config;
import com.deepak.gitpay.config.XRPNetwork;
import com.deepak.gitpay.model.AmountTransferRequest;
import com.deepak.gitpay.model.AmountTransferResponse;
import com.deepak.gitpay.model.BalanceResponse;
import com.deepak.gitpay.model.github.Root;
import com.deepak.gitpay.service.XRPService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.xpring.common.XrplNetwork;
import io.xpring.payid.PayIdClient;
import io.xpring.payid.PayIdException;
import io.xpring.payid.XrpPayIdClient;
import io.xpring.payid.generated.model.Address;
import io.xpring.xpring.XpringClient;
import io.xpring.xrpl.Wallet;
import io.xpring.xrpl.XrpClient;
import io.xpring.xrpl.XrpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
public class XRPController {

   private final XRPService xrpService;
   private final Config config;

   @Autowired
   XRPController( XRPService xrpService,
                  Config config ) {
      this.xrpService = xrpService;
      this.config = config;
   }

   @GetMapping("/payid/{payId}")
   public List<Address> getAddresses( @PathVariable("payId") String payId ) throws PayIdException, XrpException, JsonProcessingException {

      System.out.println("Github event: " + config.getGithubEvent() );

      Root actionEvent = new ObjectMapper().readValue( config.getGithubEvent(), Root.class);
      System.out.println( "printing marshalled action event's commits: \n" + actionEvent.getEvent().getCommits() );

      System.out.println("Running get address: " + payId);
      List<Address> addresses = new PayIdClient().allAddressesForPayId( payId );

      XrplNetwork network = getXrplNetwork( config.getXrpNetwork().getEnvironment() );
      System.out.println("Using XRP Environment : " + network.getNetworkName());

      XrpPayIdClient xrpPayIdClient = new XrpPayIdClient( network );
      XrpClient xrpClient = new XrpClient( config.getXrpNetwork().getServer(), network );

      XpringClient xpringClient = new XpringClient( xrpPayIdClient, xrpClient );

      System.out.println("Sending xrp amount in drops: " + config.getXrpNetwork().getAmount() + " to payId " + payId);

      xpringClient.send( new BigInteger( config.getXrpNetwork().getAmount()), payId, new Wallet( config.getXrpNetwork().getWalletSeed() ) );

      return addresses;
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

   @GetMapping(path = "balances/{address}")
   public BalanceResponse getBalance(@PathVariable("address") String address) {
      return xrpService.getBalance(address);
   }

   @PostMapping(path = "/send")
   public @ResponseBody
   AmountTransferResponse sendAmount(@RequestBody AmountTransferRequest amountTransferRequest) {
      return xrpService.sendAmount(amountTransferRequest);
   }
}
