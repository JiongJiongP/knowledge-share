package com.company.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class SM4UtilTest {

    @Test
    void shouldGenerateKeyHex() throws Exception {
        String keyHex = SM4Util.generateKeyHex();
        assertThat(keyHex).isNotNull();
        assertThat(keyHex).hasSize(32);
    }

    @Test
    void shouldGenerateKeyBase64() throws Exception {
        String keyBase64 = SM4Util.generateKeyBase64();
        assertThat(keyBase64).isNotNull();
        assertThat(keyBase64).isNotEmpty();
    }

    @Test
    void shouldEncryptAndDecrypt() throws Exception {
        String keyHex = SM4Util.generateKeyHex();
        String plaintext = "Hello, 世界! @#$%";

        String encrypted = SM4Util.encrypt(plaintext, keyHex);
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plaintext);

        String decrypted = SM4Util.decrypt(encrypted, keyHex);
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void shouldEncryptEmptyString() throws Exception {
        String keyHex = SM4Util.generateKeyHex();
        String plaintext = "";

        String encrypted = SM4Util.encrypt(plaintext, keyHex);
        String decrypted = SM4Util.decrypt(encrypted, keyHex);
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void shouldEncryptUnicodeText() throws Exception {
        String keyHex = SM4Util.generateKeyHex();
        String plaintext = "中文测试内容，包含特殊字符：！@#￥%……&*（）";

        String encrypted = SM4Util.encrypt(plaintext, keyHex);
        String decrypted = SM4Util.decrypt(encrypted, keyHex);
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void shouldProduceDifferentCiphertextForSamePlaintext() throws Exception {
        String keyHex = SM4Util.generateKeyHex();
        String plaintext = "same text";

        String encrypted1 = SM4Util.encrypt(plaintext, keyHex);
        String encrypted2 = SM4Util.encrypt(plaintext, keyHex);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void shouldDecryptWithCorrectKey() throws Exception {
        String keyHex = SM4Util.generateKeyHex();
        String plaintext = "secret data";

        String encrypted = SM4Util.encrypt(plaintext, keyHex);
        String decrypted = SM4Util.decrypt(encrypted, keyHex);
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void shouldGenerateDifferentKeysEachTime() throws Exception {
        String key1 = SM4Util.generateKeyHex();
        String key2 = SM4Util.generateKeyHex();
        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldEncryptLongText() throws Exception {
        String keyHex = SM4Util.generateKeyHex();
        String plaintext = "a".repeat(10000);

        String encrypted = SM4Util.encrypt(plaintext, keyHex);
        String decrypted = SM4Util.decrypt(encrypted, keyHex);
        assertThat(decrypted).isEqualTo(plaintext);
    }
}
