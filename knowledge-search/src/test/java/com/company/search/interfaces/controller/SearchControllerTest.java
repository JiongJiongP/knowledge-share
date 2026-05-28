package com.company.search.interfaces.controller;

import com.company.common.exception.GlobalExceptionHandler;
import com.company.search.application.dto.SearchResult;
import com.company.search.application.service.SearchService;
import com.company.search.application.service.VectorSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SearchService searchService;

    @Mock
    private VectorSearchService vectorSearchService;

    @InjectMocks
    private SearchController searchController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldSearchByKeyword() throws Exception {
        when(searchService.search("测试", 1, 10, "relevance")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/search?keyword=测试&page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void shouldVectorSearch() throws Exception {
        when(vectorSearchService.search("测试", 10)).thenReturn(List.of(1L, 2L));

        mockMvc.perform(get("/api/search/vector?q=测试&topK=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.ids").isArray());
    }

    @Test
    void shouldHybridSearch() throws Exception {
        when(searchService.hybridSearch("测试", 1, 10)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/search/hybrid?keyword=测试&page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }
}
