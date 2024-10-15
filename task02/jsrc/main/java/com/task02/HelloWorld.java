package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.Map;
import java.util.function.Function;

@LambdaHandler(
        lambdaName = "hello_world",
        roleName = "hello_world-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final int SC_OK = 200;
    private static final int SC_NOT_FOUND = 404;

    private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");
    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        String method = getMethod(requestEvent);
        String path = getPath(requestEvent);

        if (path.trim().equals("/hello") && method.trim().equals("GET")) {
            return handleGetHello();
        }
        return notFoundResponse(requestEvent);
    }

    private APIGatewayV2HTTPResponse handleGetHello() {
        try {
            return buildResponse(SC_OK, Body.ok("Hello from Lambda"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private APIGatewayV2HTTPResponse notFoundResponse(APIGatewayV2HTTPEvent requestEvent) {
        try {
            return buildResponse(SC_NOT_FOUND, Body.notFound(
                    String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s",
                            getPath(requestEvent),
                            getMethod(requestEvent)
                    )));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private APIGatewayV2HTTPResponse buildResponse(int statusCode, Object body) throws JsonProcessingException {
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(statusCode)
                .withHeaders(responseHeaders)
                .withBody(mapper.writeValueAsString(body))
                .build();
    }

    private String getMethod(APIGatewayV2HTTPEvent requestEvent) {
        return requestEvent.getRequestContext().getHttp().getMethod();
    }

    private String getPath(APIGatewayV2HTTPEvent requestEvent) {
        return requestEvent.getRequestContext().getHttp().getPath();
    }

}
