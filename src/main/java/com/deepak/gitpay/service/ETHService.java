package com.deepak.gitpay.service;

import com.deepak.gitpay.client.ethereum.EthereumNetwork;
import com.deepak.gitpay.client.ethereum.EthereumPayIdClient;
import com.deepak.gitpay.config.Config;
import com.deepak.gitpay.config.ETHNetwork;
import io.xpring.payid.PayIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

@Service
public class ETHService
{
   private final Config config;

   @Autowired
   public ETHService(Config config) {
      this.config = config;
   }

   public void sendPayment(String payId, String amountToBeSent, String commitId )
   {

      // Recipient address
      // String recipientAddress = "0x7975D1f0769eEfB7F8d902658574E485bACF72A0";

      EthereumNetwork network = getEthNetwork( config.getEthNetwork().getEnvironment() );
      System.out.println("Using ETH Environment : " + network.getNetworkName());

      EthereumPayIdClient ethereumPayIdClient = new EthereumPayIdClient( network );

      final Optional<String> recipientAddress = getEthereumAddress(ethereumPayIdClient, payId);

      if( !recipientAddress.isPresent() )
      {
         System.out.println("Skipping the payment because payId is not valid");
         // TODO - throw an exception here.
         return;
      }

      System.out.println("Connecting to Ethereum ...");
      Web3j web3 = Web3j.build(new HttpService(config.getEthNetwork().getServer()));
      System.out.println("Successfully connected to Ethereum");

      try {
         Credentials credentials = getCredentials( network, config.getEthNetwork().getWalletSeed() );
         System.out.println("Account address: " + credentials.getAddress());
         System.out.println("Balance: " + Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER));

         // Get the latest nonce
         EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
         BigInteger nonce =  ethGetTransactionCount.getTransactionCount();

         // Value to transfer (in wei)
         BigInteger value = Convert.toWei( amountToBeSent, Convert.Unit.ETHER).toBigInteger();

         // Gas Parameters
         BigInteger gasLimit = BigInteger.valueOf(21000);
         BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();

         // Prepare the rawTransaction
         RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                 nonce,
                 gasPrice,
                 gasLimit,
                 recipientAddress.get(),
                 value );

         // Sign the transaction
         byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
         String hexValue = Numeric.toHexString(signedMessage);

         // Send transaction
         EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
         String transactionHash = ethSendTransaction.getTransactionHash();
         if( transactionHash == null )
         {
            System.out.println( ethSendTransaction.getError().getMessage() );
            // TODO - throw an exception and handle it.
            return;
         }
         System.out.println("transactionHash: " + transactionHash);

         // Wait for transaction to be mined
         Optional<TransactionReceipt> transactionReceipt;
         do {
            System.out.println("checking if transaction " + transactionHash + " is mined....");
            EthGetTransactionReceipt ethGetTransactionReceiptResp = web3.ethGetTransactionReceipt(transactionHash).send();
            transactionReceipt = ethGetTransactionReceiptResp.getTransactionReceipt();
            Thread.sleep(3000); // Wait 3 sec
         } while(!transactionReceipt.isPresent());

         System.out.println("Transaction " + transactionHash + " was mined in block # " + transactionReceipt.get().getBlockNumber());
         System.out.println("Balance: " + Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER));

      } catch (IOException | InterruptedException ex) {
         throw new RuntimeException(ex);
      }

   }

   private Optional<String> getEthereumAddress(EthereumPayIdClient ethereumPayIdClient, String payId)
   {
      Optional ethereumAddress = Optional.empty();
      try {
         ethereumAddress = Optional.of( ethereumPayIdClient.ethereumAddressForPayId(payId) );
         System.out.println( payId + " resolves to ethereum Address : " + ethereumAddress.get() );
      } catch (PayIdException e) {
         System.out.println("Invalid Pay Id : " + payId + " because of " + e.getMessage() );
      }

      return ethereumAddress;
   }

   private Credentials getCredentials(EthereumNetwork network, String mnemonic ) {
      String password = null; // no encryption
      Credentials credentials = null;

      if( network.equals( EthereumNetwork.ROPSTEN ) )
      {
         System.out.println("Using ROPSTEN Testnet Derivation path (m/44'/1'/0'/0) to generate credentials");
         // Ropsten Testnet (m/44'/1'/0'/0)
         int[] derivationPath = {44 | Bip32ECKeyPair.HARDENED_BIT, 1 | Bip32ECKeyPair.HARDENED_BIT, 0 | Bip32ECKeyPair.HARDENED_BIT, 0,0};

         // Generate a BIP32 master keypair from the mnemonic phrase
         Bip32ECKeyPair masterKeypair = Bip32ECKeyPair.generateKeyPair(MnemonicUtils.generateSeed(mnemonic, password));

         // Derived the key using the derivation path
         Bip32ECKeyPair  derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, derivationPath);

         // Load the wallet for the derived key
         credentials = Credentials.create(derivedKeyPair);

      }
      else
      {
         System.out.println("Using ETHEREUM MAINNET Derivation path (m/44'/60'/0'/1) to generate credentials");
         // Decrypt and open the wallet into a Credential object
         credentials = WalletUtils.loadBip39Credentials(password, mnemonic);
      }

      return credentials;
   }


   private EthereumNetwork getEthNetwork(ETHNetwork.Env env )
   {
      switch ( env )
      {
         case MAINNET:
            return EthereumNetwork.MAINNET;
         default:
            return EthereumNetwork.ROPSTEN;
      }
   }
}
