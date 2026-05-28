package com.company.content.interfaces.controller;

import com.company.common.exception.GlobalExceptionHandler;
import com.company.content.application.dto.*;
import com.company.content.application.service.ProductionToolchainService;
import com.company.content.domain.model.*;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductionToolchainControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProductionToolchainService toolchainService;

    @InjectMocks
    private ProductionToolchainController toolchainController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(toolchainController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListVersions() throws Exception {
        ContentVersion v = new ContentVersion();
        v.setId(1L);
        v.setContentId(1L);
        v.setTitle("v1");
        when(toolchainService.listVersions(1L)).thenReturn(List.of(v));

        mockMvc.perform(get("/api/contents/1/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("v1"));
    }

    @Test
    void shouldGetVersion() throws Exception {
        ContentVersion v = new ContentVersion();
        v.setId(1L);
        v.setTitle("v1");
        when(toolchainService.getVersion(1L)).thenReturn(v);

        mockMvc.perform(get("/api/contents/1/versions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("v1"));
    }

    @Test
    void shouldSaveVersion() throws Exception {
        SaveVersionRequest req = new SaveVersionRequest();
        req.setTitle("新版本");
        req.setBody("内容");
        req.setChangeSummary("更新");

        ContentVersion v = new ContentVersion();
        v.setId(1L);
        v.setTitle("新版本");
        when(toolchainService.saveVersion(eq(1L), eq("新版本"), eq("内容"), eq("更新"), eq(1L))).thenReturn(v);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/contents/1/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldListPendingAudits() throws Exception {
        AuditRecord r = new AuditRecord();
        r.setId(1L);
        when(toolchainService.listPendingAudits()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/admin/audit/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void shouldApproveAudit() throws Exception {
        doNothing().when(toolchainService).approveAudit(1L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/admin/audit/1/approve")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldRejectAudit() throws Exception {
        RejectAuditRequest req = new RejectAuditRequest();
        req.setReason("内容违规");

        doNothing().when(toolchainService).rejectAudit(1L, 1L, "内容违规");

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/admin/audit/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldListTemplates() throws Exception {
        ContentTemplate t = new ContentTemplate();
        t.setId(1L);
        t.setName("模板1");
        when(toolchainService.listTemplates()).thenReturn(List.of(t));

        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("模板1"));
    }

    @Test
    void shouldGetTemplate() throws Exception {
        ContentTemplate t = new ContentTemplate();
        t.setId(1L);
        t.setName("模板1");
        when(toolchainService.getTemplate(1L)).thenReturn(t);

        mockMvc.perform(get("/api/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("模板1"));
    }

    @Test
    void shouldCreateTemplate() throws Exception {
        CreateTemplateRequest req = new CreateTemplateRequest();
        req.setName("新模板");
        req.setBody("模板内容");

        ContentTemplate t = new ContentTemplate();
        t.setId(1L);
        t.setName("新模板");
        when(toolchainService.createTemplate(eq("新模板"), isNull(), eq("MARKDOWN"), eq("模板内容"), eq(1L))).thenReturn(t);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDeleteTemplate() throws Exception {
        doNothing().when(toolchainService).deleteTemplate(1L);

        mockMvc.perform(delete("/api/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldSchedulePublish() throws Exception {
        SchedulePublishRequest req = new SchedulePublishRequest();
        req.setScheduledAt("2026-06-01T10:00:00");

        ScheduledPublish sp = new ScheduledPublish();
        sp.setId(1L);
        when(toolchainService.schedule(eq(1L), any(LocalDateTime.class))).thenReturn(sp);

        mockMvc.perform(post("/api/contents/1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldCancelSchedule() throws Exception {
        CancelScheduleRequest req = new CancelScheduleRequest();
        req.setScheduleId(1L);

        doNothing().when(toolchainService).cancelSchedule(1L);

        mockMvc.perform(delete("/api/contents/1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
