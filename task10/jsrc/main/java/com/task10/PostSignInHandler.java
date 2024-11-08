package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

public class PostSignInHandler extends CognitoSupport implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final AccessTokenCache tokenCache = new AccessTokenCache();

    protected PostSignInHandler(CognitoIdentityProviderClient cognitoClient) {
        super(cognitoClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            SignIn signIn = SignIn.fromJson(requestEvent.getBody());

            String accessToken = cognitoSignIn(signIn.getEmail(), signIn.getPassword())
                    .authenticationResult()
                    .idToken();

            System.out.println("accessToken: " + accessToken);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new ObjectMapper().writeValueAsString(new AccessToken(accessToken)));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(String.format("{\"error\": \"%s\"}", e.getMessage()));
        }
    }
}
