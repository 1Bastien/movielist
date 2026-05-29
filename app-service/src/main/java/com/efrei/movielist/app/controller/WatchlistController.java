package com.efrei.movielist.app.controller;

import com.efrei.movielist.app.data.entity.WatchlistEntry;
import com.efrei.movielist.app.dto.MovieDto;
import com.efrei.movielist.app.dto.SeenUpdateDto;
import com.efrei.movielist.app.service.MovieService;
import com.efrei.movielist.app.service.WatchlistService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final MovieService movieService;

    public WatchlistController(WatchlistService watchlistService, MovieService movieService) {
        this.watchlistService = watchlistService;
        this.movieService = movieService;
    }

    @GetMapping
    public String watchlist(@AuthenticationPrincipal UserDetails principal, Model model) {
        List<WatchlistEntry> entries = watchlistService.getToWatch(principal.getUsername());
        model.addAttribute("entries", entries);
        return "watchlist";
    }

    @GetMapping("/seen")
    public String seen(@RequestParam(defaultValue = "false") boolean sort,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model) {
        List<WatchlistEntry> entries = watchlistService.getSeen(principal.getUsername(), sort);
        model.addAttribute("entries", entries);
        model.addAttribute("sort", sort);
        return "seen";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long tmdbId,
                      @RequestParam String status,
                      @RequestParam(required = false) Integer rating,
                      @RequestParam(required = false) String comment,
                      @AuthenticationPrincipal UserDetails principal) {
        MovieDto movie = movieService.getDetails(tmdbId);
        WatchlistEntry.Status entryStatus = WatchlistEntry.Status.valueOf(status);
        try {
            watchlistService.addEntry(principal.getUsername(), movie, entryStatus, rating, comment);
        } catch (IllegalStateException ignored) {
            // deja dans la liste
        }
        return "redirect:/search";
    }

    @PostMapping("/{id}/seen")
    public String markSeen(@PathVariable Long id, @ModelAttribute SeenUpdateDto dto) {
        watchlistService.markAsSeen(id, dto.getRating(), dto.getComment());
        return "redirect:/watchlist";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "watchlist") String from) {
        watchlistService.remove(id);
        String destination = "watchlist/seen".equals(from) ? "watchlist/seen" : "watchlist";
        return "redirect:/" + destination;
    }
}
