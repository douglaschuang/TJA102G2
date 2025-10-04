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

/**
 * 天氣控制器 - 透過 OpenWeather API 根據經緯度取得天氣資訊
 */
@RestController("memberWeatherController")
public class WeatherController {
	
	// 讀取 application.properties 中的 API 金鑰設定
	@Value("${openweather.api.key}")
	private String apiKey;

	/**
     * 取得天氣資訊的 API 端點
     * 
     * @param lat 使用者所在位置的緯度
     * @param lon 使用者所在位置的經度
     * @return 一個 Map，包含城市名稱、氣溫、天氣描述，或錯誤訊息
     */
	@GetMapping("/member/weather")
	public Map<String, Object> getWeather(@RequestParam double lat, @RequestParam double lon){
		
		Map<String, Object> result = new HashMap<>();
		
		try {
			// API Spec: https://openweathermap.org/current
			// 組合 API 請求網址（使用 metric 單位、繁體中文、帶入 API 金鑰）
			String urlString = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&lang=zh_tw&appid=%s",
                    lat, lon, apiKey);
			
			// 建立 URL 連線
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			// 使用 BufferedReader 讀取 API 回傳的 JSON 字串
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			in.close();
			
			// 使用 Gson 將 JSON 字串轉成 JsonObject 方便取值
			Gson gson = new Gson();
			JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
			
			// 從 JSON 中取得城市名稱、天氣描述、氣溫
			String city = json.get("name").getAsString();
			String description = json.getAsJsonArray("weather")
					.get(0).getAsJsonObject()
					.get("description").getAsString();
			double temp = json.getAsJsonObject("main").get("temp").getAsDouble();
			
			// 將取得的資料放入結果 Map 中
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
