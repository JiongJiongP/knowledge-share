package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.content.domain.model.SensitiveWord;
import com.company.content.domain.repository.SensitiveWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensitiveWordServiceTest {

    @Mock
    private SensitiveWordRepository repository;

    @InjectMocks
    private SensitiveWordService service;

    @BeforeEach
    void setUp() {
        SensitiveWord sw1 = new SensitiveWord();
        sw1.setId(1L); sw1.setWord("敏感词"); sw1.setCategory("GENERAL");
        SensitiveWord sw2 = new SensitiveWord();
        sw2.setId(2L); sw2.setWord("bad"); sw2.setCategory("GENERAL");
        when(repository.findAll()).thenReturn(List.of(sw1, sw2));
        service.init();
    }

    @Test
    void shouldDetectSensitiveWord() {
        assertThat(service.containsSensitiveWord("包含敏感词的文本")).isTrue();
        assertThat(service.containsSensitiveWord("clean text")).isFalse();
    }

    @Test
    void shouldFilterSensitiveWord() {
        String result = service.filter("这是敏感词内容");
        assertThat(result).isEqualTo("这是***内容");
    }

    @Test
    void shouldReturnOriginalTextWhenClean() {
        String result = service.filter("正常文本");
        assertThat(result).isEqualTo("正常文本");
    }

    @Test
    void shouldAddWord() {
        when(repository.findByWord("new")).thenReturn(null);
        doAnswer(inv -> null).when(repository).insert(any());

        SensitiveWord result = service.add("new", "GENERAL");
        assertThat(result.getWord()).isEqualTo("new");
    }

    @Test
    void shouldThrowWhenDuplicateWord() {
        SensitiveWord existing = new SensitiveWord();
        existing.setWord("敏感词");
        when(repository.findByWord("敏感词")).thenReturn(existing);

        assertThatThrownBy(() -> service.add("敏感词", "GENERAL"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("已存在");
    }
}
