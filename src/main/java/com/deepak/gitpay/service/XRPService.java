package com.deepak.gitpay.service;

import com.deepak.gitpay.controller.TestConstructs;
import com.deepak.gitpay.model.AmountTransferRequest;
import com.deepak.gitpay.model.AmountTransferResponse;
import com.deepak.gitpay.model.BalanceResponse;
import com.deepak.gitpay.model.ResponseStatus;
import io.xpring.xrpl.ImmutableClassicAddress;
import io.xpring.xrpl.TransactionStatus;
import io.xpring.xrpl.Utils;
import io.xpring.xrpl.XrpException;
import io.xpring.xrpl.model.XrpTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Objects;

@Service
public class XRPService {
   private final TestConstructs testConstructs;

   @Autowired
   XRPService(TestConstructs testConstructs) {
      this.testConstructs = testConstructs;
   }

   public BalanceResponse getBalance(String address) {
      BalanceResponse response = new BalanceResponse();
      response.setAddress(address);
      try {
         if (!Utils.isValidAddress(address)) {
            response.setResponseStatus(ResponseStatus.FAILED);
            response.setMessage("Not a valid address");
            return response;
         }

         BigInteger balance = testConstructs.getXrpClient()
                 .getBalance(transformClassicToXAddress(address))
                 .divide(BigInteger.valueOf(10000L));
         response.setResponseStatus(ResponseStatus.SUCCESS);
         response.setBalance(balance.toString());
      } catch (XrpException e) {
         e.printStackTrace();
         response.setResponseStatus(ResponseStatus.FAILED);
         response.setMessage(e.getMessage());
      }
      return response;
   }

   public AmountTransferResponse sendAmount(AmountTransferRequest amountTransferRequest) {
      AmountTransferResponse response = new AmountTransferResponse();
      try {
         if (!Utils.isValidAddress(amountTransferRequest.getDestinationAddress())) {
            response.setTransactionStatus(TransactionStatus.FAILED);
            response.setMessage("Not a valid address");
            return response;
         }

         String transactionHash = testConstructs.getXrpClient()
                 .send(amountTransferRequest.getAmount(),
                         transformClassicToXAddress(amountTransferRequest.getDestinationAddress()),
                         testConstructs.getWallet());
         XrpTransaction xrpTransaction = testConstructs.getXrpClient().getPayment(transactionHash);
         response.setTransaction(xrpTransaction);
         TransactionStatus status = testConstructs.getXrpClient().getPaymentStatus(transactionHash);
         response.setTransactionStatus(status);
      } catch (XrpException e) {
         e.printStackTrace();
         response.setTransactionStatus(TransactionStatus.UNKNOWN);
         response.setMessage(e.getMessage());
      }
      return response;
   }

   private String transformClassicToXAddress(String address) {
      Objects.requireNonNull(address);
      if (Utils.isValidXAddress(address)) {
         return address;
      }
      return Utils.encodeXAddress(ImmutableClassicAddress.builder().address(address).isTest(true).build());
   }
}
