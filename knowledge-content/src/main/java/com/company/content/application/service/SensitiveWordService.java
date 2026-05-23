package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.content.domain.model.SensitiveWord;
import com.company.content.domain.repository.SensitiveWordRepository;
import com.company.content.domain.service.AhoCorasickAutomaton;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SensitiveWordService {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordService.class);

    private final SensitiveWordRepository repository;
    private final AhoCorasickAutomaton automaton = new AhoCorasickAutomaton();

    public SensitiveWordService(SensitiveWordRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        rebuildAutomaton();
    }

    public List<SensitiveWord> listAll() {
        return repository.findAll();
    }

    @Transactional
    public SensitiveWord add(String word, String category) {
        SensitiveWord existing = repository.findByWord(word);
        if (existing != null) {
            throw BizException.badRequest("敏感词已存在");
        }
        SensitiveWord sw = new SensitiveWord();
        sw.setWord(word);
        sw.setCategory(category != null ? category : "GENERAL");
        repository.insert(sw);
        rebuildAutomaton();
        return sw;
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(id);
        rebuildAutomaton();
    }

    @Transactional
    public int batchImport(List<String> words, String category) {
        int count = 0;
        for (String word : words) {
            String trimmed = word.trim();
            if (trimmed.isEmpty()) continue;
            try {
                SensitiveWord sw = new SensitiveWord();
                sw.setWord(trimmed);
                sw.setCategory(category != null ? category : "GENERAL");
                repository.insert(sw);
                count++;
            } catch (DataIntegrityViolationException e) {
                log.debug("Duplicate sensitive word skipped: {}", trimmed);
            }
        }
        if (count > 0) rebuildAutomaton();
        return count;
    }

    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) return false;
        return automaton.containsAny(text);
    }

    public List<AhoCorasickAutomaton.MatchResult> match(String text) {
        if (text == null || text.isEmpty()) return List.of();
        return automaton.match(text);
    }

    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;
        List<AhoCorasickAutomaton.MatchResult> matches = automaton.match(text);
        if (matches.isEmpty()) return text;
        StringBuilder sb = new StringBuilder(text);
        // Replace from end to start to preserve indices
        matches.sort((a, b) -> Integer.compare(b.start(), a.start()));
        for (var m : matches) {
            sb.replace(m.start(), m.end(), "*".repeat(m.word().length()));
        }
        return sb.toString();
    }

    private void rebuildAutomaton() {
        List<SensitiveWord> words = repository.findAll();
        automaton.build(words.stream().map(SensitiveWord::getWord).toList());
        log.info("AC automaton rebuilt with {} sensitive words", words.size());
    }
}
