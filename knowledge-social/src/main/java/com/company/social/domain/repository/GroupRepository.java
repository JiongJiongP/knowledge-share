package com.company.social.domain.repository;

import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;

import java.util.List;

public interface GroupRepository {
    Group findById(Long id);
    List<Group> findPublic(int page, int size);
    long countPublic();
    void insert(Group group);
    void update(Group group);
    void softDelete(Long id);

    void insertMember(GroupMember member);
    GroupMember findMember(Long groupId, Long userId);
    List<GroupMember> findMembers(Long groupId);
    List<GroupMember> findPendingMembers(Long groupId);
    boolean updateMemberStatus(Long memberId, String status);
    void deleteMember(Long groupId, Long userId);
}
