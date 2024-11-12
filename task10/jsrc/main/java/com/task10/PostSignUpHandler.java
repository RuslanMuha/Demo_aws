package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

public class PostSignUpHandler extends CognitoSupport implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    public PostSignUpHandler(CognitoIdentityProviderClient cognitoClient) {
        super(cognitoClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            System.out.println("PostSignUpHandler: " + "start");
            System.out.println("body: " + requestEvent.getBody());
            SignUp signUp = SignUp.fromJson(requestEvent.getBody());
            System.out.println("mail: " + signUp.getEmail());


            String userId = cognitoSignUp(signUp)
                    .user().attributes().stream()
                    .filter(attr -> attr.name().equals("email"))
                    .map(AttributeType::value)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Sub not found."));

            System.out.println("userId: " + userId);
            String idToken = confirmSignUp(signUp)
                    .authenticationResult()
                    .idToken();
            System.out.println("idToken: " + idToken);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(String.format("{\"accessToken\": \"%s\"}", idToken));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(String.format("{\"error\": \"%s\"}", e.getMessage()));
        }
    }

}
