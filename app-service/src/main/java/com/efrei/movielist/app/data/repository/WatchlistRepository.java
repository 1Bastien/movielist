package com.efrei.movielist.app.data.repository;

import com.efrei.movielist.app.data.entity.WatchlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<WatchlistEntry, Long> {
    List<WatchlistEntry> findByUsernameAndStatus(String username, WatchlistEntry.Status status);
    List<WatchlistEntry> findByUsernameAndStatusOrderByRatingDesc(String username, WatchlistEntry.Status status);
    Optional<WatchlistEntry> findByUsernameAndTmdbId(String username, Long tmdbId);
    boolean existsByUsernameAndTmdbId(String username, Long tmdbId);
}
