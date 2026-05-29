package com.efrei.movielist.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbSearchResponse {
    private List<MovieDto> results;

    public List<MovieDto> getResults() { return results; }
    public void setResults(List<MovieDto> results) { this.results = results; }
}
