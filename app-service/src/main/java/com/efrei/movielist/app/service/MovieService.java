package com.efrei.movielist.app.service;

import com.efrei.movielist.app.dto.MovieDto;
import com.efrei.movielist.app.dto.TmdbSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class MovieService {

    private final RestTemplate restTemplate;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    public MovieService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<MovieDto> search(String query) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("language", "fr-FR")
                .build()
                .toUriString();

        TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
        if (response == null || response.getResults() == null) return List.of();
        return response.getResults();
    }

    public MovieDto getDetails(Long tmdbId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/" + tmdbId)
                .queryParam("api_key", apiKey)
                .queryParam("language", "fr-FR")
                .build()
                .toUriString();

        return restTemplate.getForObject(url, MovieDto.class);
    }
}
