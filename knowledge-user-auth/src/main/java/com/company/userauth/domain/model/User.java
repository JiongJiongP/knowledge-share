package com.company.userauth.domain.model;

import com.company.common.base.BaseEntity;
import com.company.common.config.typehandler.SM4DeterministicTypeHandler;
import com.company.common.config.typehandler.SM4EncryptTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    @TableField(typeHandler = SM4DeterministicTypeHandler.class)
    private String username;

    private String password;

    @TableField(typeHandler = SM4EncryptTypeHandler.class)
    private String displayName;

    @TableField(typeHandler = SM4DeterministicTypeHandler.class)
    private String email;

    private String ssoId;
    private Long departmentId;
    private String status;

    @TableField(exist = false)
    private List<String> roles;
}
