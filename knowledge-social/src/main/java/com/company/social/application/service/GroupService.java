package com.company.social.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.social.application.dto.GroupMemberVO;
import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import com.company.social.domain.repository.GroupRepository;
import com.company.userauth.domain.model.User;
import com.company.userauth.infrastructure.mapper.UserMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserMapper userMapper;

    public GroupService(GroupRepository groupRepository, UserMapper userMapper) {
        this.groupRepository = groupRepository;
        this.userMapper = userMapper;
    }

    public PageResult<Group> listPublic(int page, int size) {
        List<Group> groups = groupRepository.findPublic(page, size);
        enrichGroups(groups);
        return PageResult.of(groups, groupRepository.countPublic(), page, size);
    }

    public Group getById(Long id) {
        Group g = groupRepository.findById(id);
        if (g == null) {
            throw BizException.notFound("群组");
        }
        enrichGroups(List.of(g));
        return g;
    }

    private void enrichGroups(List<Group> groups) {
        if (groups.isEmpty()) return;

        // Resolve owner names
        List<Long> ownerIds = groups.stream().map(Group::getOwnerId).distinct().collect(Collectors.toList());
        Map<Long, String> userMap = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .in(User::getId, ownerIds)
        ).stream().collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));

        // Resolve member counts
        List<Long> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());
        Map<Long, Long> countMap = groupRepository.countMembersBatch(groupIds);

        for (Group g : groups) {
            g.setOwnerName(userMap.getOrDefault(g.getOwnerId(), "未知"));
            g.setMemberCount(countMap.getOrDefault(g.getId(), 0L).intValue());
        }
    }

    @Transactional
    public Group create(Long userId, String name, String description) {
        Group g = new Group();
        g.setName(name);
        g.setDescription(description);
        g.setOwnerId(userId);
        g.setVisibility("PUBLIC");
        g.setStatus("APPROVED");
        groupRepository.insert(g);

        GroupMember gm = new GroupMember();
        gm.setGroupId(g.getId());
        gm.setUserId(userId);
        gm.setRole("OWNER");
        gm.setStatus("APPROVED");
        gm.setJoinedAt(LocalDateTime.now());
        groupRepository.insertMember(gm);

        return g;
    }

    @Transactional
    public void requestJoin(Long groupId, Long userId) {
        getById(groupId);
        GroupMember existing = groupRepository.findMember(groupId, userId);
        if (existing != null) {
            throw BizException.badRequest("已是群组成员或已提交申请");
        }
        GroupMember gm = new GroupMember();
        gm.setGroupId(groupId);
        gm.setUserId(userId);
        gm.setRole("MEMBER");
        gm.setStatus("PENDING");
        try {
            groupRepository.insertMember(gm);
        } catch (DataIntegrityViolationException e) {
            throw BizException.badRequest("已是群组成员或已提交申请");
        }
    }

    @Transactional
    public void approveMember(Long groupId, Long userId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) {
            throw BizException.forbidden();
        }
        GroupMember member = groupRepository.findMember(groupId, userId);
        if (member == null) {
            throw BizException.notFound("申请");
        }
        boolean updated = groupRepository.updateMemberStatus(member.getId(), "APPROVED");
        if (!updated) {
            throw BizException.badRequest("该申请已处理");
        }
    }

    @Transactional
    public void rejectMember(Long groupId, Long userId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) {
            throw BizException.forbidden();
        }
        GroupMember member = groupRepository.findMember(groupId, userId);
        if (member == null) {
            throw BizException.notFound("申请");
        }
        boolean updated = groupRepository.updateMemberStatus(member.getId(), "REJECTED");
        if (!updated) {
            throw BizException.badRequest("该申请已处理");
        }
    }

    @Transactional
    public void removeMember(Long groupId, Long userId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) {
            throw BizException.forbidden();
        }
        if (userId.equals(g.getOwnerId())) {
            throw BizException.badRequest("不能移除群主");
        }
        groupRepository.deleteMember(groupId, userId);
    }

    public List<GroupMemberVO> listMembers(Long groupId) {
        return toVOList(groupRepository.findMembers(groupId));
    }

    public List<GroupMemberVO> listPendingMembers(Long groupId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) {
            throw BizException.forbidden();
        }
        return toVOList(groupRepository.findPendingMembers(groupId));
    }

    private List<GroupMemberVO> toVOList(List<GroupMember> members) {
        if (members.isEmpty()) {
            return List.of();
        }
        Map<Long, String> userMap = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .in(User::getId, members.stream().map(GroupMember::getUserId).distinct().collect(Collectors.toList()))
        ).stream().collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));

        return members.stream().map(m -> {
            GroupMemberVO vo = new GroupMemberVO();
            vo.setId(m.getId());
            vo.setGroupId(m.getGroupId());
            vo.setUserId(m.getUserId());
            vo.setUserName(userMap.getOrDefault(m.getUserId(), String.valueOf(m.getUserId())));
            vo.setRole(m.getRole());
            vo.setStatus(m.getStatus());
            vo.setJoinedAt(m.getJoinedAt() != null ? m.getJoinedAt().toString() : null);
            vo.setCreatedAt(m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
            return vo;
        }).collect(Collectors.toList());
    }
}
