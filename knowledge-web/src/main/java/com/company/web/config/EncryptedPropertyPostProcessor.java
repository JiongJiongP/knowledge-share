package com.company.web.config;

import com.company.common.util.SM4Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EncryptedPropertyPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(EncryptedPropertyPostProcessor.class);
    private static final Pattern SM4_PATTERN = Pattern.compile("^SM4\\((.+)\\)$");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String sm4Key = resolveSm4Key();
        if (sm4Key == null || sm4Key.isBlank()) {
            log.warn("SM4_KEY not set — encrypted properties will not be decrypted. "
                    + "Set SM4_KEY environment variable or create ~/.knowledge-secret.key file.");
            return;
        }

        Map<String, Object> decrypted = new HashMap<>();
        MutablePropertySources sources = environment.getPropertySources();

        sources.forEach(source -> {
            if (source instanceof MapPropertySource mapSource) {
                for (String key : mapSource.getPropertyNames()) {
                    Object value = mapSource.getProperty(key);
                    if (value instanceof String str) {
                        Matcher m = SM4_PATTERN.matcher(str.trim());
                        if (m.matches()) {
                            try {
                                String plaintext = SM4Util.decrypt(m.group(1), sm4Key);
                                decrypted.put(key, plaintext);
                                log.debug("Decrypted SM4 property: {}", key);
                            } catch (Exception e) {
                                log.error("Failed to decrypt SM4 property '{}': {}", key, e.getMessage());
                            }
                        }
                    }
                }
            }
        });

        if (!decrypted.isEmpty()) {
            sources.addFirst(new MapPropertySource("sm4-decrypted", decrypted));
            log.info("Decrypted {} SM4-encrypted properties", decrypted.size());
        }
    }

    private String resolveSm4Key() {
        // 1. Environment variable
        String key = System.getenv("SM4_KEY");
        if (key != null && !key.isBlank()) return key.trim();

        // 2. File ~/.knowledge-secret.key
        try {
            Path keyFile = Path.of(System.getProperty("user.home"), ".knowledge-secret.key");
            if (Files.exists(keyFile)) {
                key = Files.readString(keyFile).trim();
                if (!key.isBlank()) return key;
            }
        } catch (Exception ignored) {}

        return null;
    }
}
