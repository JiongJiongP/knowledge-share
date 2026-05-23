package com.company.content.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.content.domain.model.SensitiveWord;
import com.company.content.domain.repository.SensitiveWordRepository;
import com.company.content.infrastructure.mapper.SensitiveWordMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SensitiveWordRepositoryImpl implements SensitiveWordRepository {

    private final SensitiveWordMapper mapper;

    public SensitiveWordRepositoryImpl(SensitiveWordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<SensitiveWord> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public SensitiveWord findByWord(String word) {
        return mapper.selectOne(
            new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getWord, word)
        );
    }

    @Override
    public void insert(SensitiveWord word) {
        mapper.insert(word);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public void batchInsert(List<SensitiveWord> words) {
        for (SensitiveWord w : words) {
            mapper.insert(w);
        }
    }
}
