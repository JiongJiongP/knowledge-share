package com.company.common.config.typehandler;

import com.company.common.config.Sm4Config;
import com.company.common.util.SM4Util;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Deterministic SM4 encryption. IV = SHA-256(plaintext)[0:16], same plaintext → same ciphertext. */
public class SM4DeterministicTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, SM4Util.encryptDeterministic(parameter, Sm4Config.getDataKey()));
        } catch (Exception e) {
            throw new SQLException("SM4 deterministic encrypt failed", e);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? decrypt(value) : null;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? decrypt(value) : null;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? decrypt(value) : null;
    }

    private String decrypt(String value) throws SQLException {
        try {
            return SM4Util.decryptDeterministic(value, Sm4Config.getDataKey());
        } catch (Exception e) {
            throw new SQLException("SM4 deterministic decrypt failed", e);
        }
    }
}
