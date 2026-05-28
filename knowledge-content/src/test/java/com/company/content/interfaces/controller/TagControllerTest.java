package com.company.content.interfaces.controller;

import com.company.common.exception.GlobalExceptionHandler;
import com.company.content.application.dto.CreateTagRequest;
import com.company.content.application.dto.TagContentRequest;
import com.company.content.application.service.TagService;
import com.company.content.domain.model.Tag;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tagController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListAllTags() throws Exception {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Java");
        when(tagService.listAll()).thenReturn(List.of(tag));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Java"));
    }

    @Test
    void shouldGetContentTags() throws Exception {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Spring");
        when(tagService.listByContentId(1L)).thenReturn(List.of(tag));

        mockMvc.perform(get("/api/contents/1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Spring"));
    }

    @Test
    void shouldCreateTag() throws Exception {
        CreateTagRequest req = new CreateTagRequest();
        req.setName("新标签");
        req.setColor("#FF0000");

        Tag created = new Tag();
        created.setId(1L);
        created.setName("新标签");
        when(tagService.create("新标签", "#FF0000", 1L)).thenReturn(created);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/admin/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldUpdateTag() throws Exception {
        CreateTagRequest req = new CreateTagRequest();
        req.setName("更新标签");
        req.setColor("#00FF00");

        Tag updated = new Tag();
        updated.setId(1L);
        updated.setName("更新标签");
        when(tagService.update(1L, "更新标签", "#00FF00")).thenReturn(updated);

        mockMvc.perform(put("/api/admin/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDeleteTag() throws Exception {
        doNothing().when(tagService).delete(1L);

        mockMvc.perform(delete("/api/admin/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldSetContentTags() throws Exception {
        TagContentRequest req = new TagContentRequest();
        req.setTagIds(List.of(1L, 2L));

        doNothing().when(tagService).setContentTags(1L, List.of(1L, 2L), 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(put("/api/contents/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldSetContentTagsWithNullTagIds() throws Exception {
        TagContentRequest req = new TagContentRequest();

        doNothing().when(tagService).setContentTags(1L, List.of(), 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(put("/api/contents/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
