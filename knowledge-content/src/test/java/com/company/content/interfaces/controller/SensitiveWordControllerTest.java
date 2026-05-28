package com.company.content.interfaces.controller;

import com.company.common.exception.GlobalExceptionHandler;
import com.company.content.application.dto.SensitiveWordBatchRequest;
import com.company.content.application.dto.SensitiveWordCheckRequest;
import com.company.content.application.dto.SensitiveWordRequest;
import com.company.content.application.service.SensitiveWordService;
import com.company.content.domain.model.SensitiveWord;
import com.company.content.domain.service.AhoCorasickAutomaton;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SensitiveWordControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SensitiveWordService sensitiveWordService;

    @InjectMocks
    private SensitiveWordController sensitiveWordController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sensitiveWordController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListAll() throws Exception {
        SensitiveWord sw = new SensitiveWord();
        sw.setId(1L);
        sw.setWord("违规词");
        when(sensitiveWordService.listAll()).thenReturn(List.of(sw));

        mockMvc.perform(get("/api/admin/sensitive-words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].word").value("违规词"));
    }

    @Test
    void shouldAddSensitiveWord() throws Exception {
        SensitiveWordRequest req = new SensitiveWordRequest();
        req.setWord("新敏感词");
        req.setCategory("POLITICAL");

        SensitiveWord sw = new SensitiveWord();
        sw.setId(1L);
        sw.setWord("新敏感词");
        sw.setCategory("POLITICAL");
        when(sensitiveWordService.add("新敏感词", "POLITICAL")).thenReturn(sw);

        mockMvc.perform(post("/api/admin/sensitive-words")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDeleteSensitiveWord() throws Exception {
        doNothing().when(sensitiveWordService).delete(1L);

        mockMvc.perform(delete("/api/admin/sensitive-words/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldBatchImport() throws Exception {
        SensitiveWordBatchRequest req = new SensitiveWordBatchRequest();
        req.setWords(List.of("词1", "词2"));
        req.setCategory("GENERAL");

        when(sensitiveWordService.batchImport(List.of("词1", "词2"), "GENERAL")).thenReturn(2);

        mockMvc.perform(post("/api/admin/sensitive-words/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(2));
    }

    @Test
    void shouldCheckSensitiveWords() throws Exception {
        SensitiveWordCheckRequest req = new SensitiveWordCheckRequest();
        req.setText("包含违规词的文本");

        AhoCorasickAutomaton.MatchResult match = new AhoCorasickAutomaton.MatchResult("违规", 2, 4);
        when(sensitiveWordService.match("包含违规词的文本")).thenReturn(List.of(match));

        mockMvc.perform(post("/api/admin/sensitive-words/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("违规"));
    }
}
