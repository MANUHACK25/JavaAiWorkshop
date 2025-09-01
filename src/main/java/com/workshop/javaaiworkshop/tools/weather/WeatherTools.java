package com.workshop.javaaiworkshop.tools.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WeatherTools {

    private static final String BASE_URL = "https://api.weather.gov";
    private final RestClient restClient;

    // inyecta RestClient.Builder (configura bean en tu contexto si no existe)
    public WeatherTools(RestClient.Builder builder){
        this.restClient = builder
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/geo+json")
                .defaultHeader("User-Agent", "WeatherApiClient/1.0 (your@email.com)")
                .build();
    }

    // DTOs (estáticos para evitar confusiones de anidamiento)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Points(@JsonProperty("properties") Points.Props properties) {
        public static record Props(@JsonProperty("forecast") String forecast) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Forecast(@JsonProperty("properties") Forecast.Props properties) {
        public static record Props(@JsonProperty("periods") List<Period> periods) {}
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record Period(
                @JsonProperty("number") Integer number,
                @JsonProperty("name") String name,
                @JsonProperty("startTime") String startTime,
                @JsonProperty("endTime") String endTime,
                @JsonProperty("isDayTime") Boolean isDayTime,
                @JsonProperty("temperature") Integer temperature,
                @JsonProperty("temperatureUnit") String temperatureUnit,
                @JsonProperty("temperatureTrend") String temperatureTrend,
                @JsonProperty("probabilityOfPrecipitation") Map<String, Object> probabilityOfPrecipitation,
                @JsonProperty("windSpeed") String windSpeed,
                @JsonProperty("windDirection") String windDirection,
                @JsonProperty("icon") String icon,
                @JsonProperty("shortForecast") String shortForecast,
                @JsonProperty("detailedForecast") String detailedForecast
        ) {}
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record Alert(List<Feature> features) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static record Feature(@JsonProperty("properties") Feature.Properties properties) {
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static record Properties(
                        @JsonProperty("event") String event,
                        @JsonProperty("areaDesc") String areaDesc,
                        @JsonProperty("severity") String severity,
                        @JsonProperty("description") String description,
                        @JsonProperty("instruction") String instruction
                ) {}
            }
        }
    }

    // -------------------
    // Métodos expuestos como Tools (deben estar en esta clase)
    // -------------------

    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(
            @ToolParam(description = "Latitude") Double latitude,
            @ToolParam(description = "Longitude") Double longitude) {

        Points points = this.restClient.get()
                .uri("/points/{lat},{lon}", latitude, longitude)
                .retrieve()
                .body(Points.class);

        Forecast forecast = this.restClient.get()
                .uri(points.properties().forecast())
                .retrieve()
                .body(Forecast.class);

        String foreCastText = forecast.properties().periods()
                .stream()
                .map(p -> String.format(
                        "%s:\nTemperature: %s %s\nWind: %s %s\nForecast: %s\n\n",
                        p.name(),
                        p.temperature(),
                        p.temperatureUnit(),
                        p.windSpeed(),
                        p.windDirection(),
                        p.detailedForecast()
                ))
                .collect(Collectors.joining());

        return foreCastText;
    }

    @Tool(description = "Get weather alerts for a US state. Input is two-letter US state code (e.g. CA, NY)")
    public String getAlerts(@ToolParam(description = "Two-letter US state code (e.g. CA, NY)") String state){
        Forecast.Alert alert = this.restClient.get()
                .uri("/alerts/active/area/{state}", state)
                .retrieve()
                .body(Forecast.Alert.class);

        if (alert == null || alert.features() == null || alert.features().isEmpty()) {
            return "No active alerts for " + state;
        }

        return alert.features()
                .stream()
                .map(f -> {
                    var p = f.properties();
                    return String.format(
                            "Event: %s\nArea: %s\nSeverity: %s\nDescription: %s\nInstructions: %s\n\n",
                            p.event(), p.areaDesc(), p.severity(), p.description(), p.instruction()
                    );
                })
                .collect(Collectors.joining());
    }
}