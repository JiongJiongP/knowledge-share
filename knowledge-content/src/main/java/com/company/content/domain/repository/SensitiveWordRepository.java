package com.company.content.domain.repository;

import com.company.content.domain.model.SensitiveWord;

import java.util.List;

public interface SensitiveWordRepository {
    List<SensitiveWord> findAll();
    SensitiveWord findByWord(String word);
    void insert(SensitiveWord word);
    void delete(Long id);
    void batchInsert(List<SensitiveWord> words);
}
