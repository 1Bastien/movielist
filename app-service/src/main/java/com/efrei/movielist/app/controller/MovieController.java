package com.efrei.movielist.app.controller;

import com.efrei.movielist.app.dto.MovieDto;
import com.efrei.movielist.app.service.MovieService;
import com.efrei.movielist.app.service.WatchlistService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class MovieController {

    private final MovieService movieService;
    private final WatchlistService watchlistService;

    public MovieController(MovieService movieService, WatchlistService watchlistService) {
        this.movieService = movieService;
        this.watchlistService = watchlistService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/search";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        if (q != null && !q.isBlank()) {
            List<MovieDto> results = movieService.search(q);
            Set<Long> alreadyAdded = principal != null
                    ? results.stream()
                            .map(MovieDto::getId)
                            .filter(id -> watchlistService.isInWatchlist(principal.getUsername(), id))
                            .collect(Collectors.toSet())
                    : Set.of();
            model.addAttribute("results", results);
            model.addAttribute("alreadyAdded", alreadyAdded);
            model.addAttribute("query", q);
        }
        return "search";
    }
}
