package com.babymate.member.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class WeatherController {
	
	@Value("${openweather.api.key}")
	private String apiKey;

	@GetMapping("/member/weather")
	public Map<String, Object> getWeather(@RequestParam double lat, @RequestParam double lon){
		Map<String, Object> result = new HashMap<>();
		try {
			String urlString = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&lang=zh_tw&appid=%s",
                    lat, lon, apiKey);
			
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			in.close();
			
			Gson gson = new Gson();
			JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
			
			String city = json.get("name").getAsString();
			String description = json.getAsJsonArray("weather")
					.get(0).getAsJsonObject()
					.get("description").getAsString();
			double temp = json.getAsJsonObject("main").get("temp").getAsDouble();
			
			result.put("city", city);
			result.put("temp", temp);
			result.put("description", description);
			
		} catch (Exception e) {
			 e.printStackTrace();
			 result.put("error", e.getMessage());
		}
		return result;
		}
	}
