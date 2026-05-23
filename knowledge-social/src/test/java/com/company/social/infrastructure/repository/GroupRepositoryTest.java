package com.company.social.infrastructure.repository;

import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import com.company.social.domain.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.company.social.TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @Test
    void shouldInsertAndFindGroupById() {
        Group group = new Group();
        group.setName("测试群组");
        group.setDescription("描述");
        group.setOwnerId(1L);
        group.setVisibility("PUBLIC");
        group.setStatus("APPROVED");

        groupRepository.insert(group);
        assertThat(group.getId()).isNotNull();

        Group found = groupRepository.findById(group.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("测试群组");
    }

    @Test
    void shouldFindPublicGroups() {
        for (int i = 1; i <= 5; i++) {
            Group g = new Group();
            g.setName("公开群组 " + i);
            g.setOwnerId(1L);
            g.setVisibility("PUBLIC");
            g.setStatus("APPROVED");
            groupRepository.insert(g);
        }
        // Private group should not appear
        Group privateG = new Group();
        privateG.setName("私有群组");
        privateG.setOwnerId(1L);
        privateG.setVisibility("PARTIAL");
        privateG.setStatus("APPROVED");
        groupRepository.insert(privateG);

        List<Group> page = groupRepository.findPublic(1, 10);
        assertThat(page).hasSize(5);
        assertThat(page).allMatch(g -> "PUBLIC".equals(g.getVisibility()));

        long count = groupRepository.countPublic();
        assertThat(count).isEqualTo(5);
    }

    @Test
    void shouldSoftDelete() {
        Group group = new Group();
        group.setName("待删除群组");
        group.setOwnerId(1L);
        group.setVisibility("PUBLIC");
        group.setStatus("APPROVED");
        groupRepository.insert(group);

        groupRepository.softDelete(group.getId());

        Group found = groupRepository.findById(group.getId());
        assertThat(found).isNull();
    }

    @Test
    void shouldInsertAndFindMember() {
        Group group = new Group();
        group.setName("群组");
        group.setOwnerId(1L);
        group.setVisibility("PUBLIC");
        group.setStatus("APPROVED");
        groupRepository.insert(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(2L);
        member.setRole("MEMBER");
        member.setStatus("PENDING");
        groupRepository.insertMember(member);
        assertThat(member.getId()).isNotNull();

        GroupMember found = groupRepository.findMember(group.getId(), 2L);
        assertThat(found).isNotNull();
        assertThat(found.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldApprovePendingMember() {
        Group group = new Group();
        group.setName("群组");
        group.setOwnerId(1L);
        group.setVisibility("PUBLIC");
        group.setStatus("APPROVED");
        groupRepository.insert(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(2L);
        member.setRole("MEMBER");
        member.setStatus("PENDING");
        groupRepository.insertMember(member);

        groupRepository.updateMemberStatus(member.getId(), "APPROVED");

        GroupMember updated = groupRepository.findMember(group.getId(), 2L);
        assertThat(updated.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void shouldDeleteMember() {
        Group group = new Group();
        group.setName("群组");
        group.setOwnerId(1L);
        group.setVisibility("PUBLIC");
        group.setStatus("APPROVED");
        groupRepository.insert(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(2L);
        member.setRole("MEMBER");
        member.setStatus("APPROVED");
        groupRepository.insertMember(member);

        groupRepository.deleteMember(group.getId(), 2L);

        GroupMember found = groupRepository.findMember(group.getId(), 2L);
        assertThat(found).isNull();
    }

    @Test
    void shouldFindMembersAndFilterPending() {
        Group group = new Group();
        group.setName("群组");
        group.setOwnerId(1L);
        group.setVisibility("PUBLIC");
        group.setStatus("APPROVED");
        groupRepository.insert(group);

        for (int i = 1; i <= 3; i++) {
            GroupMember m = new GroupMember();
            m.setGroupId(group.getId());
            m.setUserId((long) i);
            m.setRole(i == 1 ? "OWNER" : "MEMBER");
            m.setStatus(i == 3 ? "PENDING" : "APPROVED");
            m.setJoinedAt(LocalDateTime.now());
            groupRepository.insertMember(m);
        }

        List<GroupMember> approved = groupRepository.findMembers(group.getId());
        assertThat(approved).hasSize(2);
        assertThat(approved).allMatch(m -> "APPROVED".equals(m.getStatus()));

        List<GroupMember> pending = groupRepository.findPendingMembers(group.getId());
        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getStatus()).isEqualTo("PENDING");
    }
}
