CREATE TABLE IF NOT EXISTS `user_action_log` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  action_type VARCHAR(32) NOT NULL,
  target_type VARCHAR(32),
  target_id BIGINT,
  extra_data JSON,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_created (created_at DESC),
  INDEX idx_action (action_type, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `content_stats` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  view_count BIGINT DEFAULT 0,
  favorite_count BIGINT DEFAULT 0,
  comment_count BIGINT DEFAULT 0,
  download_count BIGINT DEFAULT 0,
  stat_date DATE NOT NULL,
  UNIQUE KEY uk_content_date (content_id, stat_date),
  INDEX idx_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `search_hot_keyword` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  keyword VARCHAR(128) NOT NULL,
  search_count INT DEFAULT 1,
  stat_date DATE NOT NULL,
  UNIQUE KEY uk_keyword_date (keyword, stat_date),
  INDEX idx_count (stat_date, search_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
