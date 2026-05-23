package com.company.search.application.service;

public interface EmbeddingService {
    float[] embed(String text);
    int dimension();
}
