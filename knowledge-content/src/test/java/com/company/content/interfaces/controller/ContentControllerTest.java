package com.company.content.interfaces.controller;

import com.company.common.exception.GlobalExceptionHandler;
import com.company.common.result.PageResult;
import com.company.content.application.dto.CreateContentRequest;
import com.company.content.application.service.ContentService;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.ContentType;
import com.company.content.domain.model.enums.PublishStatus;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ContentService contentService;

    @InjectMocks
    private ContentController contentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(contentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListContents() throws Exception {
        KnowledgeContent c = new KnowledgeContent();
        c.setId(1L);
        c.setTitle("测试内容");
        c.setStatus(PublishStatus.PUBLISHED);
        PageResult<KnowledgeContent> page = PageResult.of(List.of(c), 1L, 1, 10);
        when(contentService.listPublished(1, 10, "latest", null, null)).thenReturn(page);

        mockMvc.perform(get("/api/contents?page=1&size=10&sort=latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].title").value("测试内容"));
    }

    @Test
    void shouldCreateContent() throws Exception {
        CreateContentRequest req = new CreateContentRequest();
        req.setTitle("新内容");
        req.setBody("正文");
        req.setContentType(ContentType.MARKDOWN);

        KnowledgeContent created = new KnowledgeContent();
        created.setId(1L);
        created.setTitle("新内容");
        created.setStatus(PublishStatus.DRAFT);
        when(contentService.create(eq(1L), any(CreateContentRequest.class))).thenReturn(created);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldPublishContent() throws Exception {
        doNothing().when(contentService).publish(1L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/contents/1/publish")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDeleteContent() throws Exception {
        doNothing().when(contentService).softDelete(1L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(delete("/api/contents/1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
