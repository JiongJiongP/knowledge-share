-- ============================================================
-- V11: Index optimization for homepage and common queries
-- ============================================================

-- 1. StatsService: count today's content (WHERE created_at >= today)
ALTER TABLE knowledge_content ADD INDEX idx_created_at (created_at);

-- 2. ContentService: list published with type filter + sort
--    (WHERE status = 'PUBLISHED' AND content_type = ? ORDER BY published_at DESC)
ALTER TABLE knowledge_content ADD INDEX idx_status_type_pub (status, content_type, published_at);

-- 3. GroupService: list public approved groups
--    (WHERE visibility = 'PUBLIC' AND status = 'APPROVED')
ALTER TABLE group_info ADD INDEX idx_visibility_status (visibility, status);

-- 4. TodoService: count pending members in owned groups
--    (WHERE group_id IN (..) AND status = 'PENDING')
ALTER TABLE group_member ADD INDEX idx_group_status (group_id, status);
