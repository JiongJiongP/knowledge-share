-- ============================================================
-- V12: Optimize content list count query performance
-- ============================================================

-- Drop useless low-cardinality index (99% of rows have is_deleted=0)
-- This index confuses the MySQL optimizer for COUNT queries
ALTER TABLE knowledge_content DROP INDEX idx_is_deleted;

-- Replace idx_status_published with an index that includes is_deleted
-- so COUNT queries can use index-only scan (no table access)
ALTER TABLE knowledge_content DROP INDEX idx_status_published;
ALTER TABLE knowledge_content ADD INDEX idx_status_del_pub (status, is_deleted, published_at);
