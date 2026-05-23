package com.company.social.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import com.company.social.domain.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    private Group group;
    private GroupMember pendingMember;

    @BeforeEach
    void setUp() {
        group = new Group();
        group.setId(1L);
        group.setName("技术分享组");
        group.setOwnerId(1L);
        group.setVisibility("PUBLIC");
        group.setStatus("APPROVED");

        pendingMember = new GroupMember();
        pendingMember.setId(10L);
        pendingMember.setGroupId(1L);
        pendingMember.setUserId(2L);
        pendingMember.setRole("MEMBER");
        pendingMember.setStatus("PENDING");
    }

    @Test
    void shouldCreateGroupWithOwnerAsMember() {
        doAnswer(inv -> {
            Group g = inv.getArgument(0);
            g.setId(1L);
            return null;
        }).when(groupRepository).insert(any());

        Group result = groupService.create(1L, "技术分享组", "技术交流");

        assertThat(result.getName()).isEqualTo("技术分享组");
        assertThat(result.getOwnerId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("APPROVED");

        ArgumentCaptor<GroupMember> memberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupRepository).insertMember(memberCaptor.capture());
        GroupMember gm = memberCaptor.getValue();
        assertThat(gm.getRole()).isEqualTo("OWNER");
        assertThat(gm.getStatus()).isEqualTo("APPROVED");
        assertThat(gm.getUserId()).isEqualTo(1L);
    }

    @Test
    void shouldGetById() {
        when(groupRepository.findById(1L)).thenReturn(group);

        Group result = groupService.getById(1L);
        assertThat(result.getName()).isEqualTo("技术分享组");
    }

    @Test
    void shouldThrowWhenGroupNotFound() {
        when(groupRepository.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> groupService.getById(999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    void shouldRequestJoin() {
        when(groupRepository.findById(1L)).thenReturn(group);
        when(groupRepository.findMember(1L, 2L)).thenReturn(null);

        groupService.requestJoin(1L, 2L);

        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupRepository).insertMember(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
        assertThat(captor.getValue().getRole()).isEqualTo("MEMBER");
    }

    @Test
    void shouldThrowWhenAlreadyMemberOrPending() {
        when(groupRepository.findById(1L)).thenReturn(group);
        when(groupRepository.findMember(1L, 2L)).thenReturn(pendingMember);

        assertThatThrownBy(() -> groupService.requestJoin(1L, 2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("已是群组成员");
    }

    @Test
    void shouldApproveMember() {
        when(groupRepository.findById(1L)).thenReturn(group);
        when(groupRepository.findMember(1L, 2L)).thenReturn(pendingMember);
        when(groupRepository.updateMemberStatus(10L, "APPROVED")).thenReturn(true);

        groupService.approveMember(1L, 2L, 1L);

        verify(groupRepository).updateMemberStatus(10L, "APPROVED");
    }

    @Test
    void shouldThrowWhenNonOwnerApproves() {
        when(groupRepository.findById(1L)).thenReturn(group);

        assertThatThrownBy(() -> groupService.approveMember(1L, 2L, 99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    void shouldThrowWhenApprovingNonPending() {
        pendingMember.setStatus("APPROVED");
        when(groupRepository.findById(1L)).thenReturn(group);
        when(groupRepository.findMember(1L, 2L)).thenReturn(pendingMember);
        when(groupRepository.updateMemberStatus(10L, "APPROVED")).thenReturn(false);

        assertThatThrownBy(() -> groupService.approveMember(1L, 2L, 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("已处理");
    }

    @Test
    void shouldRejectMember() {
        when(groupRepository.findById(1L)).thenReturn(group);
        when(groupRepository.findMember(1L, 2L)).thenReturn(pendingMember);
        when(groupRepository.updateMemberStatus(10L, "REJECTED")).thenReturn(true);

        groupService.rejectMember(1L, 2L, 1L);

        verify(groupRepository).updateMemberStatus(10L, "REJECTED");
    }

    @Test
    void shouldRemoveMember() {
        when(groupRepository.findById(1L)).thenReturn(group);

        groupService.removeMember(1L, 2L, 1L);

        verify(groupRepository).deleteMember(1L, 2L);
    }

    @Test
    void shouldThrowWhenRemovingOwner() {
        when(groupRepository.findById(1L)).thenReturn(group);

        assertThatThrownBy(() -> groupService.removeMember(1L, 1L, 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能移除群主");
    }

    @Test
    void shouldThrowWhenNonOwnerRemoves() {
        when(groupRepository.findById(1L)).thenReturn(group);

        assertThatThrownBy(() -> groupService.removeMember(1L, 2L, 99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    void shouldListPublic() {
        when(groupRepository.findPublic(1, 12)).thenReturn(List.of(group));
        when(groupRepository.countPublic()).thenReturn(1L);

        PageResult<Group> result = groupService.listPublic(1, 12);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
    }

    @Test
    void shouldListMembers() {
        when(groupRepository.findMembers(1L)).thenReturn(List.of());

        List<GroupMember> members = groupService.listMembers(1L);

        assertThat(members).isEmpty();
    }
}
