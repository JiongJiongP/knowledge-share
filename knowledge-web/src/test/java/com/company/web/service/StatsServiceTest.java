package com.company.web.service;

import com.company.content.domain.model.KnowledgeContent;
import com.company.content.infrastructure.mapper.ContentMapper;
import com.company.social.infrastructure.mapper.CommentMapper;
import com.company.social.infrastructure.mapper.GroupMapper;
import com.company.userauth.infrastructure.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private ContentMapper contentMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private StatsService statsService;

    @Test
    void shouldReturnOverview() {
        when(contentMapper.selectCount(any())).thenReturn(100L);
        when(userMapper.selectCount(any())).thenReturn(50L);
        when(groupMapper.selectCount(any())).thenReturn(10L);
        when(commentMapper.selectCount(any())).thenReturn(200L);

        Map<String, Object> overview = statsService.overview();

        assertThat(overview).containsEntry("totalContents", 100L);
        assertThat(overview).containsEntry("totalUsers", 50L);
        assertThat(overview).containsEntry("totalGroups", 10L);
        assertThat(overview).containsEntry("totalComments", 200L);
        assertThat(overview).containsKey("todayContents");
    }
}
