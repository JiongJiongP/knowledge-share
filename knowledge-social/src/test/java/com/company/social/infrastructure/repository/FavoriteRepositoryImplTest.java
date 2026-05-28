package com.company.social.infrastructure.repository;

import com.company.social.domain.model.Favorite;
import com.company.social.infrastructure.mapper.FavoriteMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteRepositoryImplTest {

    @Mock private FavoriteMapper mapper;
    @InjectMocks private FavoriteRepositoryImpl repo;

    @Test
    void shouldFindByUserAndContent() {
        Favorite f = new Favorite();
        when(mapper.selectOne(any())).thenReturn(f);

        Favorite result = repo.findByUserAndContent(1L, 2L);
        assertThat(result).isNotNull();
    }

    @Test
    void shouldInsertFavorite() {
        Favorite f = new Favorite();
        when(mapper.insert(any())).thenReturn(1);

        repo.insert(f);
        verify(mapper).insert(f);
    }

    @Test
    void shouldDeleteFavorite() {
        when(mapper.delete(any())).thenReturn(1);

        repo.delete(1L, 2L);
        verify(mapper).delete(any());
    }

    @Test
    void shouldExistReturnTrue() {
        Favorite f = new Favorite();
        when(mapper.selectOne(any())).thenReturn(f);

        assertThat(repo.exists(1L, 2L)).isTrue();
    }

    @Test
    void shouldExistReturnFalse() {
        when(mapper.selectOne(any())).thenReturn(null);

        assertThat(repo.exists(1L, 2L)).isFalse();
    }

    @Test
    void shouldCountByUser() {
        when(mapper.selectCount(any())).thenReturn(10L);

        long count = repo.countByUser(1L);
        assertThat(count).isEqualTo(10L);
    }
}
