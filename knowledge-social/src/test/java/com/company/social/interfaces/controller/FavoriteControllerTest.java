package com.company.social.interfaces.controller;

import com.company.common.exception.GlobalExceptionHandler;
import com.company.common.result.PageResult;
import com.company.social.application.service.FavoriteService;
import com.company.social.domain.model.Favorite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private FavoriteController favoriteController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(favoriteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListFavorites() throws Exception {
        Favorite f = new Favorite();
        f.setId(1L);
        f.setContentId(10L);
        PageResult<Favorite> page = PageResult.of(List.of(f), 1L, 1, 10);
        when(favoriteService.listByUser(1L, 1, 10)).thenReturn(page);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(get("/api/favorites?page=1&size=10")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].contentId").value(10));
    }

    @Test
    void shouldFavoriteContent() throws Exception {
        doNothing().when(favoriteService).favorite(1L, 10L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/favorites/10")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldUnfavoriteContent() throws Exception {
        doNothing().when(favoriteService).unfavorite(1L, 10L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(delete("/api/favorites/10")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldCheckFavorite() throws Exception {
        when(favoriteService.isFavorited(1L, 10L)).thenReturn(true);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(get("/api/favorites/check/10")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.favorited").value(true));
    }
}
