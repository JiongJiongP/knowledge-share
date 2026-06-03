package com.company.web.data;

import com.company.search.application.service.SearchService;
import com.company.search.application.service.VectorSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ConditionalOnProperty(name = "datagenerator.enabled", havingValue = "true")
public class DataGenerator implements org.springframework.boot.CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataGenerator.class);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // --- Scale knobs ---
    private static final int USER_COUNT = 1_000;
    private static final int TAG_COUNT = 50;
    private static final int CONTENT_COUNT = 500_000;
    private static final int GROUP_COUNT = 30;
    private static final int GROUP_MEMBER_COUNT = 50_000;
    private static final int COMMENT_COUNT = 2_000_000;
    private static final int FAVORITE_COUNT = 500_000;
    private static final int STATS_COUNT = 500_000;
    private static final int NOTIFICATION_COUNT = 1_000_000;
    private static final int ACTION_LOG_COUNT = 2_000_000;
    private static final int HOT_KEYWORD_COUNT = 500;

    private static final int BATCH = 500;
    private static final int LARGE_BATCH = 1000;

    private final JdbcTemplate jdbc;
    private final SearchService searchService;
    private final VectorSearchService vectorSearchService;

    public DataGenerator(JdbcTemplate jdbc, SearchService searchService, VectorSearchService vectorSearchService) {
        this.jdbc = jdbc;
        this.searchService = searchService;
        this.vectorSearchService = vectorSearchService;
    }

    // ================================================================
    //  Entry point
    // ================================================================

    @Override
    public void run(String... args) {
        log.info("============================================");
        log.info("  DataGenerator starting (target: {} contents, ~7.5M total rows)", CONTENT_COUNT);
        log.info("============================================");

        long t0 = System.currentTimeMillis();

        // Phase 0 – prerequisites
        phase0_prerequisites();

        // Check if data already generated
        Integer existingContent = jdbc.queryForObject(
                "SELECT COUNT(*) FROM knowledge_content", Integer.class);
        if (existingContent != null && existingContent >= CONTENT_COUNT) {
            log.info("  Data already generated ({} content rows). Exiting.", existingContent);
            return;
        }

        // Phase 1 – Tags
        List<Long> tagIds = phase1_tags();
        // Phase 2 – Users
        List<Long> userIds = phase2_users();
        // Phase 3 – Groups
        List<Long> groupIds = phase3_groups(userIds);
        // Phase 4 – Group members
        phase4_groupMembers(groupIds, userIds);
        // Phase 5 – Content (the big one) + tag relations + ES/Qdrant
        phase5_content(userIds, tagIds);
        // Phase 6 – Comments
        phase6_comments(userIds);
        // Phase 7 – Favorites
        phase7_favorites(userIds);
        // Phase 8 – Content stats
        phase8_contentStats();
        // Phase 9 – Notifications
        phase9_notifications(userIds);
        // Phase 10 – User action logs
        phase10_actionLogs(userIds);
        // Phase 11 – Search hot keywords
        phase11_hotKeywords();

        long elapsed = (System.currentTimeMillis() - t0) / 1000;
        log.info("============================================");
        log.info("  DataGenerator finished in {} s", elapsed);
        log.info("============================================");
    }

    // ================================================================
    //  Phase 0 – Prerequisites
    // ================================================================

    private void phase0_prerequisites() {
        log.info("[Phase 0] Checking prerequisites...");
        Integer userCnt = jdbc.queryForObject("SELECT COUNT(*) FROM user", Integer.class);
        if (userCnt == null || userCnt < 2) {
            log.warn("  Fewer than 2 users in DB; seed data may be missing. Continuing anyway.");
        }
        try {
            searchService.createIndexIfNotExists();
            log.info("  ES index ready.");
        } catch (Exception e) {
            log.warn("  ES not reachable – will skip ES indexing. {}", e.getMessage());
        }
        try {
            vectorSearchService.createCollectionIfNotExists();
            log.info("  Qdrant collection ready.");
        } catch (Exception e) {
            log.warn("  Qdrant not reachable – will skip vector indexing. {}", e.getMessage());
        }
    }

    // ================================================================
    //  Phase 1 – Tags (50)
    // ================================================================

    private List<Long> phase1_tags() {
        log.info("[Phase 1] Generating {} tags...", TAG_COUNT);

        List<String> tagNames = Arrays.asList(
                "Java", "Spring Boot", "微服务", "分布式", "Redis", "MySQL", "消息队列", "Kubernetes",
                "Docker", "CI/CD", "DevOps", "前端开发", "React", "Vue", "TypeScript", "JavaScript",
                "CSS", "Node.js", "Python", "Go", "Rust", "机器学习", "深度学习", "NLP", "大语言模型",
                "RAG", "向量数据库", "Elasticsearch", "系统设计", "架构", "DDD", "设计模式", "重构",
                "代码质量", "单元测试", "自动化测试", "性能优化", "安全", "网络协议", "操作系统",
                "数据结构", "算法", "数据库", "数据仓库", "数据分析", "项目管理", "敏捷开发",
                "产品设计", "职场成长", "面试"
        );

        List<Long> ids = new ArrayList<>();
        String sql = "INSERT IGNORE INTO tag (name, color, created_by, created_at, updated_at) VALUES (?, ?, 1, NOW(), NOW())";
        String[] colors = {"#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399", "#B37FEB",
                "#36CFC9", "#FF85C0", "#FFC069", "#95DE64"};

        jdbc.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                ps.setString(1, tagNames.get(i));
                ps.setString(2, colors[i % colors.length]);
            }

            @Override
            public int getBatchSize() { return tagNames.size(); }
        });

        // Read back IDs
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT id FROM tag ORDER BY id");
        for (Map<String, Object> row : rows) {
            ids.add(((Number) row.get("id")).longValue());
        }
        log.info("  Tags done: {} inserted.", ids.size());
        return ids.subList(Math.max(0, ids.size() - TAG_COUNT), ids.size());
    }

    // ================================================================
    //  Phase 2 – Users (1,000)
    // ================================================================

    private static final String[] SURNAMES = {
            "张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴", "徐", "孙", "胡", "朱", "高",
            "林", "何", "郭", "马", "罗", "梁", "宋", "郑", "谢", "韩", "唐", "冯", "于", "董", "萧"
    };
    private static final String[] GIVEN = {
            "伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "洋", "勇", "艳", "杰", "军",
            "涛", "明", "超", "秀兰", "霞", "平", "刚", "桂英", "文", "华", "建国", "建军", "宇",
            "鑫", "浩然", "子涵", "欣怡", "梓轩", "雨桐", "一鸣", "诗涵", "思远", "博文", "晨曦"
    };

    private List<Long> phase2_users() {
        log.info("[Phase 2] Generating {} users...", USER_COUNT);

        // Get existing max id
        Long maxId = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM user", Long.class);
        long startId = (maxId == null ? 0 : maxId) + 1;

        String pwHash = "$2a$10$KkrV5Vq1WZ.06JZGEar3Jec2SHzZ70tFyldyY1wZWh4cJZmzPW4cu";
        String sql = "INSERT IGNORE INTO user (id, username, password, display_name, email, department_id, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', NOW(), NOW())";

        jdbc.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                long uid = startId + i;
                String surname = SURNAMES[ThreadLocalRandom.current().nextInt(SURNAMES.length)];
                String given = GIVEN[ThreadLocalRandom.current().nextInt(GIVEN.length)];
                ps.setLong(1, uid);
                ps.setString(2, "user_" + (i + 1));
                ps.setString(3, pwHash);
                ps.setString(4, surname + given);
                ps.setString(5, "user_" + (i + 1) + "@example.com");
                ps.setLong(6, ThreadLocalRandom.current().nextInt(5) + 1);
            }

            @Override
            public int getBatchSize() { return USER_COUNT; }
        });

        List<Long> ids = new ArrayList<>();
        for (long id = startId; id < startId + USER_COUNT; id++) {
            ids.add(id);
        }
        log.info("  Users done: {} inserted (id {} – {}).", ids.size(), startId, startId + USER_COUNT - 1);
        return ids;
    }

    // ================================================================
    //  Phase 3 – Groups (30)
    // ================================================================

    private static final String[][] GROUP_DEFS = {
            {"Java技术交流群", "Java核心技术讨论、Spring框架实战经验分享"},
            {"微服务架构研讨", "微服务设计模式、分布式系统架构深度交流"},
            {"前端技术前沿", "React/Vue/Angular 等现代前端框架技术探讨"},
            {"DevOps实践社区", "CI/CD、容器化、监控告警等运维开发实践"},
            {"AI与机器学习", "深度学习、NLP、大模型技术交流与论文研讨"},
            {"Go语言爱好者", "Go语言高性能编程、云原生开发"},
            {"Python数据科学", "Python数据分析、机器学习、爬虫技术"},
            {"数据库技术圈", "MySQL/PostgreSQL/MongoDB 数据库优化与管理"},
            {"云计算与K8s", "Kubernetes容器编排、服务网格、云原生"},
            {"系统设计训练营", "系统设计面试准备、架构案例讨论"},
            {"算法与数据结构", "LeetCode刷题、算法竞赛、面试准备"},
            {"产品经理之家", "产品设计方法论、需求分析、项目管理"},
            {"测试技术社区", "自动化测试、性能测试、测试策略"},
            {"移动开发技术", "Android/iOS/Flutter/React Native 移动端开发"},
            {"Rust编程语言", "Rust语言学习、系统编程、WASM开发"},
            {"大数据技术栈", "Hadoop/Spark/Flink 大数据处理框架"},
            {"安全技术圈", "Web安全、渗透测试、安全架构设计"},
            {"开源项目贡献者", "开源项目维护、开源社区参与、技术布道"},
            {"技术写作与博客", "技术文章写作技巧、知识分享方法论"},
            {"职场成长与规划", "程序员职业发展、技术管理、面试经验"},
            {"Node.js全栈开发", "Node.js/Express/NestJS 服务端开发"},
            {"推荐系统与搜索", "推荐算法、搜索引擎、排序模型"},
            {"低代码与效能平台", "低代码平台、研发效能度量与提升"},
            {"音视频技术", "WebRTC、直播、音视频编解码技术"},
            {"游戏开发技术", "Unity/Unreal 游戏引擎、游戏服务器架构"},
            {"IoT物联网", "物联网设备接入、边缘计算、协议解析"},
            {"可观测性技术", "日志、指标、链路追踪、eBPF"},
            {"Serverless架构", "函数计算、Serverless应用设计与优化"},
            {"技术管理圈", "技术团队管理、OKR、技术战略规划"},
            {"知识图谱", "知识图谱构建、图数据库、语义搜索"}
    };

    private List<Long> phase3_groups(List<Long> userIds) {
        log.info("[Phase 3] Generating {} groups...", GROUP_COUNT);

        List<Long> ids = new ArrayList<>();
        String sql = "INSERT IGNORE INTO group_info (name, description, owner_id, visibility, status, is_deleted, created_at, updated_at) VALUES (?, ?, ?, ?, 'APPROVED', 0, NOW(), NOW())";

        jdbc.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                ps.setString(1, GROUP_DEFS[i][0]);
                ps.setString(2, GROUP_DEFS[i][1]);
                ps.setLong(3, userIds.get(ThreadLocalRandom.current().nextInt(Math.min(100, userIds.size()))));
                ps.setString(4, ThreadLocalRandom.current().nextDouble() < 0.8 ? "PUBLIC" : "PRIVATE");
            }

            @Override
            public int getBatchSize() { return GROUP_DEFS.length; }
        });

        List<Map<String, Object>> rows = jdbc.queryForList("SELECT id FROM group_info ORDER BY id DESC LIMIT " + GROUP_COUNT);
        for (int i = rows.size() - 1; i >= 0; i--) {
            ids.add(((Number) rows.get(i).get("id")).longValue());
        }
        log.info("  Groups done: {} inserted.", ids.size());
        return ids;
    }

    // ================================================================
    //  Phase 4 – Group Members (50,000)
    // ================================================================

    private void phase4_groupMembers(List<Long> groupIds, List<Long> userIds) {
        log.info("[Phase 4] Generating {} group members...", GROUP_MEMBER_COUNT);
        String sql = "INSERT IGNORE INTO group_member (group_id, user_id, role, status, joined_at) VALUES (?, ?, 'MEMBER', 'APPROVED', ?)";
        String now = LocalDateTime.now().format(DT_FMT);

        int inserted = 0;
        for (Long gid : groupIds) {
            int membersPerGroup = ThreadLocalRandom.current().nextInt(500, 5000);
            List<Long> picked = pickRandom(userIds, membersPerGroup);
            for (Long uid : picked) {
                jdbc.update(sql, gid, uid, now);
                inserted++;
            }
            if (inserted % 5000 == 0) {
                log.info("  Group members progress: {}/{}", inserted, GROUP_MEMBER_COUNT);
            }
            if (inserted >= GROUP_MEMBER_COUNT) break;
        }
        log.info("  Group members done: {} inserted.", inserted);
    }

    // ================================================================
    //  Phase 5 – Content (500,000) + Tag Relations (~1M) + ES + Qdrant
    // ================================================================

    private void phase5_content(List<Long> userIds, List<Long> tagIds) {
        log.info("[Phase 5] Generating {} content articles + tag relations + ES/Qdrant indexing...", CONTENT_COUNT);

        // Pre-allocate content ID range
        Long maxId = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM knowledge_content", Long.class);
        AtomicLong nextContentId = new AtomicLong((maxId == null ? 0 : maxId) + 1);

        String contentSql = "INSERT IGNORE INTO knowledge_content (id, title, body, content_type, status, created_by, published_at, is_deleted, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?)";
        String relationSql = "INSERT IGNORE INTO content_tag_relation (content_id, tag_id) VALUES (?, ?)";

        int esIndexed = 0;
        int qdrantUpserted = 0;
        LocalDateTime baseTime = LocalDateTime.now().minusDays(90);

        for (int batch = 0; batch < CONTENT_COUNT; batch += BATCH) {
            int batchSize = Math.min(BATCH, CONTENT_COUNT - batch);

            // Build batch data
            List<Long> batchContentIds = new ArrayList<>(batchSize);
            List<String> batchTitles = new ArrayList<>(batchSize);
            List<String> batchBodies = new ArrayList<>(batchSize);
            List<String> batchContentTypes = new ArrayList<>(batchSize);
            List<String> batchCreatedBys = new ArrayList<>(batchSize);
            List<String> batchPublishedAts = new ArrayList<>(batchSize);

            List<long[]> batchRelations = new ArrayList<>(); // pairs of [contentId, tagId]

            for (int i = 0; i < batchSize; i++) {
                long cid = nextContentId.getAndIncrement();
                String title = makeTitle();
                String body = makeBody();
                String contentType = pickContentType();
                boolean published = ThreadLocalRandom.current().nextDouble() < 0.9;
                long authorId = userIds.get(ThreadLocalRandom.current().nextInt(userIds.size()));
                LocalDateTime publishedAt = published ? randTime(baseTime) : null;

                batchContentIds.add(cid);
                batchTitles.add(title);
                batchBodies.add(body);
                batchContentTypes.add(contentType);
                batchCreatedBys.add(String.valueOf(authorId));
                batchPublishedAts.add(published ? publishedAt.format(DT_FMT) : null);

                // Tag relations: 1-4 tags per content
                int nTags = ThreadLocalRandom.current().nextInt(1, 5);
                List<Long> picked = pickRandom(tagIds, nTags);
                for (Long tid : picked) {
                    batchRelations.add(new long[]{cid, tid});
                }
            }

            // INSERT content batch
            LocalDateTime now = LocalDateTime.now();
            String nowStr = now.format(DT_FMT);
            jdbc.batchUpdate(contentSql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    ps.setLong(1, batchContentIds.get(i));
                    ps.setString(2, batchTitles.get(i));
                    ps.setString(3, batchBodies.get(i));
                    ps.setString(4, batchContentTypes.get(i));
                    ps.setString(5, batchPublishedAts.get(i) != null ? "PUBLISHED" : "DRAFT");
                    ps.setLong(6, Long.parseLong(batchCreatedBys.get(i)));
                    ps.setString(7, batchPublishedAts.get(i));
                    ps.setString(8, nowStr);
                    ps.setString(9, nowStr);
                }

                @Override
                public int getBatchSize() { return batchSize; }
            });

            // INSERT tag relations
            jdbc.batchUpdate(relationSql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    ps.setLong(1, batchRelations.get(i)[0]);
                    ps.setLong(2, batchRelations.get(i)[1]);
                }

                @Override
                public int getBatchSize() { return batchRelations.size(); }
            });

            // Index to ES (published only)
            List<Long> pubIds = new ArrayList<>();
            List<String> pubTitles = new ArrayList<>();
            List<String> pubBodies = new ArrayList<>();
            List<String> pubTypes = new ArrayList<>();
            List<String> pubStatuses = new ArrayList<>();
            List<String> pubAuthors = new ArrayList<>();
            List<String> pubDates = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                if (batchPublishedAts.get(i) != null) {
                    pubIds.add(batchContentIds.get(i));
                    pubTitles.add(batchTitles.get(i));
                    pubBodies.add(batchBodies.get(i));
                    pubTypes.add(batchContentTypes.get(i));
                    pubStatuses.add("PUBLISHED");
                    pubAuthors.add(batchCreatedBys.get(i));
                    pubDates.add(batchPublishedAts.get(i));
                }
            }
            if (!pubIds.isEmpty()) {
                try {
                    searchService.batchIndex(pubIds, pubTitles, pubBodies, pubTypes, pubStatuses, pubAuthors, pubDates);
                    esIndexed += pubIds.size();
                } catch (Exception e) {
                    log.warn("  ES batch index failed at content batch {}: {}", batch / BATCH, e.getMessage());
                }
            }

            // Index to Qdrant (published only)
            if (!pubIds.isEmpty()) {
                try {
                    vectorSearchService.batchUpsert(pubIds, pubTitles, pubBodies);
                    qdrantUpserted += pubIds.size();
                } catch (Exception e) {
                    log.warn("  Qdrant batch upsert failed at content batch {}: {}", batch / BATCH, e.getMessage());
                }
            }

            if ((batch + BATCH) % 50000 == 0 || batch + batchSize >= CONTENT_COUNT) {
                log.info("  Content: {}/{} | ES: {} | Qdrant: {}",
                        batch + batchSize, CONTENT_COUNT, esIndexed, qdrantUpserted);
            }
        }

        log.info("  Content done: {} inserted, {} ES indexed, {} Qdrant upserted.",
                CONTENT_COUNT, esIndexed, qdrantUpserted);
    }

    // ================================================================
    //  Phase 6 – Comments (2,000,000)
    // ================================================================

    private static final String[] COMMENT_TEMPLATES = {
            "写得非常好，受益匪浅！", "学习了，感谢分享。", "这篇文章讲得很透彻，收藏了。",
            "有个问题想请教一下，这个方案在生产环境中稳定性如何？", "总结得很到位，特别是关于性能优化的部分。",
            "之前也遇到过类似的问题，当时我们采用的方案是...", "思路很清晰，代码示例也很实用。",
            "Mark一下，回头仔细研究。", "想问一下有没有相关的开源项目可以参考？",
            "这个方案相比于业界常见的做法有什么优势吗？", "写得不错，不过有个小建议...",
            "深有同感！在实际项目中也踩过类似的坑。", "有没有考虑过使用XXX替代方案？",
            "感谢作者的分享，对新手很有帮助。", "请问这个适用什么版本的？",
            "期待下一篇！", "干货满满，已转发到团队群。", "这个知识点面试经常会被问到。",
            "有没有对应的视频教程推荐？", "请问有完整的示例代码吗？",
            "最近正好在调研这方面的技术，非常及时！", "请问有没有对比过不同的技术方案？",
            "这篇文章解决了我一直以来的困惑，非常感谢！", "大佬牛逼！",
            "请问这个对硬件配置有什么要求吗？", "想问下具体的实施步骤有没有更详细的说明？",
            "我们公司也在推这个，效果确实不错。", "已star，期待更多更新。",
            "讲得太好了，比看官方文档清晰多了。", "这么好的文章为什么没人看？",
            "请教一下，如果并发量很大的话，这个方案要怎么调优？", "先收藏，等有时间了认真学习。",
            "写得挺好的，但是有几个地方不太明白...", "赞一个！", "值得反复阅读的好文章。",
            "可以配合XXX一起使用，效果更好。", "这个问题我们之前讨论过，结论是..."
    };

    private void phase6_comments(List<Long> userIds) {
        log.info("[Phase 6] Generating {} comments...", COMMENT_COUNT);
        String sql = "INSERT INTO comment (content_id, parent_id, user_id, body, like_count, status, audit_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, 'PUBLISHED', 'APPROVED', ?, ?)";

        // Content IDs range determined by earlier phase
        Long maxCid = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM knowledge_content", Long.class);
        Long minCid = maxCid - CONTENT_COUNT + 1;
        if (minCid < 1) minCid = 1L;

        int inserted = 0;
        int batchSize = LARGE_BATCH;
        LocalDateTime now = LocalDateTime.now();

        for (int contentIdx = 0; contentIdx < CONTENT_COUNT && inserted < COMMENT_COUNT; contentIdx++) {
            long contentId = minCid + contentIdx;
            int nComments = ThreadLocalRandom.current().nextInt(0, 9); // 0-8 comments per content
            if (nComments == 0) continue;

            List<Object[]> batch = new ArrayList<>(nComments);
            for (int c = 0; c < nComments && inserted < COMMENT_COUNT; c++) {
                String body = COMMENT_TEMPLATES[ThreadLocalRandom.current().nextInt(COMMENT_TEMPLATES.length)];
                long userId = userIds.get(ThreadLocalRandom.current().nextInt(userIds.size()));
                int likeCount = ThreadLocalRandom.current().nextInt(0, 51);
                LocalDateTime commentTime = now.minusDays(ThreadLocalRandom.current().nextInt(90))
                        .minusHours(ThreadLocalRandom.current().nextInt(24));
                String ts = commentTime.format(DT_FMT);
                batch.add(new Object[]{contentId, null, userId, body, likeCount, ts, ts});
            }

            jdbc.batchUpdate(sql, batch, batchSize, (ps, args) -> {
                ps.setLong(1, (Long) args[0]);
                ps.setObject(2, args[1]);
                ps.setLong(3, (Long) args[2]);
                ps.setString(4, (String) args[3]);
                ps.setInt(5, (Integer) args[4]);
                ps.setString(6, (String) args[5]);
                ps.setString(7, (String) args[6]);
            });
            inserted += batch.size();

            if (inserted % 100_000 == 0) {
                log.info("  Comments: {}/{}", inserted, COMMENT_COUNT);
            }
        }
        log.info("  Comments done: {} inserted.", inserted);
    }

    // ================================================================
    //  Phase 7 – Favorites (500,000)
    // ================================================================

    private void phase7_favorites(List<Long> userIds) {
        log.info("[Phase 7] Generating {} favorites...", FAVORITE_COUNT);
        String sql = "INSERT IGNORE INTO favorite (user_id, content_id, created_at) VALUES (?, ?, ?)";

        Long maxCid = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM knowledge_content", Long.class);
        Long minCid = maxCid - CONTENT_COUNT + 1;
        if (minCid < 1) minCid = 1L;

        int inserted = 0;
        int batchSize = LARGE_BATCH;
        LocalDateTime base = LocalDateTime.now().minusDays(90);

        while (inserted < FAVORITE_COUNT) {
            List<Object[]> batch = new ArrayList<>(batchSize);
            int remaining = FAVORITE_COUNT - inserted;
            int sz = Math.min(batchSize, remaining);
            for (int i = 0; i < sz; i++) {
                long uid = userIds.get(ThreadLocalRandom.current().nextInt(userIds.size()));
                long cid = minCid + ThreadLocalRandom.current().nextLong(CONTENT_COUNT);
                String ts = randTime(base).format(DT_FMT);
                batch.add(new Object[]{uid, cid, ts});
            }
            jdbc.batchUpdate(sql, batch, batchSize, (ps, args) -> {
                ps.setLong(1, (Long) args[0]);
                ps.setLong(2, (Long) args[1]);
                ps.setString(3, (String) args[2]);
            });
            inserted += sz;
            if (inserted % 100_000 == 0) {
                log.info("  Favorites: {}/{}", inserted, FAVORITE_COUNT);
            }
        }
        log.info("  Favorites done: {} rows (actual unique depends on IGNORE).", inserted);
    }

    // ================================================================
    //  Phase 8 – Content Stats (500,000)
    // ================================================================

    private void phase8_contentStats() {
        log.info("[Phase 8] Generating {} content stats...", STATS_COUNT);
        String sql = "INSERT IGNORE INTO content_stats (content_id, view_count, favorite_count, comment_count, download_count, stat_date) VALUES (?, ?, ?, ?, ?, ?)";

        Long maxCid = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM knowledge_content", Long.class);
        Long minCid = maxCid - CONTENT_COUNT + 1;
        if (minCid < 1) minCid = 1L;

        int inserted = 0;
        int batchSize = LARGE_BATCH;
        String today = LocalDate.now().format(DATE_FMT);

        for (long cid = minCid; cid <= maxCid && inserted < STATS_COUNT; cid++) {
            List<Object[]> batch = new ArrayList<>(batchSize);
            int sz = Math.min(batchSize, (int) (maxCid - cid + 1));
            for (int i = 0; i < sz; i++) {
                long currCid = cid + i;
                batch.add(new Object[]{
                        currCid,
                        ThreadLocalRandom.current().nextInt(0, 5001),   // view_count
                        ThreadLocalRandom.current().nextInt(0, 201),    // favorite_count
                        ThreadLocalRandom.current().nextInt(0, 51),     // comment_count
                        ThreadLocalRandom.current().nextInt(0, 101),    // download_count
                        today
                });
            }
            jdbc.batchUpdate(sql, batch, batchSize, (ps, args) -> {
                ps.setLong(1, (Long) args[0]);
                ps.setInt(2, (Integer) args[1]);
                ps.setInt(3, (Integer) args[2]);
                ps.setInt(4, (Integer) args[3]);
                ps.setInt(5, (Integer) args[4]);
                ps.setString(6, (String) args[5]);
            });
            inserted += sz;
            cid += sz - 1;
            if (inserted % 100_000 == 0) {
                log.info("  Content stats: {}/{}", inserted, STATS_COUNT);
            }
        }
        log.info("  Content stats done: {} inserted.", inserted);
    }

    // ================================================================
    //  Phase 9 – Notifications (1,000,000)
    // ================================================================

    private void phase9_notifications(List<Long> userIds) {
        log.info("[Phase 9] Generating {} notifications...", NOTIFICATION_COUNT);
        String sql = "INSERT INTO notification (user_id, type, title, content, related_id, related_type, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String[] types = {"SYSTEM", "COMMENT", "LIKE", "GROUP_INVITE", "CONTENT_APPROVED"};
        String[] titles = {
                "系统通知", "新评论提醒", "收到点赞", "群组邀请",
                "内容审核通过", "你的文章有新评论", "有人收藏了你的文章",
                "群组申请已通过", "内容发布成功"
        };
        String[] contents = {
                "欢迎使用知识分享平台！", "有用户评论了你的文章。", "你的评论收到了新的点赞。",
                "你被邀请加入技术交流群。", "你提交的内容已通过审核。", "你的文章《系统设计指南》收到了新评论。",
                "用户收藏了你的文章。", "你加入群组的申请已被批准。", "你发布的内容已成功上线。"
        };

        Long maxCid = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM knowledge_content", Long.class);
        Long minCid = maxCid - CONTENT_COUNT + 1;
        if (minCid < 1) minCid = 1L;

        int inserted = 0;
        int batchSize = LARGE_BATCH;
        LocalDateTime base = LocalDateTime.now().minusDays(90);

        while (inserted < NOTIFICATION_COUNT) {
            List<Object[]> batch = new ArrayList<>(batchSize);
            int remaining = NOTIFICATION_COUNT - inserted;
            int sz = Math.min(batchSize, remaining);
            for (int i = 0; i < sz; i++) {
                long uid = userIds.get(ThreadLocalRandom.current().nextInt(userIds.size()));
                String type = types[ThreadLocalRandom.current().nextInt(types.length)];
                String title = titles[ThreadLocalRandom.current().nextInt(titles.length)];
                String content = contents[ThreadLocalRandom.current().nextInt(contents.length)];
                long relatedId = minCid + ThreadLocalRandom.current().nextLong(CONTENT_COUNT);
                int isRead = ThreadLocalRandom.current().nextDouble() < 0.4 ? 1 : 0;
                String ts = randTime(base).format(DT_FMT);
                batch.add(new Object[]{uid, type, title, content, relatedId, "CONTENT", isRead, ts});
            }
            jdbc.batchUpdate(sql, batch, batchSize, (ps, args) -> {
                ps.setLong(1, (Long) args[0]);
                ps.setString(2, (String) args[1]);
                ps.setString(3, (String) args[2]);
                ps.setString(4, (String) args[3]);
                ps.setLong(5, (Long) args[4]);
                ps.setString(6, (String) args[5]);
                ps.setInt(7, (Integer) args[6]);
                ps.setString(8, (String) args[7]);
            });
            inserted += sz;
            if (inserted % 100_000 == 0) {
                log.info("  Notifications: {}/{}", inserted, NOTIFICATION_COUNT);
            }
        }
        log.info("  Notifications done: {} inserted.", inserted);
    }

    // ================================================================
    //  Phase 10 – User Action Logs (2,000,000)
    // ================================================================

    private void phase10_actionLogs(List<Long> userIds) {
        log.info("[Phase 10] Generating {} user action logs...", ACTION_LOG_COUNT);
        String sql = "INSERT INTO user_action_log (user_id, action_type, target_type, target_id, extra_data, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        String[] actionTypes = {"VIEW", "CREATE", "UPDATE", "DELETE", "PUBLISH", "SEARCH", "LOGIN", "DOWNLOAD"};
        String[] targetTypes = {"CONTENT", "COMMENT", "GROUP", "USER"};
        String[] extraDatas = {
                "{\"source\":\"homepage\"}", "{\"source\":\"search\"}", "{\"source\":\"recommend\"}",
                "{\"source\":\"group_page\"}", "{\"source\":\"favorites\"}", "{\"source\":\"notification\"}",
                "{\"keyword\":\"微服务\"}", "{\"keyword\":\"Spring Boot\"}", "{\"keyword\":\"Redis\"}",
                "{\"keyword\":\"Kubernetes\"}", "{\"keyword\":\"数据库优化\"}", "{\"keyword\":\"系统设计\"}"
        };

        Long maxCid = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM knowledge_content", Long.class);
        Long minCid = maxCid - CONTENT_COUNT + 1;
        if (minCid < 1) minCid = 1L;

        int inserted = 0;
        int batchSize = LARGE_BATCH;
        LocalDateTime base = LocalDateTime.now().minusDays(90);

        while (inserted < ACTION_LOG_COUNT) {
            List<Object[]> batch = new ArrayList<>(batchSize);
            int remaining = ACTION_LOG_COUNT - inserted;
            int sz = Math.min(batchSize, remaining);
            for (int i = 0; i < sz; i++) {
                long uid = userIds.get(ThreadLocalRandom.current().nextInt(userIds.size()));
                String actionType = actionTypes[ThreadLocalRandom.current().nextInt(actionTypes.length)];
                String targetType = targetTypes[ThreadLocalRandom.current().nextInt(targetTypes.length)];
                long targetId = minCid + ThreadLocalRandom.current().nextLong(CONTENT_COUNT);
                String extra = extraDatas[ThreadLocalRandom.current().nextInt(extraDatas.length)];
                String ts = randTime(base).format(DT_FMT);
                batch.add(new Object[]{uid, actionType, targetType, targetId, extra, ts});
            }
            jdbc.batchUpdate(sql, batch, batchSize, (ps, args) -> {
                ps.setLong(1, (Long) args[0]);
                ps.setString(2, (String) args[1]);
                ps.setString(3, (String) args[2]);
                ps.setLong(4, (Long) args[3]);
                ps.setString(5, (String) args[4]);
                ps.setString(6, (String) args[5]);
            });
            inserted += sz;
            if (inserted % 200_000 == 0) {
                log.info("  Action logs: {}/{}", inserted, ACTION_LOG_COUNT);
            }
        }
        log.info("  Action logs done: {} inserted.", inserted);
    }

    // ================================================================
    //  Phase 11 – Search Hot Keywords (500)
    // ================================================================

    private void phase11_hotKeywords() {
        log.info("[Phase 11] Generating {} search hot keywords...", HOT_KEYWORD_COUNT);
        String sql = "INSERT IGNORE INTO search_hot_keyword (keyword, search_count, stat_date) VALUES (?, ?, ?)";

        String[] keywords = {
                "Java", "Spring Boot", "微服务", "Redis", "MySQL", "Kubernetes", "Docker",
                "Elasticsearch", "机器学习", "深度学习", "大语言模型", "RAG", "向量数据库",
                "React", "Vue", "TypeScript", "Go", "Python", "Rust", "DDD",
                "系统设计", "算法", "性能优化", "分布式事务", "消息队列", "CI/CD",
                "NLP", "数据结构", "设计模式", "面试", "架构", "安全"
        };

        int batchSize = 500;
        List<Object[]> batch = new ArrayList<>(batchSize);

        for (int i = 0; i < HOT_KEYWORD_COUNT; i++) {
            String kw = keywords[i % keywords.length];
            int count = ThreadLocalRandom.current().nextInt(50, 5001);
            String date = LocalDate.now().minusDays(i % 30).format(DATE_FMT);
            batch.add(new Object[]{kw, count, date});
        }

        jdbc.batchUpdate(sql, batch, batchSize, (ps, args) -> {
            ps.setString(1, (String) args[0]);
            ps.setInt(2, (Integer) args[1]);
            ps.setString(3, (String) args[2]);
        });
        log.info("  Search hot keywords done: {} inserted.", HOT_KEYWORD_COUNT);
    }

    // ================================================================
    //  Content generation helpers
    // ================================================================

    private static final List<String> TITLE_TEMPLATES = Arrays.asList(
            "深入理解{term}：从原理到实践",
            "{term}实战指南：构建高性能{term2}系统",
            "{term}最佳实践与性能优化",
            "基于{term}的{term2}解决方案",
            "{term}入门到精通：万字长文总结",
            "大厂都在用的{term}技术方案",
            "面试官必问：{term}核心知识点梳理",
            "从零开始搭建{term}平台",
            "{term}在{term2}场景下的应用实践",
            "系统设计：如何设计一个高可用{term}系统",
            "{term}与{term2}的对比分析：如何选择",
            "{term}进阶之路：从初级到专家的成长指南",
            "手把手教你实现{term}",
            "{term}常见问题排查与解决方案",
            "生产环境中的{term}踩坑记录",
            "{term}在亿级流量场景下的架构演进",
            "详解{term}的核心原理与实现细节",
            "{term}技术选型：为什么我们选择了{term2}",
            "{term}开发规范与代码质量提升",
            "{term}的过去、现在与未来"
    );

    private static final List<String> TERMS = Arrays.asList(
            "微服务", "Spring Cloud", "分布式事务", "消息队列", "Redis缓存",
            "MySQL优化", "JVM", "Kubernetes", "Docker", "CI/CD流水线",
            "React", "Vue", "TypeScript", "Webpack", "前端监控",
            "机器学习", "深度学习", "大语言模型", "RAG", "向量数据库",
            "Elasticsearch", "数据仓库", "实时计算", "数据湖", "流处理",
            "DDD", "CQRS", "事件驱动", "设计模式", "代码重构",
            "API网关", "服务网格", "可观测性", "链路追踪", "日志系统",
            "单元测试", "自动化测试", "安全防护", "性能压测", "容量规划",
            "Go语言", "Rust", "Python", "Node.js", "数据库索引",
            "负载均衡", "限流熔断", "配置中心", "服务注册", "认证授权"
    );

    // ~200 paragraphs across 6 topics
    private static final Map<String, List<String>> PARAGRAPHS = Map.of(
            "backend", Arrays.asList(
                    "在微服务架构设计中，服务拆分是首要考虑的问题。合理的服务粒度能够提升系统的可维护性和可扩展性，但过度拆分又会引入分布式系统的复杂性。通常我们建议按照业务领域边界进行拆分，每个微服务对应一个限界上下文（Bounded Context），这样可以最大程度地减少服务间的耦合度。",
                    "Spring Cloud Alibaba 提供了完整的微服务解决方案，包括 Nacos 服务注册与发现、Sentinel 流量控制、Seata 分布式事务等组件。其中 Nacos 兼具配置中心的功能，可以实现配置的动态刷新而无需重启服务，这在生产环境中非常实用。",
                    "数据库索引优化是提升查询性能的关键手段。合理使用复合索引、覆盖索引和前缀索引可以显著减少查询时间。但索引并非越多越好，每次写入操作都需要维护索引，过量的索引反而会拖慢写入性能。建议根据实际查询场景，有针对性地建立索引。",
                    "JVM 调优是 Java 应用性能优化的核心。通过调整堆内存大小、选择合适的垃圾收集器、优化 GC 参数，可以有效降低应用停顿时间。G1 收集器在低延迟场景下表现优异，而 ZGC 则进一步将停顿时间控制在亚毫秒级别。",
                    "分布式事务的实现通常采用 TCC 模式或 Saga 模式。TCC 通过 Try-Confirm-Cancel 三个阶段来保证事务的原子性，而 Saga 则将长事务拆分为多个本地事务，每个本地事务都有对应的补偿操作。选择合适的分布式事务方案需要考虑业务场景的一致性要求和性能要求。",
                    "使用 Redis 缓存可以大幅降低数据库压力，但需要注意缓存穿透、缓存击穿和缓存雪崩等经典问题。缓存穿透可以使用布隆过滤器来拦截不存在的数据查询；缓存击穿可以通过互斥锁或永不过期来解决；缓存雪崩则需要给过期时间添加随机因子避免集中失效。",
                    "消息队列在系统解耦、流量削峰、异步处理等场景中发挥着重要作用。RocketMQ 支持事务消息，可以保证本地事务和消息发送的原子性，适用于分布式事务场景。Kafka 则以其高吞吐量著称，常用于日志收集和流处理场景。",
                    "MySQL 的主从复制是实现读写分离和数据备份的基础。基于 binlog 的异步复制可能导致主从延迟，而半同步复制则在性能和一致性之间取得了平衡。对于要求强一致性的场景，可以考虑使用 Group Replication 或者直接读写主库。",
                    "连接池的合理配置对应用性能至关重要。HikariCP 是目前性能最好的 Java 连接池，其默认配置已经适合大多数场景。主要需要关注的参数包括最大连接数、最小空闲连接数、连接超时时间和最大生命周期。连接数过多会消耗数据库资源，过少则会导致请求排队。",
                    "接口幂等性是分布式系统中不可忽视的问题。常用的幂等方案包括：基于数据库唯一索引约束、基于 Redis 的分布式锁、基于 Token 的防重机制等。对于支付等金融场景，建议在业务层做严格的幂等控制，避免因为网络重试导致重复扣款。",
                    "分布式锁的实现方式主要有三种：基于数据库的乐观锁、基于 Redis 的 SETNX 命令、以及基于 ZooKeeper 的临时顺序节点。Redis 方案性能最高但可能存在锁失效的风险，ZooKeeper 方案可靠性高但性能稍逊。Redisson 的看门狗机制通过自动续期解决了 Redis 分布式锁的过期问题。",
                    "数据库读写分离可以有效提升系统的并发处理能力。主库负责处理写操作，从库负责处理读操作。但读写分离引入了主从延迟的问题，对于写入后立即读取的场景，需要强制走主库或使用同步复制来保证数据一致性。ShardingSphere 可以透明地实现读写分离功能。",
                    "缓存与数据库的一致性问题是系统设计中的经典难题。Cache Aside 模式是先更新数据库再删除缓存的策略，虽然存在短暂的不一致窗口，但配合延迟双删可以进一步降低不一致的风险。对于要求强一致性的场景，可以考虑使用分布式事务或者直接读写数据库。",
                    "限流是保护系统不被突发流量压垮的重要手段。常用的限流算法包括：固定窗口计数、滑动窗口计数、漏桶算法和令牌桶算法。Sentinel 结合了滑动窗口和令牌桶的优点，支持多种限流规则和控制台实时监控，是微服务架构中首选的限流框架。",
                    "熔断机制是微服务容错的重要一环。当某个服务的失败率达到阈值时，熔断器会打开并快速失败，避免级联故障。Hystrix 虽然已经停止维护，但 Resilience4j 和 Sentinel 都是优秀的替代方案。熔断器通常配合降级策略使用，在服务不可用时返回兜底数据。",
                    "日志系统是排查问题和监控系统健康状态的基础。ELK（Elasticsearch + Logstash + Kibana）技术栈是业界常用的日志解决方案。结构化日志比纯文本日志更容易检索和分析，建议使用 JSON 格式输出日志，并包含 traceId 以便进行全链路追踪。",
                    "API 网关是微服务架构中的统一入口，负责请求路由、认证鉴权、限流熔断、协议转换等功能。Spring Cloud Gateway 基于 WebFlux 实现，性能优于 Zuul，且支持响应式编程。Kong 和 APISIX 基于 OpenResty，在高并发场景下表现优异。",
                    "分布式配置中心解决了微服务多实例的配置管理问题。Nacos 和 Apollo 是国内使用最广泛的配置中心框架。配置中心支持配置的版本管理、灰度发布和一键回滚，大大提升了运维效率。敏感配置如数据库密码等应该使用加密存储或密钥管理服务。",
                    "数据库分库分表是处理大数据量的终极方案。垂直拆分按业务模块将不同的表分布到不同的库中；水平拆分将同一张表的数据按分片键分布到多个库中。分片键的选择直接影响数据分布的均匀性和查询效率，需要根据业务查询模式慎重选择。",
                    "线程池的合理配置对 Java 应用的并发性能至关重要。核心线程数、最大线程数、队列容量和拒绝策略需要根据业务特性进行调整。IO 密集型任务可以配置较多线程，CPU 密集型任务则建议线程数等于 CPU 核心数。建议使用有界队列以避免内存溢出。",
                    "Spring Boot 的自动配置原理是基于 @Conditional 注解和 spring.factories 机制实现的。理解自动配置的原理有助于排查配置冲突和定制化需求。可以通过 --debug 启动参数查看自动配置的匹配情况，用 @ConditionalOnMissingBean 覆盖默认的自动配置。",
                    "MyBatis 和 JPA 是 Java 领域最主流的两种 ORM 框架。MyBatis 更加灵活，适合复杂的查询场景和需要精细 SQL 调优的项目；JPA 则通过面向对象的方式屏蔽了 SQL 细节，适合以 CRUD 为主的项目。MyBatis-Plus 在 MyBatis 基础上提供了更便捷的 CRUD 操作。",
                    "分布式 ID 生成方案需要满足全局唯一、高性能和高可用三个基本要求。雪花算法（Snowflake）是常用的分布式 ID 方案，由 1 位符号位 + 41 位时间戳 + 10 位机器 ID + 12 位序列号组成。美团开源的 Leaf 和百度的 UidGenerator 都是基于雪花算法的改进方案。",
                    "数据库事务隔离级别定义了事务之间的可见性规则。READ COMMITTED 解决了脏读问题，REPEATABLE READ 进一步解决了不可重复读问题，SERIALIZABLE 则完全避免了并发问题但性能最差。MySQL InnoDB 默认的 REPEATABLE READ 通过间隙锁机制实际也解决了幻读问题。",
                    "服务注册与发现是微服务架构的基础设施。Eureka 采用 AP 设计保证可用性，Consul 和 Nacos 则同时支持 AP 和 CP 模式。心跳机制用于检测服务实例的健康状态，当实例下线时注册中心会将其从服务列表中剔除，避免请求发送到不可用的节点。",
                    "系统的高可用架构设计需要从多个层面考虑：应用层通过多实例部署和负载均衡实现冗余；数据层通过主从复制和故障切换保证数据不丢失；基础设施层通过跨机房部署避免单点故障。常见的可用性目标如 99.99%（四个九）意味着全年停机时间不超过 52 分钟。",
                    "OAuth 2.0 是当前最主权的授权协议，定义了四种授权模式：授权码模式、简化模式、密码模式和客户端凭证模式。在微服务安全架构中，通常使用 OAuth 2.0 结合 JWT 实现无状态的认证和授权。JWT Token 中包含了用户身份和权限信息，避免了频繁查询认证服务。",
                    "CDN（内容分发网络）通过将静态资源缓存到离用户最近的边缘节点，可以显著减少网络延迟和源站压力。DNS 解析、缓存策略和回源机制是 CDN 架构的核心要素。对于动态内容，可以使用边缘计算（Edge Computing）在 CDN 节点执行部分业务逻辑来进一步缩短响应时间。",
                    "数据库慢查询是影响系统性能的常见原因。通过开启 MySQL 的慢查询日志并结合 EXPLAIN 分析执行计划，可以定位到需要优化的 SQL。常见的优化手段包括：添加合适的索引、重写不高效的 SQL、减少查询返回的数据量、以及将复杂查询拆分为多个简单查询。",
                    "灰度发布是一种降低发布风险的策略，通过将新版本逐步推送给部分用户来验证新功能的稳定性。实现灰度发布的技术手段包括：基于负载均衡的流量分配、基于 HTTP Header 的路由规则、以及基于服务网格的流量治理。Istio 通过 VirtualService 和 DestinationRule 支持灵活的灰度发布策略。",
                    "全链路压测是验证系统容量和发现性能瓶颈的重要手段。与传统的单接口压测不同，全链路压测模拟真实的用户行为链路，可以更准确地反映系统在真实负载下的表现。压测时需要注意数据隔离、流量标识和监控告警，避免压测数据污染生产环境和影响真实用户。",
                    "Serverless 架构让开发者无需关心服务器的运维，只需关注业务代码的编写。函数计算（FaaS）按实际执行时间计费，对于低频调用的场景非常经济。但 Serverless 也存在冷启动延迟、执行时间限制和状态管理困难等局限性，不适用于所有类型的应用。",
                    "事件驱动架构（EDA）通过异步事件实现服务间的松耦合通信。相比同步调用，事件驱动架构具有更好的可扩展性和容错性。在电商场景中，订单创建后会发布事件触发库存扣减、积分发放、短信通知等一系列后续操作，这些操作之间相互独立，某个环节失败不会影响其他环节。",
                    "领域驱动设计（DDD）强调将业务领域的核心概念和规则作为软件设计的驱动力。通过通用语言（Ubiquitous Language）统一开发人员和领域专家的沟通语言，避免因理解偏差导致的设计错误。聚合根（Aggregate Root）是 DDD 的核心模式之一，它确保了聚合内部数据的一致性。",
                    "数据库连接泄漏是生产环境中的常见故障。当应用从连接池获取的连接没有正确归还时，连接池中的可用连接会逐渐耗尽，最终导致应用无法响应新的请求。使用连接池的泄漏检测功能和合理的超时配置可以及早发现和防范这类问题。"
            ),
            "frontend", Arrays.asList(
                    "React 18 引入的并发模式（Concurrent Mode）允许 React 在渲染过程中暂停和恢复工作，从而提供更流畅的用户体验。useTransition 和 useDeferredValue 是并发模式的核心 Hook，它们可以将非紧急的更新标记为可中断的，优先响应用户的交互操作。",
                    "Vue 3 的组合式 API（Composition API）相比选项式 API 提供了更好的逻辑复用能力。通过 setup 函数和响应式 API，开发者可以将相关的逻辑组织在一起，而不再分散在 data、methods、computed 等选项中。这使得大型组件的代码更加清晰和易于维护。",
                    "前端性能优化涵盖多个维度：资源加载优化包括代码压缩、CDN 加速、Tree Shaking 和懒加载；渲染性能优化包括减少重排重绘、使用虚拟列表和 Web Worker；网络性能优化包括 HTTP/2 多路复用、资源预加载和合理的缓存策略。",
                    "TypeScript 的类型系统可以在编译阶段发现潜在的错误，提高代码质量和可维护性。泛型（Generics）是 TypeScript 中最强大的类型特性之一，可以实现类型的参数化，在保证类型安全的同时保持代码的灵活性和复用性。",
                    "Webpack 的模块打包机制将各种资源视为模块，通过 Loader 和 Plugin 的配合实现高度可定制的构建流程。Vite 作为新一代构建工具，基于浏览器原生 ES Module 实现极速的冷启动和热更新，在开发体验上有显著提升。",
                    "微前端架构将前端应用拆分为多个独立的子应用，每个子应用可以独立开发、测试和部署。常用的微前端框架包括 qiankun、Micro-app 和 Module Federation。微前端适用于大型团队协作的场景，但需要注意子应用间的样式隔离、JS 沙箱和公共依赖共享等问题。",
                    "CSS 布局技术经历了从浮动到 Flexbox 再到 Grid 的演进过程。Flexbox 适合一维布局，是处理行内元素对齐和分布的首选方案。Grid 则适合二维布局，可以同时控制行和列的排列方式。在实际开发中，两者经常配合使用以达到最佳的布局效果。",
                    "前端状态管理方案的选择取决于应用的复杂度。对于简单应用，Vue 的 reactive/ref 或 React 的 useState/useContext 已经足够。对于复杂应用，Pinia（Vue）和 Zustand（React）是轻量级的状态管理库。Redux 虽然功能强大但模板代码较多，适用于需要严格状态管理的大型项目。",
                    "浏览器渲染原理是前端性能优化的基础。页面渲染经过构建 DOM 树、样式计算、布局、分层、绘制、合成等多个阶段。重排（Reflow）会触发布局计算，重绘（Repaint）只更新像素，合成（Composite）仅由 GPU 处理。使用 transform 和 opacity 属性可以跳过重排重绘直接进入合成阶段。",
                    "前端测试体系包括单元测试、组件测试和端到端测试三个层次。Jest/Vitest 用于单元测试，Testing Library 用于组件测试，Playwright/Cypress 用于端到端测试。测试金字塔原则建议底层测试多一些，上层测试少一些，以在测试覆盖率和维护成本之间取得平衡。",
                    "响应式设计是现代前端开发的标配。使用 CSS 媒体查询、相对单位和弹性布局可以实现一套代码适配多种屏幕尺寸。移动端优先（Mobile First）的设计策略先针对小屏幕设计样式，再通过媒体查询逐步增强大屏幕的体验。Tailwind CSS 的工具类方式让响应式设计更加高效。",
                    "前端工程化涵盖代码规范、自动化构建、模块打包、持续集成和部署等多个环节。ESLint + Prettier 组合是代码规范的标准方案，Husky + lint-staged 可以在 Git 提交前自动检查代码质量。Monorepo 通过 Turborepo 或 Nx 统一管理多个包的构建和发布流程。",
                    "虚拟 DOM 是 React 和 Vue 等框架的核心优化机制。通过在内存中维护一棵轻量级的 DOM 树，框架可以批量计算最小化的 DOM 更新操作，避免频繁的直接 DOM 操作带来的性能损耗。Diff 算法是虚拟 DOM 的关键，React 和 Vue 都采用了启发式的 O(n) 复杂度算法。",
                    "前端安全防护是容易被忽视但至关重要的领域。XSS（跨站脚本攻击）通过转义用户输入和 Content Security Policy 来防护。CSRF（跨站请求伪造）通过 Token 验证来防护。Cookie 设置 HttpOnly 和 Secure 标记可以增强安全性。HTTPS 加密传输是保护数据在传输过程中不被窃取的基础保障。",
                    "Service Worker 是 PWA（渐进式 Web 应用）的核心技术，它作为浏览器和服务器之间的代理，可以拦截网络请求、管理缓存策略和实现离线访问。Workbox 是 Google 提供的 Service Worker 工具库，封装了常用的缓存策略如 Cache First、Network First 和 Stale While Revalidate。",
                    "前端监控体系包括错误监控、性能监控和用户行为监控三个维度。Sentry 是应用最广泛的前端错误监控平台，可以实时收集 JS 运行时错误、资源加载错误和接口异常，并提供 Sourcemap 还原和上下文信息。自建监控系统通常基于 window.onerror 和 Performance API 来实现。"
            ),
            "ai", Arrays.asList(
                    "大语言模型（LLM）的微调技术包括全量微调和 LoRA 等参数高效微调方法。全量微调需要更新模型所有参数，对计算资源要求较高；LoRA 通过在模型的注意力层中引入低秩矩阵，只需要训练少量参数即可取得接近全量微调的效果，是目前最主流的微调方案。",
                    "RAG（检索增强生成）架构通过将外部知识库与语言模型结合，有效解决了模型知识更新滞后和幻觉问题。RAG 的工作流程包括：将文档切分为语义块、通过嵌入模型将文本向量化存入向量数据库、用户提问时检索最相关的文档片段、将检索结果作为上下文提供给 LLM 生成回答。",
                    "向量数据库在高维向量检索中发挥着关键作用，支持的索引类型包括 IVF、HNSW 等近似最近邻搜索算法。IVF 通过聚类降低搜索空间，HNSW 基于图结构实现高效检索。Qdrant、Milvus、Weaviate 和 Pinecone 是目前主流的向量数据库方案，各有优势和适用场景。",
                    "Transformer 架构是现代大语言模型的基础。自注意力机制（Self-Attention）允许模型在处理序列中的每个位置时都能够关注到序列中的所有其他位置，从而捕捉长距离的依赖关系。多头注意力（Multi-Head Attention）进一步增强了模型的表达能力，使之能够从不同表示子空间中学习特征。",
                    "Prompt Engineering（提示工程）是挖掘大语言模型能力的关键技术。通过精心设计的提示词，可以引导模型生成更准确、更有用的回答。Chain-of-Thought（思维链）提示通过在提示中展示推理步骤，显著提升了模型在复杂推理任务上的表现。Few-shot 提示通过提供少量示例让模型理解任务要求。",
                    "模型量化是降低大语言模型部署成本的重要技术。通过将模型参数从 FP32 降精度到 INT8 或 INT4，可以大幅减少显存占用和推理延迟。GPTQ 和 AWQ 是常用的量化算法，它们在校准数据上衡量每一层的敏感度来进行针对性的量化，在保持模型性能的同时显著降低资源消耗。",
                    "Embedding 模型的作用是将文本转换为高维向量表示，使得语义相似的文本在向量空间中距离更近。BGE（BAAI General Embedding）系列模型在中文文本嵌入任务上表现优异，支持 768 维和 1024 维两种规格。MTEB 基准是评估嵌入模型性能的权威标准，涵盖了分类、聚类、配对、重排序等多种任务。",
                    "Agent（智能体）是大语言模型应用的前沿方向。通过赋予 LLM 调用外部工具（Tool Use）和环境交互的能力，Agent 可以完成更复杂的任务。ReAct 框架结合了推理和行动，让模型在思考和执行之间交替进行。LangChain 和 AutoGPT 是构建 AI Agent 的常用框架。",
                    "RLHF（基于人类反馈的强化学习）是训练对齐人类偏好的语言模型的关键技术。它包括三个步骤：收集人类对模型输出的偏好数据、训练奖励模型来预测人类偏好、使用 PPO（近端策略优化）算法优化语言模型。RLHF 使模型能够更好地理解用户意图和遵循指令。",
                    "深度学习中的注意力机制可以类比为人类的视觉注意力——在处理大量信息时，我们会自然地将注意力集中在最重要的部分。交叉注意力（Cross-Attention）用于解码器中，让模型在生成每个输出时关注输入序列中的相关位置，是 seq2seq 模型的核心组件。",
                    "NLP 领域的预训练-微调范式彻底改变了自然语言处理的研究和应用方式。BERT 通过掩码语言模型（MLM）和下一句预测（NSP）任务在大量无标注文本上进行预训练，然后用少量标注数据进行微调即可在多种下游任务上取得优异表现。GPT 系列则采用自回归的语言模型目标进行预训练。",
                    "GraphRAG 是对传统 RAG 架构的增强，它在文档检索的基础上引入了知识图谱的结构化信息。通过将文档中的实体和关系抽取为知识图谱，GraphRAG 可以支持更复杂的多跳推理和关联性分析。在处理需要综合多个信息源的问题时，GraphRAG 比纯文档检索的 RAG 表现更优。"
            ),
            "architecture", Arrays.asList(
                    "DDD（领域驱动设计）通过限界上下文将复杂业务领域划分为多个子域，每个子域拥有独立的领域模型和通用语言。在代码结构上，DDD 通常采用分层架构：接口层负责处理用户请求，应用层编排业务流程，领域层封装核心业务逻辑，基础设施层提供技术支撑。",
                    "CQRS 模式（命令查询职责分离）将系统的写操作和读操作分离开来。写操作使用领域模型确保业务规则的正确执行，读操作使用专门的查询模型优化数据展示的性能。CQRS 常与 Event Sourcing 结合使用，通过事件序列记录每一次状态变更，支持审计、回溯和事件重放。",
                    "六边形架构（Hexagonal Architecture）也称为端口和适配器架构，它通过端口（Port）定义系统的核心功能接口，通过适配器（Adapter）连接外部系统。这种架构使核心业务逻辑与基础设施细节完全解耦，便于测试和技术栈的替换。",
                    "微服务与服务网格（Service Mesh）的结合是云原生架构的主流选择。Istio 作为服务网格的实现，通过 Sidecar 模式在 Pod 中注入代理，实现了服务间通信的流量管理、安全加密和可观测性，而无需在应用代码中引入这些非功能性逻辑。",
                    "事件风暴（Event Storming）是 DDD 实践中的重要工作坊方法，通过快速梳理业务领域中的领域事件，帮助团队建立对业务领域的共同理解。参与者在白板上按时间顺序排列各种颜色的便签，业务人员与技术人员共同参与讨论，可以在短时间内完成对复杂业务领域建模。",
                    "系统架构的演进通常遵循单体 -> 微服务 -> 服务网格的路径。在业务早期，单体架构的开发效率最高；随着业务复杂度提升和团队规模扩大，微服务可以提高团队的自主性和系统的可扩展性；当微服务数量快速增长后，服务网格可以统一治理服务间通信的复杂性。",
                    "技术方案设计文档是架构师的核心产出之一。一份高质量的设计文档应包含：项目背景与目标、核心需求分析、业界方案调研、架构设计（含架构图）、关键业务流程设计、数据模型设计、接口设计、部署架构、风险评估和上线计划。方案评审是确保设计质量的重要环节。",
                    "软件架构中的SOLID原则是面向对象设计的基石。单一职责原则要求一个类只负责一项职责；开闭原则提倡对扩展开放对修改封闭；里氏替换原则确保子类可以替换父类而不破坏程序正确性；接口隔离原则主张小而专一的接口；依赖倒置原则要求依赖抽象而非具体实现。",
                    "系统的容量规划是架构设计中的重要环节。QPS（每秒查询数）、TPS（每秒事务数）、响应时间（RT）和并发用户数是核心的容量指标。通过压力测试确定系统的极限容量后，需要预留一定的容量冗余来应对突发流量。自动伸缩（Auto Scaling）可以根据实时负载动态调整资源。",
                    "反范式化设计是数据库架构中的常见优化策略。虽然规范化可以减少数据冗余和更新异常，但在高并发查询场景下，适当的反范式化可以减少 JOIN 操作、提升查询性能。字段冗余、汇总表、快照表等都是常用的反范式化手段。关键是要在查询性能和数据一致性之间找到平衡。"
            ),
            "devops", Arrays.asList(
                    "Docker 多阶段构建可以有效减小镜像体积，通过将编译环境和运行环境分离，最终镜像只包含运行所需的最小文件。合理编排 Dockerfile 的层级顺序可以充分利用构建缓存，将变化频率低的依赖安装在前面层，变化频率高的业务代码放在后面层。",
                    "Kubernetes 的 Pod 水平自动伸缩（HPA）基于 CPU、内存使用率或自定义指标自动调整 Pod 的副本数量。HPA 的工作原理是定期查询 Metrics Server 获取资源使用数据，然后根据目标利用率计算需要的副本数。VPA（垂直自动伸缩）则自动调整 Pod 的资源请求和限制值。",
                    "CI/CD 流水线是现代软件开发的核心实践。持续集成（CI）要求开发者频繁地将代码合并到主干，每次合并自动触发构建和测试。持续交付（CD）在 CI 的基础上自动将可发布的版本部署到测试环境。持续部署则更进一步，将通过测试的版本自动部署到生产环境。",
                    "Prometheus 是云原生时代的标准监控系统。它采用 Pull 模式主动拉取指标数据，使用多维度的时序数据模型存储指标。PromQL 是 Prometheus 的查询语言，支持灵活的指标计算和聚合。结合 Grafana 可以实现丰富的可视化报警功能，AlertManager 负责告警的管理和通知。",
                    "IaC（基础设施即代码）通过代码来定义和管理基础设施，消除了手动运维操作带来的环境不一致问题。Terraform 使用声明式的 HCL 语言描述云资源，支持资源的规划预览和依赖关系的自动解析。Ansible 则采用过程式的 YAML Playbook，更适合配置管理和应用部署。",
                    "蓝绿部署是一种零停机时间的发布策略。它同时维护蓝色（当前版本）和绿色（新版本）两套完全相同的环境，通过切换负载均衡器的流量指向来完成版本切换。如果新版本出现问题，只需要将流量切回旧版本即可快速回滚。这种方式的缺点是资源成本较高。",
                    "日志聚合系统是分布式系统运维的基础设施。Filebeat 负责在各节点上收集日志文件并发送到 Logstash 或 Kafka，Logstash 对日志进行解析和转换，Elasticsearch 负责存储和索引，Kibana 提供可视化和查询界面。结构化日志配合 traceId 可以实现跨服务的请求追踪。",
                    "GitOps 是一种以 Git 作为单一数据源的运维模式。所有环境的期望状态以声明式配置文件的形式存储在 Git 仓库中，当配置发生变化时，ArgoCD 或 Flux 等 GitOps 工具会自动将实际环境同步到期望状态。GitOps 的版本控制特性使得每次变更都可追溯、可回滚。"
            ),
            "career", Arrays.asList(
                    "Code Review 不仅是发现代码缺陷的手段，更是团队知识分享和技术传承的重要途径。高效的 Code Review 应该关注代码逻辑的正确性、设计的合理性、性能和安全问题，而不应该纠结于代码风格的细节（这些应该交给自动化的 Linter 处理）。",
                    "技术方案的编写是资深工程师的必备技能。一个好的方案不是简单地罗列技术名词，而是清晰地阐述为什么要这么做、有哪些可选的方案、各自的优缺点是什么、最终选择了哪个方案以及为什么。方案的接受者可能不熟悉你的技术栈，所以要避免过度使用专业术语。",
                    "持续学习是技术人员保持竞争力的关键。与其追求广度式地学习每项新技术，不如在选定方向上做深度钻研。阅读源码和参与开源项目是提升技术深度的高效方式，通过阅读优秀项目的代码可以学习到很多书本上学不到的工程实践和设计技巧。",
                    "在团队协作中，沟通能力与技术能力同样重要。当遇到不同的技术观点时，应该用数据和事实来说话，而不是凭个人偏好做决策。主动分享自己的知识和经验，帮助团队成员成长，不仅能提升团队的整体产出，也能锻炼自己的表达和领导能力。",
                    "程序员的职业发展有两条常见路径：技术路线和管理路线。技术路线从初级工程师一路成长为架构师和技术专家；管理路线则转向技术经理、技术总监。无论选择哪条路，技术功底都是基础。建议在职业生涯早期先在技术上打好基础，再根据自己的兴趣选择发展方向。",
                    "面试准备需要有系统性的方法。除了刷算法题，更重要的是深入理解自己做过项目的技术细节和设计考量。面试官通常会通过项目深挖来判断候选人的技术深度和思考方式。STAR 方法（情境、任务、行动、结果）是描述项目经验的有效框架。",
                    "良好的工作习惯能显著提升开发效率。每天开始工作前列出当日的待办事项并排定优先级；遇到问题时先独立思考，但不要钻牛角尖，设定合理的时间上限后主动寻求帮助；定期进行工作复盘，总结成功经验和待改进的地方，形成良性的工作循环。"
            )
    );

    private static final List<String> CONTENT_TYPE_WEIGHTS = initContentTypes();

    private static List<String> initContentTypes() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 85; i++) list.add("MARKDOWN");
        for (int i = 0; i < 5; i++) list.add("PPT_FILE");
        for (int i = 0; i < 5; i++) list.add("EXTERNAL_URL");
        for (int i = 0; i < 5; i++) list.add("INTERNAL_REF");
        return list;
    }

    private String makeTitle() {
        String tmpl = TITLE_TEMPLATES.get(ThreadLocalRandom.current().nextInt(TITLE_TEMPLATES.size()));
        String t1 = TERMS.get(ThreadLocalRandom.current().nextInt(TERMS.size()));
        String t2 = TERMS.get(ThreadLocalRandom.current().nextInt(TERMS.size()));
        return tmpl.replace("{term}", t1).replace("{term2}", t2);
    }

    private String makeBody() {
        List<String> all = new ArrayList<>();
        for (List<String> v : PARAGRAPHS.values()) {
            all.addAll(v);
        }
        int n = ThreadLocalRandom.current().nextInt(3, 9); // 3–8 paragraphs
        String[] sections = {"背景与问题", "核心概念", "技术方案", "实现细节", "优化策略",
                "踩坑经验", "业界对比", "总结与展望", "最佳实践"};

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            String title = sections[i % sections.length];
            sb.append("## ").append(title).append("\n\n");
            String para = all.get(ThreadLocalRandom.current().nextInt(all.size()));
            sb.append(para).append("\n\n");
            // Add 0-2 additional sentences for variety
            int extra = ThreadLocalRandom.current().nextInt(0, 3);
            for (int j = 0; j < extra; j++) {
                String extraPara = all.get(ThreadLocalRandom.current().nextInt(all.size()));
                String firstSentence = extraPara.split("[。；]")[0];
                if (!firstSentence.isEmpty()) {
                    sb.append(firstSentence).append("。\n\n");
                }
            }
        }
        return sb.toString();
    }

    private String pickContentType() {
        return CONTENT_TYPE_WEIGHTS.get(ThreadLocalRandom.current().nextInt(CONTENT_TYPE_WEIGHTS.size()));
    }

    // ================================================================
    //  Utility
    // ================================================================

    private LocalDateTime randTime(LocalDateTime base) {
        return base.plusDays(ThreadLocalRandom.current().nextInt(90))
                .plusHours(ThreadLocalRandom.current().nextInt(24))
                .plusMinutes(ThreadLocalRandom.current().nextInt(60));
    }

    private <T> List<T> pickRandom(List<T> source, int count) {
        if (count >= source.size()) return new ArrayList<>(source);
        List<T> copy = new ArrayList<>(source);
        Collections.shuffle(copy, ThreadLocalRandom.current());
        return copy.subList(0, count);
    }
}
