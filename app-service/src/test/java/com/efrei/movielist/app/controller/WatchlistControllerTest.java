package com.efrei.movielist.app.controller;

import com.efrei.movielist.app.client.AuthServiceClient;
import com.efrei.movielist.app.data.entity.WatchlistEntry;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WatchlistController.class)
class WatchlistControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean WatchlistService watchlistService;
    @MockBean MovieService movieService;
    @MockBean AuthServiceClient authServiceClient;

    @Test
    @WithMockUser(username = "alice")
    void watchlist_returnsToWatchList() throws Exception {
        when(watchlistService.getToWatch("alice")).thenReturn(List.of());

        mockMvc.perform(get("/watchlist"))
                .andExpect(status().isOk())
                .andExpect(view().name("watchlist"))
                .andExpect(model().attributeExists("entries"));
    }

    @Test
    @WithMockUser(username = "alice")
    void seen_withSort_passesCorrectParam() throws Exception {
        when(watchlistService.getSeen("alice", true)).thenReturn(List.of());

        mockMvc.perform(get("/watchlist/seen").param("sort", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("seen"))
                .andExpect(model().attribute("sort", true));

        verify(watchlistService).getSeen("alice", true);
    }

    @Test
    @WithMockUser(username = "alice")
    void add_toWatch_redirectsToSearch() throws Exception {
        MovieDto movie = new MovieDto();
        movie.setId(550L);
        movie.setTitle("Fight Club");
        when(movieService.getDetails(550L)).thenReturn(movie);

        mockMvc.perform(post("/watchlist/add")
                        .param("tmdbId", "550")
                        .param("status", "TO_WATCH")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/search"));
    }

    @Test
    @WithMockUser(username = "alice")
    void markSeen_redirectsToWatchlist() throws Exception {
        WatchlistEntry entry = new WatchlistEntry();
        entry.setId(1L);
        when(watchlistService.markAsSeen(eq(1L), eq(4), eq("Good"))).thenReturn(entry);

        mockMvc.perform(post("/watchlist/1/seen")
                        .param("rating", "4")
                        .param("comment", "Good")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/watchlist"));
    }

    @Test
    @WithMockUser(username = "alice")
    void delete_redirectsToFrom() throws Exception {
        mockMvc.perform(post("/watchlist/1/delete")
                        .param("from", "watchlist")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/watchlist"));

        verify(watchlistService).remove(1L);
    }
}
