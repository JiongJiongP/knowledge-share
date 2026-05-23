package com.company.social.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import com.company.social.domain.repository.GroupRepository;
import com.company.social.infrastructure.mapper.GroupMapper;
import com.company.social.infrastructure.mapper.GroupMemberMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class GroupRepositoryImpl implements GroupRepository {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;

    public GroupRepositoryImpl(GroupMapper groupMapper, GroupMemberMapper groupMemberMapper) {
        this.groupMapper = groupMapper;
        this.groupMemberMapper = groupMemberMapper;
    }

    @Override
    public Group findById(Long id) {
        return groupMapper.selectById(id);
    }

    @Override
    public List<Group> findPublic(int page, int size) {
        LambdaQueryWrapper<Group> qw = new LambdaQueryWrapper<>();
        qw.eq(Group::getVisibility, "PUBLIC")
          .eq(Group::getStatus, "APPROVED")
          .orderByDesc(Group::getCreatedAt);
        return groupMapper.selectPage(new Page<>(page, size, false), qw).getRecords();
    }

    @Override
    public long countPublic() {
        return groupMapper.selectCount(
            new LambdaQueryWrapper<Group>()
                .eq(Group::getVisibility, "PUBLIC")
                .eq(Group::getStatus, "APPROVED")
        );
    }

    @Override
    public void insert(Group group) {
        groupMapper.insert(group);
    }

    @Override
    public void update(Group group) {
        groupMapper.updateById(group);
    }

    @Override
    public void softDelete(Long id) {
        groupMapper.deleteById(id);
    }

    @Override
    public void insertMember(GroupMember member) {
        groupMemberMapper.insert(member);
    }

    @Override
    public GroupMember findMember(Long groupId, Long userId) {
        return groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
        );
    }

    @Override
    public List<GroupMember> findMembers(Long groupId) {
        return groupMemberMapper.selectList(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getStatus, "APPROVED")
        );
    }

    @Override
    public List<GroupMember> findPendingMembers(Long groupId) {
        return groupMemberMapper.selectList(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getStatus, "PENDING")
        );
    }

    @Override
    public boolean updateMemberStatus(Long memberId, String status) {
        LambdaQueryWrapper<GroupMember> qw = new LambdaQueryWrapper<>();
        qw.eq(GroupMember::getId, memberId)
          .eq(GroupMember::getStatus, "PENDING");

        GroupMember m = new GroupMember();
        m.setStatus(status);
        if ("APPROVED".equals(status)) {
            m.setJoinedAt(LocalDateTime.now());
        }
        return groupMemberMapper.update(m, qw) > 0;
    }

    @Override
    public void deleteMember(Long groupId, Long userId) {
        groupMemberMapper.delete(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
        );
    }
}
