package com.company.content.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AhoCorasickAutomatonTest {

    private final AhoCorasickAutomaton automaton = new AhoCorasickAutomaton();

    @BeforeEach
    void setUp() {
        automaton.build(List.of("敏感词", "测试", "bad", "word"));
    }

    @Test
    void shouldMatchSingleWord() {
        var results = automaton.match("这是一个敏感词测试");
        assertThat(results).hasSize(2);
        assertThat(results.stream().map(AhoCorasickAutomaton.MatchResult::word))
                .contains("敏感词", "测试");
    }

    @Test
    void shouldMatchEnglishWord() {
        var results = automaton.match("this is a bad word here");
        assertThat(results).hasSize(2);
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        var results = automaton.match("clean text");
        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNull() {
        var results = automaton.match("");
        assertThat(results).isEmpty();
    }

    @Test
    void shouldContainAny() {
        assertThat(automaton.containsAny("包含敏感词")).isTrue();
        assertThat(automaton.containsAny("clean")).isFalse();
    }

    @Test
    void shouldMatchOverlapping() {
        automaton.build(List.of("ab", "bc", "abc"));
        var results = automaton.match("abc");
        assertThat(results).hasSize(3); // ab, bc, abc
    }

    @Test
    void shouldBeEmptyInitially() {
        var a = new AhoCorasickAutomaton();
        assertThat(a.isEmpty()).isTrue();
    }
}
