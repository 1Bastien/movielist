package com.efrei.movielist.app.controller;

import com.efrei.movielist.app.client.AuthServiceClient;
import com.efrei.movielist.app.dto.MovieDto;
import com.efrei.movielist.app.service.MovieService;
import com.efrei.movielist.app.service.WatchlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean MovieService movieService;
    @MockBean WatchlistService watchlistService;
    @MockBean AuthServiceClient authServiceClient;

    @Test
    @WithMockUser(username = "alice")
    void search_withQuery_returnsResultsInModel() throws Exception {
        MovieDto dto = new MovieDto();
        dto.setId(1L);
        dto.setTitle("Inception");
        when(movieService.search("Inception")).thenReturn(List.of(dto));
        when(watchlistService.isInWatchlist(eq("alice"), any())).thenReturn(false);

        mockMvc.perform(get("/search").param("q", "Inception"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("results"))
                .andExpect(model().attribute("query", "Inception"));
    }

    @Test
    @WithMockUser
    void search_withoutQuery_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeDoesNotExist("results"));
    }

    @Test
    void search_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/search").param("q", "test"))
                .andExpect(status().is3xxRedirection());
    }
}
