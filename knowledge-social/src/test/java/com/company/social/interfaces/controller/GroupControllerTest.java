package com.company.social.interfaces.controller;

import com.company.common.exception.GlobalExceptionHandler;
import com.company.common.result.PageResult;
import com.company.social.application.dto.ApproveMemberRequest;
import com.company.social.application.dto.CreateGroupRequest;
import com.company.social.application.dto.GroupMemberVO;
import com.company.social.application.service.GroupService;
import com.company.social.domain.model.Group;
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
class GroupControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    private Group group;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        group = new Group();
        group.setId(1L);
        group.setName("技术分享组");
        group.setOwnerId(1L);
    }

    @Test
    void shouldListGroups() throws Exception {
        PageResult<Group> page = PageResult.of(List.of(group), 1L, 1, 12);
        when(groupService.listPublic(1, 12)).thenReturn(page);

        mockMvc.perform(get("/api/groups?page=1&size=12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].name").value("技术分享组"));
    }

    @Test
    void shouldGetGroupById() throws Exception {
        when(groupService.getById(1L)).thenReturn(group);

        mockMvc.perform(get("/api/groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("技术分享组"));
    }

    @Test
    void shouldCreateGroup() throws Exception {
        CreateGroupRequest req = new CreateGroupRequest();
        req.setName("新群组");
        req.setDescription("描述");

        when(groupService.create(eq(1L), eq("新群组"), eq("描述"))).thenReturn(group);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldJoinGroup() throws Exception {
        doNothing().when(groupService).requestJoin(1L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(post("/api/groups/1/join")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldListMembers() throws Exception {
        GroupMemberVO vo = new GroupMemberVO();
        vo.setId(10L);
        vo.setUserId(1L);
        vo.setRole("OWNER");
        when(groupService.listMembers(1L)).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/groups/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].role").value("OWNER"));
    }

    @Test
    void shouldListPendingMembers() throws Exception {
        GroupMemberVO vo = new GroupMemberVO();
        vo.setId(11L);
        vo.setUserId(2L);
        vo.setRole("MEMBER");
        vo.setStatus("PENDING");
        when(groupService.listPendingMembers(1L, 1L)).thenReturn(List.of(vo));

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(get("/api/groups/1/members/pending")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void shouldApproveMember() throws Exception {
        ApproveMemberRequest req = new ApproveMemberRequest();
        req.setAction("APPROVED");
        doNothing().when(groupService).approveMember(1L, 2L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(put("/api/groups/1/members/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldRejectMember() throws Exception {
        ApproveMemberRequest req = new ApproveMemberRequest();
        req.setAction("REJECTED");
        doNothing().when(groupService).rejectMember(1L, 2L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(put("/api/groups/1/members/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturn400ForInvalidAction() throws Exception {
        ApproveMemberRequest req = new ApproveMemberRequest();
        req.setAction("INVALID");

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(put("/api/groups/1/members/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldRemoveMember() throws Exception {
        doNothing().when(groupService).removeMember(1L, 2L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(delete("/api/groups/1/members/2")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
