package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.content.domain.model.SensitiveWord;
import com.company.content.domain.repository.SensitiveWordRepository;
import com.company.content.domain.service.AhoCorasickAutomaton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void shouldMatchSensitiveWords() {
        List<AhoCorasickAutomaton.MatchResult> matches = service.match("包含敏感词的文本");
        assertThat(matches).isNotEmpty();
        assertThat(matches.get(0).word()).isEqualTo("敏感词");
    }

    @Test
    void shouldReturnEmptyMatchForCleanText() {
        List<AhoCorasickAutomaton.MatchResult> matches = service.match("clean text");
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldReturnEmptyMatchForNullText() {
        List<AhoCorasickAutomaton.MatchResult> matches = service.match(null);
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldReturnEmptyMatchForEmptyText() {
        List<AhoCorasickAutomaton.MatchResult> matches = service.match("");
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldReturnFalseForNullTextInContains() {
        assertThat(service.containsSensitiveWord(null)).isFalse();
        assertThat(service.containsSensitiveWord("")).isFalse();
    }

    @Test
    void shouldReturnOriginalForNullInFilter() {
        assertThat(service.filter(null)).isNull();
        assertThat(service.filter("")).isEmpty();
    }

    @Test
    void shouldDeleteWordAndRebuild() {
        doNothing().when(repository).delete(1L);

        service.delete(1L);

        verify(repository).delete(1L);
        verify(repository, atLeast(2)).findAll();
    }

    @Test
    void shouldBatchImportWords() {
        when(repository.findAll()).thenReturn(List.of());
        doNothing().when(repository).insert(any());

        int count = service.batchImport(List.of("词1", "词2", "  ", "词3"), "POLITICAL");

        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldSkipDuplicateInBatchImport() {
        when(repository.findAll()).thenReturn(List.of());
        doThrow(DataIntegrityViolationException.class).when(repository).insert(argThat(sw -> "词1".equals(sw.getWord())));
        doNothing().when(repository).insert(argThat(sw -> !"词1".equals(sw.getWord())));

        int count = service.batchImport(List.of("词1", "词2"), "GENERAL");

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldUseDefaultCategoryWhenNull() {
        when(repository.findByWord("newword")).thenReturn(null);
        doAnswer(inv -> null).when(repository).insert(any());

        SensitiveWord result = service.add("newword", null);
        assertThat(result.getCategory()).isEqualTo("GENERAL");
    }

    @Test
    void shouldUseDefaultCategoryInBatchImport() {
        when(repository.findAll()).thenReturn(List.of());
        doNothing().when(repository).insert(any());

        service.batchImport(List.of("word1"), null);

        verify(repository).insert(argThat(sw -> "GENERAL".equals(sw.getCategory())));
    }

    @Test
    void shouldFilterMultipleSensitiveWords() {
        String result = service.filter("敏感词和bad内容");
        assertThat(result).isEqualTo("***和***内容");
    }
}
