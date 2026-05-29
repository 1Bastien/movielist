package com.efrei.movielist.app.service;

import com.efrei.movielist.app.data.entity.WatchlistEntry;
import com.efrei.movielist.app.data.entity.WatchlistEntry.Status;
import com.efrei.movielist.app.data.repository.WatchlistRepository;
import com.efrei.movielist.app.dto.MovieDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;

    public WatchlistService(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    public WatchlistEntry addEntry(String username, MovieDto movie, Status status, Integer rating, String comment) {
        if (watchlistRepository.existsByUsernameAndTmdbId(username, movie.getId())) {
            throw new IllegalStateException("Movie already in watchlist");
        }
        WatchlistEntry entry = new WatchlistEntry();
        entry.setUsername(username);
        entry.setTmdbId(movie.getId());
        entry.setTitle(movie.getTitle());
        entry.setPosterUrl(movie.getPosterUrl());
        entry.setStatus(status);
        entry.setAddedAt(LocalDateTime.now());

        if (status == Status.SEEN) {
            entry.setSeenAt(LocalDateTime.now());
            entry.setRating(rating);
            entry.setComment(comment);
        }
        return watchlistRepository.save(entry);
    }

    public WatchlistEntry markAsSeen(Long entryId, Integer rating, String comment) {
        WatchlistEntry entry = watchlistRepository.findById(entryId)
                .orElseThrow(() -> new NoSuchElementException("Entry not found: " + entryId));
        entry.setStatus(Status.SEEN);
        entry.setSeenAt(LocalDateTime.now());
        entry.setRating(rating);
        entry.setComment(comment);
        return watchlistRepository.save(entry);
    }

    public List<WatchlistEntry> getToWatch(String username) {
        return watchlistRepository.findByUsernameAndStatus(username, Status.TO_WATCH);
    }

    public List<WatchlistEntry> getSeen(String username, boolean sortByRating) {
        if (sortByRating) {
            return watchlistRepository.findByUsernameAndStatusOrderByRatingDesc(username, Status.SEEN);
        }
        return watchlistRepository.findByUsernameAndStatus(username, Status.SEEN);
    }

    public void remove(Long entryId) {
        if (!watchlistRepository.existsById(entryId)) {
            throw new NoSuchElementException("Entry not found: " + entryId);
        }
        watchlistRepository.deleteById(entryId);
    }

    public boolean isInWatchlist(String username, Long tmdbId) {
        return watchlistRepository.existsByUsernameAndTmdbId(username, tmdbId);
    }
}
