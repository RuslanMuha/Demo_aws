package com.task10;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.environment.ValueTransformer;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.Map;
@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${booking_userpool}")
@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = true,
		aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "tables_table", value = "${tables_table}"),
		@EnvironmentVariable(key = "reservations_table", value = "${reservations_table}"),
		@EnvironmentVariable(key = "COGNITO_ID", value = "${booking_userpool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
		@EnvironmentVariable(key = "CLIENT_ID", value = "${booking_userpool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID),
})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final CognitoIdentityProviderClient cognitoClient;
	private final Map<String, String> headersForCORS;

	private final Map<String, RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>> handlersByRouteKey;
	AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
			.withRegion(System.getenv("region")) // Set your region
			.build();

	public ApiHandler() {
		this.cognitoClient = initCognitoClient();
		this.headersForCORS = initHeadersForCORS();
		this.handlersByRouteKey = initHandlers();
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		String httpMethod = requestEvent.getHttpMethod();
		String path = requestEvent.getResource();
		String routeKey = String.format("%s%s", httpMethod, path);

		System.out.println("routeKey: " + routeKey);

		APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();

		var handler = handlersByRouteKey.get(routeKey);
		if (handler == null) {
			responseEvent.setStatusCode(404);
			responseEvent.setBody(String.format("Rout Not found %s", routeKey));
			return responseEvent;
		}

		return handler.handleRequest(requestEvent, context);
	}

	private Map<String, String> initHeadersForCORS() {
		return Map.of(
				"Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
				"Access-Control-Allow-Origin", "*",
				"Access-Control-Allow-Methods", "*",
				"Accept-Version", "*"
		);
	}

	private CognitoIdentityProviderClient initCognitoClient() {
		return CognitoIdentityProviderClient.builder()
				.region(Region.of(System.getenv("region")))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}

	private Map<String, RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>> initHandlers() {
		return Map.of(
				"POST/signup", new PostSignUpHandler(cognitoClient),
				"POST/signin", new PostSignInHandler(cognitoClient),
				"POST/tables", new PostTablesHandler(dynamoDB),
				"GET/tables", new GetTablesHandler(dynamoDB),
				"POST/reservations ", new PostReservationHandler(dynamoDB),
				"GET/reservations ", new GetReservationHandler(dynamoDB)
		);
	}
}
