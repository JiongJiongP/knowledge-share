package com.company.social.interfaces.controller;

import com.company.common.result.PageResult;
import com.company.common.result.Result;
import com.company.social.application.service.FavoriteService;
import com.company.social.domain.model.Favorite;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public Result<PageResult<Favorite>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(favoriteService.listByUser(userId, page, size));
    }

    @PostMapping("/{contentId}")
    public Result<Void> favorite(@PathVariable Long contentId, Authentication auth) {
        favoriteService.favorite((Long) auth.getPrincipal(), contentId);
        return Result.ok(null);
    }

    @DeleteMapping("/{contentId}")
    public Result<Void> unfavorite(@PathVariable Long contentId, Authentication auth) {
        favoriteService.unfavorite((Long) auth.getPrincipal(), contentId);
        return Result.ok(null);
    }

    @GetMapping("/check/{contentId}")
    public Result<Map<String, Boolean>> check(@PathVariable Long contentId, Authentication auth) {
        boolean favorited = favoriteService.isFavorited((Long) auth.getPrincipal(), contentId);
        return Result.ok(Map.of("favorited", favorited));
    }
}
