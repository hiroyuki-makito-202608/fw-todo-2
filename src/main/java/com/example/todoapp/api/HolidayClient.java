package com.example.todoapp.api;

import java.time.LocalDate;
import java.time.Duration;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;
import java.net.http.HttpClient;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HolidayClient {
    private static final String HOLIDAY_API_URL = "https://holidays-jp.github.io/api/v1/date.json";
    private final RestClient restClient = createRestClient();

    private RestClient createRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.create().mutate()
                .requestFactory(requestFactory)
                .build();
    }

    public HolidayResult getHolidays(LocalDate from, LocalDate to) {
        Map<String, String> holidays;
        try {
            holidays = restClient.get()
                    .uri(HOLIDAY_API_URL)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {
                    });
        } catch (RestClientException exception) {
            return new HolidayResult(Collections.emptyMap(), true);
        }

        Map<String, String> filtered = holidays.entrySet().stream()
                .filter(entry -> {
                    LocalDate date = LocalDate.parse(entry.getKey());
                    return (from == null || !date.isBefore(from))
                            && (to == null || !date.isAfter(to));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (first, second) -> first, java.util.LinkedHashMap::new));
        return new HolidayResult(filtered, false);
    }

    public record HolidayResult(Map<String, String> holidays, boolean unavailable) {
    }
}
