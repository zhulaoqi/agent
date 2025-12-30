package com.kinch.agent.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 天气查询工具
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
public class WeatherTool implements Function<String, String> {

    private final Gson gson = new Gson();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${weather.api.enabled:false}")
    private boolean weatherApiEnabled;

    @Value("${weather.api.key:}")
    private String weatherApiKey;

    @Value("${weather.api.url:https://restapi.amap.com/v3/weather/weatherInfo}")
    private String weatherApiUrl;

    @Override
    public String apply(String city) {

        log.info("查询天气 - 城市: {}", city);

        try {
            if (weatherApiEnabled && weatherApiKey != null && !weatherApiKey.isEmpty()) {
                return getWeatherFromAmap(city);
            } else {
                return getMockWeather(city);
            }
        } catch (Exception e) {
            log.error("天气查询失败", e);
            return getMockWeather(city);
        }
    }

    private String getWeatherFromAmap(String city) {
        try {
            String url = String.format("%s?key=%s&city=%s&extensions=base",
                    weatherApiUrl, weatherApiKey, city);
            
            String response = restTemplate.getForObject(url, String.class);
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            
            if ("1".equals(jsonResponse.get("status").getAsString())) {
                JsonObject lives = jsonResponse.getAsJsonArray("lives").get(0).getAsJsonObject();
                
                Map<String, Object> weather = new HashMap<>();
                weather.put("city", lives.get("city").getAsString());
                weather.put("temperature", lives.get("temperature").getAsString() + "°C");
                weather.put("weather", lives.get("weather").getAsString());
                weather.put("winddirection", lives.get("winddirection").getAsString());
                weather.put("windpower", lives.get("windpower").getAsString() + "级");
                weather.put("humidity", lives.get("humidity").getAsString() + "%");
                weather.put("reporttime", lives.get("reporttime").getAsString());
                weather.put("source", "高德地图");
                
                return gson.toJson(weather);
            } else {
                log.warn("高德天气API返回失败，使用模拟数据");
                return getMockWeather(city);
            }
        } catch (Exception e) {
            log.error("调用高德天气API失败", e);
            return getMockWeather(city);
        }
    }

    private String getMockWeather(String city) {
        Map<String, Object> weather = new HashMap<>();
        weather.put("city", city);
        weather.put("temperature", getRandomTemperature());
        weather.put("weather", getRandomWeather());
        weather.put("humidity", getRandomHumidity());
        weather.put("windSpeed", getRandomWindSpeed());
        weather.put("aqi", getRandomAqi());
        weather.put("tips", getWeatherTips(city));
        weather.put("source", "模拟数据");
        return gson.toJson(weather);
    }

    private String getRandomTemperature() {
        int temp = (int) (Math.random() * 20) + 10;
        return temp + "°C";
    }

    private String getRandomWeather() {
        String[] weathers = {"晴", "多云", "阴", "小雨", "大雨"};
        return weathers[(int) (Math.random() * weathers.length)];
    }

    private String getRandomHumidity() {
        int humidity = (int) (Math.random() * 40) + 40;
        return humidity + "%";
    }

    private String getRandomWindSpeed() {
        int wind = (int) (Math.random() * 5) + 1;
        return wind + "级";
    }

    private int getRandomAqi() {
        return (int) (Math.random() * 200) + 50;
    }

    private String getWeatherTips(String city) {
        return String.format("%s天气适宜，建议适当户外活动", city);
    }
}

