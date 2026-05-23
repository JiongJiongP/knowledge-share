package com.company.social.domain.repository;

import com.company.social.domain.model.Favorite;

import java.util.List;

public interface FavoriteRepository {
    Favorite findByUserAndContent(Long userId, Long contentId);
    List<Favorite> findByUser(Long userId, int page, int size);
    long countByUser(Long userId);
    void insert(Favorite favorite);
    void delete(Long userId, Long contentId);
    boolean exists(Long userId, Long contentId);
}
