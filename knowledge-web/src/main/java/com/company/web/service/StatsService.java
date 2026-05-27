package com.company.web.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.infrastructure.mapper.ContentMapper;
import com.company.social.infrastructure.mapper.CommentMapper;
import com.company.social.infrastructure.mapper.GroupMapper;
import com.company.userauth.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
public class StatsService {

    private final ContentMapper contentMapper;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final CommentMapper commentMapper;

    public StatsService(ContentMapper contentMapper, UserMapper userMapper,
                        GroupMapper groupMapper, CommentMapper commentMapper) {
        this.contentMapper = contentMapper;
        this.userMapper = userMapper;
        this.groupMapper = groupMapper;
        this.commentMapper = commentMapper;
    }

    public Map<String, Object> overview() {
        long totalContents = contentMapper.selectCount(null);
        long totalUsers = userMapper.selectCount(null);
        long totalGroups = groupMapper.selectCount(null);
        long totalComments = commentMapper.selectCount(null);
        long todayContents = contentMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeContent>()
                        .ge(KnowledgeContent::getCreatedAt, LocalDate.now().atStartOfDay())
        );

        return Map.of(
                "totalContents", totalContents,
                "totalUsers", totalUsers,
                "totalGroups", totalGroups,
                "todayContents", todayContents,
                "totalComments", totalComments
        );
    }
}
