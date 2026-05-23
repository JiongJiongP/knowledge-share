CREATE TABLE IF NOT EXISTS `comment` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  parent_id BIGINT,
  reply_to_id BIGINT,
  reply_to_user_id BIGINT,
  user_id BIGINT NOT NULL,
  body TEXT NOT NULL,
  like_count INT DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
  audit_status VARCHAR(16) NOT NULL DEFAULT 'APPROVED',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_content_id (content_id),
  INDEX idx_parent_id (parent_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comment_like` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_comment_user (comment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comment_mention` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  comment_id BIGINT NOT NULL,
  mentioned_user_id BIGINT NOT NULL,
  INDEX idx_comment_id (comment_id),
  INDEX idx_mentioned_user (mentioned_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
