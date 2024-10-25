package com.task05;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task05.config.DynamoDbClient;
import com.task05.model.Event;
import com.task05.model.EventRequestDto;
import com.task05.model.SaveEventResponse;
import com.task05.service.EventRepository;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)

public class ApiHandler implements RequestHandler<EventRequestDto, SaveEventResponse> {

	public SaveEventResponse handleRequest(EventRequestDto request, Context context) {

		System.out.println("request: "+request);
		DynamoDbClient dbClient = new DynamoDbClient();

		EventRepository eventRepository = new EventRepository(dbClient);


		Event event = new Event();
		event.setPrincipalId(request.getPrincipalId());
		event.setBody(request.getContent());

		eventRepository.saveEvent(event);
		System.out.println("event: " +event);
		return new SaveEventResponse(201, event);
	}
}
