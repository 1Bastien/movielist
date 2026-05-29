package com.efrei.movielist.app.service;

import com.efrei.movielist.app.data.entity.WatchlistEntry;
import com.efrei.movielist.app.data.entity.WatchlistEntry.Status;
import com.efrei.movielist.app.data.repository.WatchlistRepository;
import com.efrei.movielist.app.dto.MovieDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock WatchlistRepository watchlistRepository;
    @InjectMocks WatchlistService watchlistService;

    private static final String USERNAME = "alice";
    private MovieDto movie;

    @BeforeEach
    void setUp() {
        movie = new MovieDto();
        movie.setId(550L);
        movie.setTitle("Fight Club");
        movie.setPosterPath("/poster.jpg");
    }

    @Test
    void addEntry_toWatch_savesEntry() {
        when(watchlistRepository.existsByUsernameAndTmdbId(USERNAME, 550L)).thenReturn(false);
        when(watchlistRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WatchlistEntry entry = watchlistService.addEntry(USERNAME, movie, Status.TO_WATCH, null, null);

        assertThat(entry.getUsername()).isEqualTo(USERNAME);
        assertThat(entry.getStatus()).isEqualTo(Status.TO_WATCH);
        assertThat(entry.getRating()).isNull();
        verify(watchlistRepository).save(any());
    }

    @Test
    void addEntry_seen_setsRatingAndSeenAt() {
        when(watchlistRepository.existsByUsernameAndTmdbId(USERNAME, 550L)).thenReturn(false);
        when(watchlistRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WatchlistEntry entry = watchlistService.addEntry(USERNAME, movie, Status.SEEN, 4, "Super film");

        assertThat(entry.getStatus()).isEqualTo(Status.SEEN);
        assertThat(entry.getRating()).isEqualTo(4);
        assertThat(entry.getComment()).isEqualTo("Super film");
        assertThat(entry.getSeenAt()).isNotNull();
    }

    @Test
    void addEntry_duplicate_throwsException() {
        when(watchlistRepository.existsByUsernameAndTmdbId(USERNAME, 550L)).thenReturn(true);

        assertThatThrownBy(() -> watchlistService.addEntry(USERNAME, movie, Status.TO_WATCH, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markAsSeen_updatesEntry() {
        WatchlistEntry existing = new WatchlistEntry();
        existing.setId(1L);
        existing.setStatus(Status.TO_WATCH);
        when(watchlistRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(watchlistRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WatchlistEntry updated = watchlistService.markAsSeen(1L, 5, "Excellent");

        assertThat(updated.getStatus()).isEqualTo(Status.SEEN);
        assertThat(updated.getRating()).isEqualTo(5);
        assertThat(updated.getSeenAt()).isNotNull();
    }

    @Test
    void markAsSeen_withoutRating_statusIsSeen() {
        WatchlistEntry existing = new WatchlistEntry();
        existing.setId(2L);
        existing.setStatus(Status.TO_WATCH);
        when(watchlistRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(watchlistRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WatchlistEntry updated = watchlistService.markAsSeen(2L, null, null);

        assertThat(updated.getStatus()).isEqualTo(Status.SEEN);
        assertThat(updated.getRating()).isNull();
    }

    @Test
    void markAsSeen_notFound_throwsException() {
        when(watchlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.markAsSeen(99L, 3, null))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getSeen_sortByRating_callsOrderedQuery() {
        watchlistService.getSeen(USERNAME, true);
        verify(watchlistRepository).findByUsernameAndStatusOrderByRatingDesc(USERNAME, Status.SEEN);
    }

    @Test
    void getSeen_noSort_callsDefaultQuery() {
        watchlistService.getSeen(USERNAME, false);
        verify(watchlistRepository).findByUsernameAndStatus(USERNAME, Status.SEEN);
    }

    @Test
    void getToWatch_returnsEntries() {
        List<WatchlistEntry> expected = List.of(new WatchlistEntry());
        when(watchlistRepository.findByUsernameAndStatus(USERNAME, Status.TO_WATCH)).thenReturn(expected);

        assertThat(watchlistService.getToWatch(USERNAME)).isEqualTo(expected);
    }

    @Test
    void remove_existing_deletesIt() {
        when(watchlistRepository.existsById(1L)).thenReturn(true);
        watchlistService.remove(1L);
        verify(watchlistRepository).deleteById(1L);
    }

    @Test
    void remove_notFound_throwsException() {
        when(watchlistRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> watchlistService.remove(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void isInWatchlist_delegatesToRepository() {
        when(watchlistRepository.existsByUsernameAndTmdbId(USERNAME, 550L)).thenReturn(true);
        assertThat(watchlistService.isInWatchlist(USERNAME, 550L)).isTrue();
    }
}
