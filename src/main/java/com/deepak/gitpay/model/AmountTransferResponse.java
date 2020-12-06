package com.deepak.gitpay.model;

import io.xpring.xrpl.TransactionStatus;
import io.xpring.xrpl.model.XrpTransaction;
import lombok.Data;

@Data
public class AmountTransferResponse {
   private XrpTransaction transaction;

   private TransactionStatus transactionStatus;

   private String message;
}
