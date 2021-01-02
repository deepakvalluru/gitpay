package com.deepak.gitpay.controller;

import com.deepak.gitpay.service.ETHService;
import com.deepak.gitpay.service.PayIdService;
import io.xpring.payid.PayIdException;
import io.xpring.xrpl.XrpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class PayIdController {

   @Autowired
   PayIdService payIdService;

   @Autowired
   ETHService ethService;

   @GetMapping("/payid")
   public void pay( ) throws PayIdException, XrpException, IOException
   {
      payIdService.sendPayment();
   }

   @GetMapping("/ether")
   public void ethAddress()
   {
      ethService.sendPayment("deepak$payme.plus", "0.03", "emptyCommitId");
   }

}
