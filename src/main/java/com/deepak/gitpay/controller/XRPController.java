package com.deepak.gitpay.controller;

import com.deepak.gitpay.config.Config;
import com.deepak.gitpay.config.XRPNetwork;
import com.deepak.gitpay.model.AmountTransferRequest;
import com.deepak.gitpay.model.AmountTransferResponse;
import com.deepak.gitpay.model.BalanceResponse;
import com.deepak.gitpay.service.XRPService;
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
   public List<Address> getAddresses( @PathVariable("payId") String payId ) throws PayIdException, XrpException {

      System.out.println("Running get address: " + payId);
      List<Address> addresses = new PayIdClient().allAddressesForPayId( payId );

      XrplNetwork network = getXrplNetwork( config.getXrpNetwork().getEnvironment() );

      XrpPayIdClient xrpPayIdClient = new XrpPayIdClient( network );
      XrpClient xrpClient = new XrpClient( config.getXrpNetwork().getServer(), network );

      XpringClient xpringClient = new XpringClient( xrpPayIdClient, xrpClient );

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
