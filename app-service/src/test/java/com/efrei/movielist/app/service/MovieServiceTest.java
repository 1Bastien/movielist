package com.efrei.movielist.app.service;

import com.efrei.movielist.app.dto.MovieDto;
import com.efrei.movielist.app.dto.TmdbSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock RestTemplate restTemplate;
    private MovieService movieService;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(restTemplate);
        ReflectionTestUtils.setField(movieService, "apiKey", "test-key");
        ReflectionTestUtils.setField(movieService, "baseUrl", "http://localhost:9999");
    }

    @Test
    void search_returnsResults() {
        MovieDto dto = new MovieDto();
        dto.setId(1L);
        dto.setTitle("Inception");
        TmdbSearchResponse response = new TmdbSearchResponse();
        response.setResults(List.of(dto));
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class))).thenReturn(response);

        List<MovieDto> results = movieService.search("Inception");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    void search_nullResponse_returnsEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class))).thenReturn(null);
        assertThat(movieService.search("anything")).isEmpty();
    }

    @Test
    void search_nullResults_returnsEmptyList() {
        TmdbSearchResponse response = new TmdbSearchResponse();
        response.setResults(null);
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class))).thenReturn(response);
        assertThat(movieService.search("anything")).isEmpty();
    }

    @Test
    void search_urlContainsQueryAndApiKey() {
        TmdbSearchResponse response = new TmdbSearchResponse();
        response.setResults(List.of());
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class))).thenReturn(response);

        movieService.search("matrix");

        verify(restTemplate).getForObject(
                argThat((String url) -> url.contains("query=matrix") && url.contains("api_key=test-key")),
                eq(TmdbSearchResponse.class)
        );
    }

    @Test
    void getDetails_returnsMovie() {
        MovieDto dto = new MovieDto();
        dto.setId(550L);
        dto.setTitle("Fight Club");
        when(restTemplate.getForObject(anyString(), eq(MovieDto.class))).thenReturn(dto);

        assertThat(movieService.getDetails(550L).getTitle()).isEqualTo("Fight Club");
    }
}
