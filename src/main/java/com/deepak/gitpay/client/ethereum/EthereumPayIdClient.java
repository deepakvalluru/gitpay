package com.deepak.gitpay.client.ethereum;

import io.xpring.payid.PayIdClient;
import io.xpring.payid.PayIdException;

public class EthereumPayIdClient extends PayIdClient implements EthereumPayIdClientInterface {

   private EthereumNetwork ethereumNetwork;

   public EthereumPayIdClient(EthereumNetwork ethereumNetwork) {
      super();
      this.ethereumNetwork = ethereumNetwork;
   }

   @Override
   public EthereumNetwork getEthereumNetwork() {
      return this.ethereumNetwork;
   }

   @Override
   public String ethereumAddressForPayId(String payId) throws PayIdException {
      return null;
      // TODO
   }
}
