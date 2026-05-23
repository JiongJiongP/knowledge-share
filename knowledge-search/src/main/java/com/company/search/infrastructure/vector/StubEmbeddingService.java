package com.company.search.infrastructure.vector;

import com.company.search.application.service.EmbeddingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@ConditionalOnMissingBean(name = "bgeEmbeddingService")
public class StubEmbeddingService implements EmbeddingService {

    private static final int DIMENSION = 768;

    @Override
    public float[] embed(String text) {
        if (text == null || text.isEmpty()) {
            return new float[DIMENSION];
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            float[] vec = new float[DIMENSION];
            for (int i = 0; i < DIMENSION; i++) {
                int b0 = hash[i % hash.length] & 0xFF;
                int b1 = hash[(i + 7) % hash.length] & 0xFF;
                vec[i] = ((b0 << 8 | b1) / 65535.0f) * 2.0f - 1.0f;
            }
            // normalize
            float norm = 0;
            for (float v : vec) norm += v * v;
            norm = (float) Math.sqrt(norm);
            if (norm > 0) {
                for (int i = 0; i < DIMENSION; i++) vec[i] /= norm;
            }
            return vec;
        } catch (NoSuchAlgorithmException e) {
            return new float[DIMENSION];
        }
    }

    @Override
    public int dimension() {
        return DIMENSION;
    }
}
