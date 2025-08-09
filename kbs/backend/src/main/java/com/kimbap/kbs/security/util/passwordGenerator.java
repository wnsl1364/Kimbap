package com.kimbap.kbs.security.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class passwordGenerator {
  public static void main(String[] args) {
      BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      String rawPassword = "a1";
      String encodedPassword = encoder.encode(rawPassword);
      System.out.println("암호화된 비밀번호: " + encodedPassword);
  }
}
