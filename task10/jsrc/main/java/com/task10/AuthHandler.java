package com.task10;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class AuthHandler {

    private final AccessTokenCache accessTokenCache = new AccessTokenCache();

    public String getCurrentuser(APIGatewayProxyRequestEvent requestEvent) {
        String bearerToken = requestEvent.getHeaders().get("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }
        System.out.println("Bearer: "+ bearerToken);

        String token = bearerToken.replace("Bearer", "");
        System.out.println("Token:"+ token);

        return accessTokenCache.getEmail(token.trim());
    }
}
