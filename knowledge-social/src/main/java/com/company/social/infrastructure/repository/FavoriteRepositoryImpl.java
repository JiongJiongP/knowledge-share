package com.company.social.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.social.domain.model.Favorite;
import com.company.social.domain.repository.FavoriteRepository;
import com.company.social.infrastructure.mapper.FavoriteMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FavoriteRepositoryImpl implements FavoriteRepository {

    private final FavoriteMapper mapper;

    public FavoriteRepositoryImpl(FavoriteMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Favorite findByUserAndContent(Long userId, Long contentId) {
        return mapper.selectOne(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getContentId, contentId)
        );
    }

    @Override
    public List<Favorite> findByUser(Long userId, int page, int size) {
        return mapper.selectPage(
            new Page<>(page, size, false),
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt)
        ).getRecords();
    }

    @Override
    public long countByUser(Long userId) {
        return mapper.selectCount(
            new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId)
        );
    }

    @Override
    public void insert(Favorite favorite) {
        mapper.insert(favorite);
    }

    @Override
    public void delete(Long userId, Long contentId) {
        mapper.delete(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getContentId, contentId)
        );
    }

    @Override
    public boolean exists(Long userId, Long contentId) {
        return findByUserAndContent(userId, contentId) != null;
    }
}
