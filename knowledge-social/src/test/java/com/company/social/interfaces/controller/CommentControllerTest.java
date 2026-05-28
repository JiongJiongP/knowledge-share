package com.company.social.interfaces.controller;

import com.company.common.exception.GlobalExceptionHandler;
import com.company.social.application.dto.CreateCommentRequest;
import com.company.social.application.service.CommentService;
import com.company.social.domain.model.Comment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListComments() throws Exception {
        Comment c = new Comment();
        c.setId(1L);
        c.setBody("好文章");
        when(commentService.listByContentId(1L)).thenReturn(List.of(c));

        mockMvc.perform(get("/api/contents/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].body").value("好文章"));
    }

    @Test
    void shouldListReplies() throws Exception {
        when(commentService.listReplies(1L, 1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/comments/1/replies?contentId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldCreateComment() throws Exception {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setBody("评论内容");

        Comment created = new Comment();
        created.setId(1L);
        created.setBody("评论内容");
        when(commentService.create(eq(1L), eq(1L), eq("评论内容"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(created);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/contents/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldLikeComment() throws Exception {
        doNothing().when(commentService).like(1L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/comments/1/like")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldUnlikeComment() throws Exception {
        doNothing().when(commentService).unlike(1L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(delete("/api/comments/1/like")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDeleteComment() throws Exception {
        doNothing().when(commentService).delete(1L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(delete("/api/comments/1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
