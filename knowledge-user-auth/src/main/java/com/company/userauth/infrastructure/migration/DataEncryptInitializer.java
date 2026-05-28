package com.company.userauth.infrastructure.migration;

import com.company.common.config.Sm4Config;
import com.company.common.util.SM4Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DataEncryptInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataEncryptInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DataEncryptInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String key;
        try {
            key = Sm4Config.getDataKey();
        } catch (IllegalStateException e) {
            log.warn("SM4 data key not configured, skipping data encryption migration");
            return;
        }

        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id, username, email, display_name FROM user");

        int encrypted = 0;
        int skipped = 0;

        for (Map<String, Object> row : users) {
            Long id = (Long) row.get("id");
            String username = (String) row.get("username");
            String email = (String) row.get("email");
            String displayName = (String) row.get("display_name");

            if (isEncrypted(username, key)) {
                skipped++;
                continue;
            }

            try {
                String encUsername = SM4Util.encryptDeterministic(username, key);
                String encEmail = email != null ? SM4Util.encryptDeterministic(email, key) : null;
                String encDisplayName = displayName != null
                        ? SM4Util.encrypt(displayName, key) : null;

                jdbcTemplate.update(
                        "UPDATE user SET username = ?, email = ?, display_name = ? WHERE id = ?",
                        encUsername, encEmail, encDisplayName, id);
                encrypted++;
            } catch (Exception e) {
                log.error("Failed to encrypt user id={}: {}", id, e.getMessage());
            }
        }

        if (encrypted > 0) {
            log.info("SM4 encrypted {} user records ({} already encrypted)", encrypted, skipped);
        } else if (skipped > 0) {
            log.info("All {} user records already encrypted", skipped);
        }
    }

    private boolean isEncrypted(String value, String key) {
        if (value == null) return true;
        try {
            SM4Util.decryptDeterministic(value, key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
