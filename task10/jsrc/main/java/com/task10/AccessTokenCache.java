package com.task10;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccessTokenCache {

   private static final Map<String, String> tokenMap =  new ConcurrentHashMap<>();


   public void putToken(String email, String accessToken) {
      tokenMap.put(accessToken, email);
   }

   public String getEmail( String accessToken) {
      return tokenMap.get(accessToken);
   }

}
