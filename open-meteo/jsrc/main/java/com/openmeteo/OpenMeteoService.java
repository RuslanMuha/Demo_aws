package com.openmeteo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenMeteoService {

	private static final HttpClient client = HttpClient.newHttpClient();

	public static String getWeatherForecast() {
		String url = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET()
				.build();

		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				return response.body();
			} else {
				throw new IOException("Failed to fetch data, status code: " + response.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Error occurred while fetching weather forecast", e);
		}
	}

}
