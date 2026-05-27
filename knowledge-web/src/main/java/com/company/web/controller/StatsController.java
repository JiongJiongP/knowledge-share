package com.company.web.controller;

import com.company.common.result.Result;
import com.company.content.infrastructure.mapper.ContentMapper;
import com.company.social.infrastructure.mapper.CommentMapper;
import com.company.social.infrastructure.mapper.GroupMapper;
import com.company.userauth.infrastructure.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final ContentMapper contentMapper;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final CommentMapper commentMapper;

    public StatsController(ContentMapper contentMapper, UserMapper userMapper,
                           GroupMapper groupMapper, CommentMapper commentMapper) {
        this.contentMapper = contentMapper;
        this.userMapper = userMapper;
        this.groupMapper = groupMapper;
        this.commentMapper = commentMapper;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        long totalContents = contentMapper.selectCount(null);
        long totalUsers = userMapper.selectCount(null);
        long totalGroups = groupMapper.selectCount(null);
        long totalComments = commentMapper.selectCount(null);
        long todayContents = contentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                        com.company.content.domain.model.KnowledgeContent>()
                        .ge(com.company.content.domain.model.KnowledgeContent::getCreatedAt,
                                LocalDate.now().atStartOfDay())
        );

        return Result.ok(Map.of(
                "totalContents", totalContents,
                "totalUsers", totalUsers,
                "totalGroups", totalGroups,
                "todayContents", todayContents,
                "totalComments", totalComments
        ));
    }
}
