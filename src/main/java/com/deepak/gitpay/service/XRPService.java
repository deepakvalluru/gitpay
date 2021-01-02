package com.deepak.gitpay.service;

import com.deepak.gitpay.client.XummClient;
import com.deepak.gitpay.config.Config;
import com.deepak.gitpay.config.XRPNetwork;
import io.xpring.common.XrplNetwork;
import io.xpring.payid.PayIdException;
import io.xpring.payid.XrpPayIdClient;
import io.xpring.xpring.XpringClient;
import io.xpring.xrpl.Wallet;
import io.xpring.xrpl.XrpClient;
import io.xpring.xrpl.XrpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
public class XRPService
{
   private final Config config;
   private final XummClient xummClient;

   @Autowired
   public XRPService(Config config, XummClient xummClient) {
      this.config = config;
      this.xummClient = xummClient;
   }


   public void sendPayment(String payId, BigInteger amountToBeSent, String commitId ) throws XrpException, PayIdException {
      XrplNetwork network = getXrplNetwork( config.getXrpNetwork().getEnvironment() );
      System.out.println("Using XRP Environment : " + network.getNetworkName());

      XrpPayIdClient xrpPayIdClient = new XrpPayIdClient( network );

      final Optional<String> xrpAddress = getXrpAddress(xrpPayIdClient, payId);

      if( !xrpAddress.isPresent() )
      {
         System.out.println("Skipping the payment because payId is not valid");
         // TODO - throw an exception here.
         return;
      }

      XrpClient xrpClient = new XrpClient( config.getXrpNetwork().getServer(), network );

      XpringClient xpringClient = new XpringClient( xrpPayIdClient, xrpClient );


      System.out.println("Sending xrp amount in drops: " + amountToBeSent +
              " for commit Id: " + commitId +
              " to payId: " + payId +
              " and XRP Address: " + xrpAddress.get() );

      String transactionHash = xpringClient.send( amountToBeSent, payId, new Wallet(config.getXrpNetwork().getWalletSeed()));

      System.out.println("Transaction ID : " + transactionHash);

      xummClient.callXumm( xrpAddress.get(), amountToBeSent.toString(), commitId );
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
         case MAINNET:
            return XrplNetwork.MAIN;
         default:
            return XrplNetwork.TEST;
      }
   }
}
