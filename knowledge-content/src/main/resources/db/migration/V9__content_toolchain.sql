CREATE TABLE IF NOT EXISTS `content_version` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  version_number INT NOT NULL,
  title VARCHAR(256) NOT NULL,
  body LONGTEXT,
  change_summary VARCHAR(512),
  created_by BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_content_id (content_id),
  UNIQUE KEY uk_content_version (content_id, version_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `audit_record` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NOT NULL,
  submitter_id BIGINT NOT NULL,
  reviewer_id BIGINT,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  reject_reason VARCHAR(512),
  submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  reviewed_at DATETIME,
  INDEX idx_status (status),
  INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `content_template` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(512),
  content_type VARCHAR(16) NOT NULL DEFAULT 'MARKDOWN',
  body LONGTEXT,
  is_system TINYINT DEFAULT 0,
  created_by BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `scheduled_publish` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  scheduled_at DATETIME NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_scheduled (status, scheduled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
