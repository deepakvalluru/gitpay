package com.deepak.gitpay.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

@Data
@Builder
public class AmountTransferRequest {
   private BigInteger amount;

   private String destinationAddress;
}
