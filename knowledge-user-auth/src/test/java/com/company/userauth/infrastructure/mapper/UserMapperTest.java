package com.company.userauth.infrastructure.mapper;

import com.company.common.config.Sm4Config;
import com.company.common.util.SM4Util;
import com.company.userauth.domain.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.company.userauth.TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void shouldInsertAndSelectUserById() throws Exception {
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
        assertThat(SM4Util.decryptDeterministic(found.getUsername(), Sm4Config.getDataKey())).isEqualTo("zhangsan");
        assertThat(SM4Util.decryptDeterministic(found.getDisplayName(), Sm4Config.getDataKey())).isEqualTo("张三");
        assertThat(SM4Util.decryptDeterministic(found.getEmail(), Sm4Config.getDataKey())).isEqualTo("zhangsan@company.com");
    }

    @Test
    void shouldSelectUserByUsername() throws Exception {
        User user = new User();
        user.setUsername("lisi");
        user.setPassword("encoded_password");
        user.setDisplayName("李四");
        user.setDepartmentId(2L);
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        String encrypted = SM4Util.encryptDeterministic("lisi", Sm4Config.getDataKey());
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, encrypted);
        User found = userMapper.selectOne(wrapper);

        assertThat(found).isNotNull();
        assertThat(SM4Util.decryptDeterministic(found.getDisplayName(), Sm4Config.getDataKey())).isEqualTo("李四");
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
