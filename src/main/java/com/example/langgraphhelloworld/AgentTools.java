package com.example.langgraphhelloworld;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Tools the agent can call. The weather data is fake so the example runs
 * without another API key.
 */
@Component
public class AgentTools {

    private static final Map<String, String> WEATHER = Map.of(
            "chennai", "33°C, humid, light breeze",
            "london", "12°C, cloudy, chance of rain",
            "new york", "18°C, sunny");

    @Tool(description = "Get the current weather for a city")
    public String getWeather(@ToolParam(description = "City name, for example Chennai") String city) {
        return WEATHER.getOrDefault(city.toLowerCase(), "No weather data for " + city);
    }

    @Tool(description = "Get the current date and time in a time zone")
    public String getCurrentTime(@ToolParam(description = "IANA time zone, for example Asia/Kolkata") String timeZone) {
        return ZonedDateTime.now(ZoneId.of(timeZone)).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}
