package com.company.social.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.social.domain.model.Favorite;
import com.company.social.domain.repository.FavoriteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional
    public void favorite(Long userId, Long contentId) {
        if (favoriteRepository.exists(userId, contentId)) {
            return; // idempotent
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setContentId(contentId);
        try {
            favoriteRepository.insert(f);
        } catch (DataIntegrityViolationException e) {
            // duplicate, ignore
        }
    }

    @Transactional
    public void unfavorite(Long userId, Long contentId) {
        favoriteRepository.delete(userId, contentId);
    }

    public boolean isFavorited(Long userId, Long contentId) {
        return favoriteRepository.exists(userId, contentId);
    }

    public PageResult<Favorite> listByUser(Long userId, int page, int size) {
        return PageResult.of(
                favoriteRepository.findByUser(userId, page, size),
                favoriteRepository.countByUser(userId),
                page, size
        );
    }
}
