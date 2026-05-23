CREATE TABLE IF NOT EXISTS `knowledge_content` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(256) NOT NULL,
  body LONGTEXT,
  content_type VARCHAR(16) NOT NULL DEFAULT 'MARKDOWN',
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  created_by BIGINT NOT NULL,
  published_at DATETIME,
  is_deleted TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_created_by (created_by),
  INDEX idx_status_published (status, published_at),
  INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `content_group_relation` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_content_group (content_id, group_id),
  INDEX idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
