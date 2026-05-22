package com.company.userauth.infrastructure.mapper;

import com.company.userauth.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void shouldInsertAndSelectUserById() {
        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("encoded_password");
        user.setDisplayName("张三");
        user.setEmail("zhangsan@company.com");
        user.setDepartmentId(1L);
        user.setStatus("ACTIVE");

        int rows = userMapper.insert(user);
        assertThat(rows).isEqualTo(1);
        assertThat(user.getId()).isNotNull();

        User found = userMapper.selectById(user.getId());
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("zhangsan");
        assertThat(found.getDisplayName()).isEqualTo("张三");
        assertThat(found.getEmail()).isEqualTo("zhangsan@company.com");
    }

    @Test
    void shouldSelectUserByUsername() {
        User user = new User();
        user.setUsername("lisi");
        user.setPassword("encoded_password");
        user.setDisplayName("李四");
        user.setDepartmentId(2L);
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        // Using LambdaQueryWrapper to query by username
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, "lisi");
        User found = userMapper.selectOne(wrapper);

        assertThat(found).isNotNull();
        assertThat(found.getDisplayName()).isEqualTo("李四");
    }

    @Test
    void shouldAutoFillTimestampsOnInsert() {
        User user = new User();
        user.setUsername("wangwu");
        user.setPassword("encoded_password");
        user.setDisplayName("王五");
        user.setDepartmentId(1L);
        user.setStatus("ACTIVE");

        userMapper.insert(user);

        User found = userMapper.selectById(user.getId());
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
