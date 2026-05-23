package com.company.social.interfaces.controller;

import com.company.common.result.PageResult;
import com.company.common.result.Result;
import com.company.social.application.dto.ApproveMemberRequest;
import com.company.social.application.dto.CreateGroupRequest;
import com.company.social.application.service.GroupService;
import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public Result<PageResult<Group>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "12") int size) {
        return Result.ok(groupService.listPublic(page, size));
    }

    @GetMapping("/{id}")
    public Result<Group> get(@PathVariable Long id) {
        return Result.ok(groupService.getById(id));
    }

    @PostMapping
    public Result<Group> create(@Valid @RequestBody CreateGroupRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(groupService.create(userId, req.getName(), req.getDescription()));
    }

    @PostMapping("/{id}/join")
    public Result<Void> join(@PathVariable Long id, Authentication auth) {
        groupService.requestJoin(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @GetMapping("/{id}/members")
    public Result<List<GroupMember>> members(@PathVariable Long id) {
        return Result.ok(groupService.listMembers(id));
    }

    @GetMapping("/{id}/members/pending")
    public Result<List<GroupMember>> pendingMembers(@PathVariable Long id, Authentication auth) {
        return Result.ok(groupService.listPendingMembers(id, (Long) auth.getPrincipal()));
    }

    @PutMapping("/{id}/members/{userId}")
    public Result<Void> approveMember(@PathVariable Long id,
                                      @PathVariable Long userId,
                                      @Valid @RequestBody ApproveMemberRequest req,
                                      Authentication auth) {
        if ("APPROVED".equals(req.getAction())) {
            groupService.approveMember(id, userId, (Long) auth.getPrincipal());
        } else if ("REJECTED".equals(req.getAction())) {
            groupService.rejectMember(id, userId, (Long) auth.getPrincipal());
        } else {
            return Result.fail(400, "无效的操作");
        }
        return Result.ok(null);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long id,
                                     @PathVariable Long userId,
                                     Authentication auth) {
        groupService.removeMember(id, userId, (Long) auth.getPrincipal());
        return Result.ok(null);
    }
}
