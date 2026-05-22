CREATE TABLE IF NOT EXISTS user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(256) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  email VARCHAR(128),
  sso_id VARCHAR(128),
  department_id BIGINT,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS department (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  sort_order INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS role_permission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

-- Seed data
INSERT INTO department (id, name, parent_id, sort_order) VALUES
  (1, '技术管理部', 0, 1),
  (2, '技术中心', 0, 2),
  (3, '前端技术组', 2, 1),
  (4, '基础架构组', 2, 2),
  (5, '产品中心', 0, 3);

INSERT INTO role (code, name) VALUES ('ADMIN', '系统管理员');
INSERT INTO role (code, name) VALUES ('USER', '普通用户');

INSERT INTO permission (code, name) VALUES
  ('content:view', '查看内容'),
  ('content:create', '创建内容'),
  ('content:edit', '编辑内容'),
  ('content:delete', '删除内容'),
  ('content:audit', '审核内容'),
  ('tag:manage', '管理标签'),
  ('user:manage', '管理用户'),
  ('group:approve', '审批群组'),
  ('analytics:view', '查看数据分析'),
  ('system:config', '系统配置'),
  ('sensitive:manage', '管理敏感词');

INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'ADMIN'), id FROM permission;

INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'USER'), id FROM permission
WHERE code IN ('content:view', 'content:create', 'content:edit', 'content:delete');

INSERT INTO user (username, password, display_name, department_id) VALUES
  ('admin', '$2a$10$KkrV5Vq1WZ.06JZGEar3Jec2SHzZ70tFyldyY1wZWh4cJZmzPW4cu', '管理员', 1),
  ('lisan', '$2a$10$/mibGlql.jsDNe5C39yVL.wy5eunnU/.gtk7LKwoYEuJdb1JwDxPW', '李三', 2);

INSERT INTO user_role (user_id, role_id) SELECT u.id, r.id FROM user u, role r WHERE u.username = 'admin' AND r.code = 'ADMIN';
INSERT INTO user_role (user_id, role_id) SELECT u.id, r.id FROM user u, role r WHERE u.username = 'lisan' AND r.code = 'USER';
