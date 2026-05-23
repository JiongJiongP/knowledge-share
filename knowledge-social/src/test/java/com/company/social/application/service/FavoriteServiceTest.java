package com.company.social.application.service;

import com.company.common.result.PageResult;
import com.company.social.domain.model.Favorite;
import com.company.social.domain.repository.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    void shouldFavorite() {
        when(favoriteRepository.exists(1L, 100L)).thenReturn(false);

        favoriteService.favorite(1L, 100L);

        verify(favoriteRepository).insert(any());
    }

    @Test
    void shouldNotDuplicateFavorite() {
        when(favoriteRepository.exists(1L, 100L)).thenReturn(true);

        favoriteService.favorite(1L, 100L);

        verify(favoriteRepository, never()).insert(any());
    }

    @Test
    void shouldUnfavorite() {
        favoriteService.unfavorite(1L, 100L);
        verify(favoriteRepository).delete(1L, 100L);
    }

    @Test
    void shouldCheckFavorite() {
        when(favoriteRepository.exists(1L, 100L)).thenReturn(true);
        assertThat(favoriteService.isFavorited(1L, 100L)).isTrue();
    }

    @Test
    void shouldListFavorites() {
        Favorite f = new Favorite();
        f.setId(1L); f.setUserId(1L); f.setContentId(100L);
        when(favoriteRepository.findByUser(1L, 1, 10)).thenReturn(List.of(f));
        when(favoriteRepository.countByUser(1L)).thenReturn(1L);

        PageResult<Favorite> result = favoriteService.listByUser(1L, 1, 10);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
    }
}
