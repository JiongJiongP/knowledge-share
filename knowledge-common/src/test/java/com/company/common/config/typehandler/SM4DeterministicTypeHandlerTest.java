package com.company.common.config.typehandler;

import com.company.common.config.Sm4Config;
import com.company.common.util.SM4Util;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SM4DeterministicTypeHandlerTest {

    private static final String DATA_KEY = "0123456789abcdef0123456789abcdef";
    private final SM4DeterministicTypeHandler handler = new SM4DeterministicTypeHandler();

    @BeforeAll
    static void setUp() {
        Sm4Config.initializeForTest(DATA_KEY);
    }

    @Test
    void shouldEncryptAndDecrypt() throws Exception {
        String plaintext = "张三";

        PreparedStatement ps = mock(PreparedStatement.class);
        handler.setNonNullParameter(ps, 1, plaintext, JdbcType.VARCHAR);

        verify(ps).setString(eq(1), argThat(cipher -> !cipher.equals(plaintext)));
    }

    @Test
    void shouldDecryptFromResultSetByColumnName() throws Exception {
        String plaintext = "测试值";
        String encrypted = SM4Util.encryptDeterministic(plaintext, DATA_KEY);

        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("column")).thenReturn(encrypted);

        String result = handler.getNullableResult(rs, "column");
        assertThat(result).isEqualTo(plaintext);
    }

    @Test
    void shouldDecryptFromResultSetByColumnIndex() throws Exception {
        String plaintext = "测试值";
        String encrypted = SM4Util.encryptDeterministic(plaintext, DATA_KEY);

        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn(encrypted);

        String result = handler.getNullableResult(rs, 1);
        assertThat(result).isEqualTo(plaintext);
    }

    @Test
    void shouldDecryptFromCallableStatement() throws Exception {
        String plaintext = "测试值";
        String encrypted = SM4Util.encryptDeterministic(plaintext, DATA_KEY);

        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString(1)).thenReturn(encrypted);

        String result = handler.getNullableResult(cs, 1);
        assertThat(result).isEqualTo(plaintext);
    }

    @Test
    void shouldReturnNullWhenResultSetValueIsNull() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("column")).thenReturn(null);

        String result = handler.getNullableResult(rs, "column");
        assertThat(result).isNull();
    }

    @Test
    void shouldThrowOnInvalidCiphertext() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("column")).thenReturn("invalid-ciphertext");

        assertThatThrownBy(() -> handler.getNullableResult(rs, "column"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    void shouldProduceSameCiphertextForSamePlaintext() throws Exception {
        String plaintext = "确定性加密";

        String encrypted1 = SM4Util.encryptDeterministic(plaintext, DATA_KEY);
        String encrypted2 = SM4Util.encryptDeterministic(plaintext, DATA_KEY);
        assertThat(encrypted1).isEqualTo(encrypted2);
    }
}
