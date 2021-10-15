package com.spring.board.common;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

//=== #181. Spring Scheduler(스프링스케줄러)3. 
//=== Spring Scheduler(스프링 스케줄러)를 사용한 email 발송하기 === 
//=== Google메일서버를 사용할 수 있도록 Google email 계정 및 암호 입력하기 ===

public class MySMTPAuthenticator extends Authenticator {

   @Override
   public PasswordAuthentication getPasswordAuthentication() {
      
      // Gmail의 경우 @gmail.com을 제외한 아이디만 입력한다.
      return new PasswordAuthentication("nosenada9846", "qwer1234$");
   }
   
}