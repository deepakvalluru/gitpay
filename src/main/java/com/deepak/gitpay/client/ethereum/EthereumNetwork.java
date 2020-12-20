package com.deepak.gitpay.client.ethereum;

public enum EthereumNetwork {

  MAINNET("MAINNET"),
  ROPSTEN("ROPSTEN"),
  KOVAN("KOVAN"),
  RINKEBY("RINKEBY"),
  GORLI("GORLI");

  private String networkName;

  EthereumNetwork(String networkName) {
    this.networkName = networkName;
  }

  public String getNetworkName() {
    return networkName;
  }
}
