package com.deepak.gitpay.model;

import lombok.Data;

@Data
public class BalanceResponse {
   private String address;
   private ResponseStatus responseStatus;
   private String balance;
   private String message;
}
