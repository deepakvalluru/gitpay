package com.deepak.gitpay.client.ethereum;

import io.xpring.payid.PayIdException;

public interface EthereumPayIdClientInterface
{
   EthereumNetwork getEthereumNetwork();

   String ethereumAddressForPayId(String payId) throws PayIdException;
}
