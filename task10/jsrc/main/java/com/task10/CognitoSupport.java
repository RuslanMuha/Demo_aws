package com.task10;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

public class CognitoSupport {
    private final String userPoolId = System.getenv("user_pool");

    private final CognitoIdentityProviderClient cognitoClient;

    protected CognitoSupport(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    protected AdminInitiateAuthResponse cognitoSignIn(String nickName, String password) {
        Map<String, String> authParams = Map.of(
                "USERNAME", nickName,
                "PASSWORD", password
        );

        return cognitoClient.adminInitiateAuth(AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(authParams)
                .build());
    }

    protected AdminCreateUserResponse cognitoSignUp(SignUp signUp) {

        return cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(signUp.getEmail())
                .temporaryPassword(signUp.getPassword())
                .userAttributes(
                        AttributeType.builder()
                                .name("firstName")
                                .value(signUp.getFirstName())
                                .build(),
                        AttributeType.builder()
                                .name("lastName")
                                .value(signUp.getLastName())
                                .build(),
                        AttributeType.builder()
                                .name("email")
                                .value(signUp.getEmail())
                                .build(),
                        AttributeType.builder()
                                .name("email_verified")
                                .value("true")
                                .build())
                .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .messageAction("SUPPRESS")
                .forceAliasCreation(Boolean.FALSE)
                .build()
        );
    }

}
