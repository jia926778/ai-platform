-- Create database if not exists
-- CREATE DATABASE IF NOT EXISTS ai_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Roles table
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Users table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    status INT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User-Role join table
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Chat conversations table
CREATE TABLE IF NOT EXISTS chat_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200),
    model VARCHAR(50) DEFAULT 'gpt-3.5-turbo',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Chat messages table
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    tokens INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation_id (conversation_id),
    FOREIGN KEY (conversation_id) REFERENCES chat_conversation(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- API usage log table
CREATE TABLE IF NOT EXISTS api_usage_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    api_type VARCHAR(50) NOT NULL,
    model VARCHAR(50),
    prompt_tokens INT,
    completion_tokens INT,
    total_tokens INT,
    cost DECIMAL(10,6),
    status VARCHAR(20),
    error_msg TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_api_type (api_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default roles
INSERT IGNORE INTO sys_role (role_name, role_code, description) VALUES
('User', 'ROLE_USER', 'Default user role'),
('Administrator', 'ROLE_ADMIN', 'System administrator role');

-- Insert default admin user (password: admin123, BCrypt encoded)
INSERT IGNORE INTO sys_user (username, password, email, nickname, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', 'Administrator', 1);

-- Assign admin role to admin user
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'admin' AND r.role_code = 'ROLE_ADMIN';

-- Also assign user role to admin
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'admin' AND r.role_code = 'ROLE_USER';

-- AI请求日志表（记录完整请求/响应内容，支持追溯与分析）
CREATE TABLE IF NOT EXISTS ai_request_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名（冗余字段）',
    api_type VARCHAR(30) NOT NULL COMMENT 'API类型：CHAT/SUMMARY/CODEGEN',
    model VARCHAR(50) COMMENT '使用的AI模型',
    provider VARCHAR(30) COMMENT 'AI服务提供商',
    request_content MEDIUMTEXT COMMENT '请求内容（用户输入）',
    response_content MEDIUMTEXT COMMENT '响应内容（AI输出）',
    system_prompt TEXT COMMENT '系统提示词',
    prompt_tokens INT DEFAULT 0 COMMENT '提示词Token数',
    completion_tokens INT DEFAULT 0 COMMENT '生成内容Token数',
    total_tokens INT DEFAULT 0 COMMENT '总Token消耗',
    duration_ms BIGINT DEFAULT 0 COMMENT '请求耗时（毫秒）',
    status VARCHAR(20) NOT NULL COMMENT '请求状态：SUCCESS/FAILED',
    error_message TEXT COMMENT '错误信息',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    conversation_id BIGINT COMMENT '会话ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_request_user_id (user_id),
    INDEX idx_request_api_type (api_type),
    INDEX idx_request_status (status),
    INDEX idx_request_created_at (created_at),
    INDEX idx_request_provider (provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI请求日志表';
