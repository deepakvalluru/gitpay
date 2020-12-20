package com.deepak.gitpay.controller;

import com.deepak.gitpay.service.PayIdService;
import io.xpring.payid.PayIdException;
import io.xpring.xrpl.XrpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class XRPController {

   @Autowired
   PayIdService payIdService;

   @GetMapping("/payid")
   public void getAddresses( ) throws PayIdException, XrpException, IOException
   {
      payIdService.sendPayment();
   }



}
