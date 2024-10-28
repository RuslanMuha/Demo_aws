package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@LambdaHandler(
    lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
@RuleEventSource(targetRule = "uuid_trigger")
public class UuidGenerator implements RequestHandler<Object, Map<String, Object>> {

	private static final S3Client s3 = S3Client.builder()
			.region(Region.EU_CENTRAL_1)  // Change region if needed
			.build();

	public Map<String, Object> handleRequest(Object request, Context context) {

		String bucketName = System.getenv("target_bucket");
		System.out.println("bucket name: " + bucketName);

		if (bucketName == null || bucketName.isEmpty()) {
			throw new IllegalArgumentException("Environment variable 'uuid-storage' is not set or empty.");
		}

		// Delete all files in the bucket
		deleteAllFilesInBucket(bucketName);

		// Generate file name and content
		String fileName = Instant.now().toString(); // ISO 8601 format
		String fileContent = generateFileContent();

		// Upload file to S3 bucket
		uploadFileToS3(bucketName, fileName, fileContent);

		return Map.of("status", 200);
	}

	private static void deleteAllFilesInBucket(String bucketName) {
		ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
				.bucket(bucketName)
				.build();

		ListObjectsV2Response listResponse;
		do {
			listResponse = s3.listObjectsV2(listRequest);
			for (S3Object s3Object : listResponse.contents()) {
				s3.deleteObject(DeleteObjectRequest.builder()
						.bucket(bucketName)
						.key(s3Object.key())
						.build());
			}
			listRequest = listRequest.toBuilder()
					.continuationToken(listResponse.nextContinuationToken())
					.build();
		} while (listResponse.isTruncated());
	}

	private static String generateFileContent() {
		List<String> uniqueIds = IntStream.range(0, 10)
				.mapToObj(i -> UUID.randomUUID().toString())
				.collect(Collectors.toList());

		Map<String, List<String>> data = new HashMap<>();
		data.put("ids", uniqueIds);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(data);
		} catch (Exception e) {
			throw new RuntimeException("Error generating file content", e);
		}
	}

	private static void uploadFileToS3(String bucketName, String fileName, String fileContent) {
		try (InputStream fileStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8))) {
			PutObjectRequest putRequest = PutObjectRequest.builder()
					.bucket(bucketName)
					.key(fileName)
					.build();

			s3.putObject(putRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(fileStream, fileContent.length()));
			System.out.println("File uploaded successfully: " + fileName);
		} catch (Exception e) {
			throw new RuntimeException("Error uploading file to S3", e);
		}
	}
}
