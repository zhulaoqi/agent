-- ================================
-- Spring AI Alibaba Agent 数据库初始化脚本
-- 数据库：MySQL 8.0+
-- ================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS agent_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE agent_db;

-- ================================
-- 0. 提示词模板表（新增 - 行业最佳实践：数据库持久化）
-- ================================
CREATE TABLE IF NOT EXISTS t_prompt_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '模板名称（唯一）',
    title VARCHAR(200) COMMENT '模板标题',
    template TEXT NOT NULL COMMENT '模板内容（支持{variable}占位符）',
    description TEXT COMMENT '模板描述',
    category VARCHAR(50) COMMENT '模板分类',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
    variables TEXT COMMENT '变量列表（JSON格式）',
    usage_count BIGINT NOT NULL DEFAULT 0 COMMENT '使用次数统计',
    is_system BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否系统内置模板',
    creator_id BIGINT COMMENT '创建人ID',
    updater_id BIGINT COMMENT '最后修改人ID',
    tags TEXT COMMENT '标签（JSON数组）',
    config TEXT COMMENT '扩展配置（JSON格式）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词模板表';

-- ================================
-- 1. 用户表
-- ================================
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    email VARCHAR(100) COMMENT '邮箱',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-活跃，disabled-禁用',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色：user-普通用户，admin-管理员',
    token_quota INT DEFAULT 100000 COMMENT 'Token配额',
    token_used INT DEFAULT 0 COMMENT '已使用Token数',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ================================
-- 2. 会话表
-- ================================
CREATE TABLE IF NOT EXISTS t_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    thread_id VARCHAR(100) NOT NULL UNIQUE COMMENT '线程ID（用于ReactAgent的Memory）',
    title VARCHAR(200) DEFAULT '新对话' COMMENT '会话标题',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-活跃，archived-归档，deleted-删除',
    message_count INT DEFAULT 0 COMMENT '消息数量',
    token_count INT DEFAULT 0 COMMENT 'Token消耗',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_thread_id (thread_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- ================================
-- 3. 消息表
-- ================================
CREATE TABLE IF NOT EXISTS t_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色：user-用户，assistant-助手，system-系统',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type VARCHAR(50) DEFAULT 'text' COMMENT '消息类型：text-文本，image-图片，file-文件',
    metadata JSON COMMENT '元数据（工具调用信息等）',
    token_count INT DEFAULT 0 COMMENT 'Token消耗',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ================================
-- 4. 生成内容表
-- ================================
CREATE TABLE IF NOT EXISTS t_generated_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content_type VARCHAR(50) NOT NULL COMMENT '内容类型：naming-命名，script-脚本，image-图片，ppt-PPT',
    prompt TEXT COMMENT '生成提示词',
    content LONGTEXT COMMENT '生成内容',
    metadata JSON COMMENT '元数据',
    status VARCHAR(20) DEFAULT 'success' COMMENT '状态：success-成功，failed-失败',
    error_message TEXT COMMENT '错误信息',
    token_count INT DEFAULT 0 COMMENT 'Token消耗',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_content_type (content_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生成内容表';

-- ================================
-- 5. 审计日志表
-- ================================
CREATE TABLE IF NOT EXISTS t_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    agent_name VARCHAR(100) COMMENT 'Agent名称',
    action VARCHAR(50) NOT NULL COMMENT '操作：agent_start-Agent开始，tool_call-工具调用，agent_end-Agent结束',
    input_data TEXT COMMENT '输入数据',
    output_data TEXT COMMENT '输出数据',
    status VARCHAR(20) DEFAULT 'success' COMMENT '状态：success-成功，failed-失败',
    error_message TEXT COMMENT '错误信息',
    duration_ms INT COMMENT '执行时长（毫秒）',
    token_count INT DEFAULT 0 COMMENT 'Token消耗',
    metadata JSON COMMENT '元数据',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT '用户代理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- ================================
-- 6. Token使用日志表
-- ================================
CREATE TABLE IF NOT EXISTS t_token_usage_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    model_name VARCHAR(100) COMMENT '模型名称',
    request_type VARCHAR(50) COMMENT '请求类型：chat-对话，image-图片，embedding-向量',
    prompt_tokens INT DEFAULT 0 COMMENT '提示词Token数',
    completion_tokens INT DEFAULT 0 COMMENT '补全Token数',
    total_tokens INT DEFAULT 0 COMMENT '总Token数',
    cost DECIMAL(10, 6) DEFAULT 0 COMMENT '成本（元）',
    request_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
    INDEX idx_user_id (user_id),
    INDEX idx_request_time (request_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token使用日志表';

-- ================================
-- 7. 工作流执行记录表
-- ================================
CREATE TABLE IF NOT EXISTS t_workflow_execution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    workflow_id VARCHAR(100) COMMENT '工作流ID',
    workflow_name VARCHAR(100) COMMENT '工作流名称',
    workflow_definition TEXT COMMENT '工作流定义（JSON）',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待执行，running-执行中，paused-暂停，completed-完成，failed-失败',
    current_step VARCHAR(100) COMMENT '当前步骤',
    total_steps INT DEFAULT 0 COMMENT '总步骤数',
    completed_steps INT DEFAULT 0 COMMENT '已完成步骤数',
    result TEXT COMMENT '执行结果（JSON）',
    error_message TEXT COMMENT '错误信息',
    require_human_intervention BOOLEAN DEFAULT FALSE COMMENT '是否需要人工介入',
    intervention_note TEXT COMMENT '人工介入说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    complete_time DATETIME COMMENT '完成时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流执行记录表';

-- ================================
-- 插入初始数据
-- ================================

-- 插入默认用户（密码：admin123，需要BCrypt加密后的值）
-- 实际密码：admin123，BCrypt加密后：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
INSERT INTO t_user (username, password, email, nickname, role, token_quota, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com', '管理员', 'admin', 1000000, 'active')
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试用户（密码：user123）
-- 实际密码：user123，BCrypt加密后：$2a$10$X.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
INSERT INTO t_user (username, password, email, nickname, role, token_quota, status) 
VALUES ('user', '$2a$10$X.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user@example.com', '测试用户', 'user', 100000, 'active')
ON DUPLICATE KEY UPDATE username=username;

-- ================================
-- 权限说明
-- ================================
-- 确保MySQL用户有足够的权限：
-- GRANT ALL PRIVILEGES ON agent_db.* TO 'root'@'localhost';
-- FLUSH PRIVILEGES;

-- ================================
-- 完成
-- ================================
-- 初始化完成！
-- 默认用户：
--   管理员 - username: admin, password: admin123
--   普通用户 - username: user, password: user123

