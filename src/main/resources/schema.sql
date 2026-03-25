-- 全平台智能记账应用 - 数据库初始化脚本
-- MySQL 8.0

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(255) COMMENT '头像 URL',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 账本表
CREATE TABLE IF NOT EXISTS books (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    name VARCHAR(50) NOT NULL COMMENT '账本名称',
    icon VARCHAR(50) COMMENT '图标',
    color VARCHAR(20) COMMENT '颜色',
    type TINYINT DEFAULT 1 COMMENT '类型：1-普通账本，2-公共账本',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：0-否，1-是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本表';

-- 分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY,
    book_id BIGINT NOT NULL COMMENT '账本 ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类 ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(50) COMMENT '图标',
    type TINYINT NOT NULL COMMENT '类型：1-支出，2-收入',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_book_id (book_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 账户表
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    name VARCHAR(50) NOT NULL COMMENT '账户名称',
    type TINYINT NOT NULL COMMENT '类型：1-现金，2-银行卡，3-支付宝，4-微信',
    balance DECIMAL(15,2) DEFAULT 0.00 COMMENT '余额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币',
    icon VARCHAR(50) COMMENT '图标',
    color VARCHAR(20) COMMENT '颜色',
    is_include TINYINT DEFAULT 1 COMMENT '是否包含统计：0-否，1-是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户表';

-- 交易记录表
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY,
    book_id BIGINT NOT NULL COMMENT '账本 ID',
    account_id BIGINT NOT NULL COMMENT '账户 ID',
    category_id BIGINT NOT NULL COMMENT '分类 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    amount DECIMAL(15,2) NOT NULL COMMENT '金额',
    type TINYINT NOT NULL COMMENT '类型：1-支出，2-收入，3-转账',
    transaction_date DATE NOT NULL COMMENT '交易日期',
    description VARCHAR(255) COMMENT '描述',
    location VARCHAR(100) COMMENT '地点',
    merchant VARCHAR(100) COMMENT '商户',
    tags VARCHAR(255) COMMENT '标签',
    attachment_urls TEXT COMMENT '附件 URLs',
    is_transfer TINYINT DEFAULT 0 COMMENT '是否转账：0-否，1-是',
    transfer_to_id BIGINT COMMENT '转账到交易 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_book_id (book_id),
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_date (transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- 预算表
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    book_id BIGINT COMMENT '账本 ID',
    category_id BIGINT COMMENT '分类 ID',
    amount DECIMAL(15,2) NOT NULL COMMENT '预算金额',
    budget_type TINYINT NOT NULL COMMENT '类型：1-月度，2-年度，3-自定义',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    warning_threshold DECIMAL(5,2) DEFAULT 80.00 COMMENT '预警阈值 (%)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算表';

-- 导出记录表
CREATE TABLE IF NOT EXISTS export_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    book_id BIGINT COMMENT '账本 ID',
    export_type VARCHAR(50) NOT NULL COMMENT '导出类型',
    file_format VARCHAR(20) NOT NULL COMMENT '文件格式',
    file_path TEXT COMMENT '文件路径',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小 (字节)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-进行中，2-成功，3-失败',
    error_message TEXT COMMENT '错误信息',
    expire_at DATETIME COMMENT '过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导出记录表';

-- 通知记录表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type TINYINT NOT NULL COMMENT '类型：1-系统，2-业务',
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    template_id BIGINT COMMENT '模板 ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-未发送，2-已发送，3-发送失败',
    error_message TEXT,
    sent_at DATETIME COMMENT '发送时间',
    read_at DATETIME COMMENT '阅读时间',
    biz_type VARCHAR(50) COMMENT '业务类型',
    biz_id BIGINT COMMENT '业务 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录表';

-- 通知模板表
CREATE TABLE IF NOT EXISTS notification_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '模板编码',
    type TINYINT NOT NULL COMMENT '类型',
    content TEXT NOT NULL COMMENT '模板内容',
    title_template VARCHAR(255) COMMENT '标题模板',
    biz_type VARCHAR(50) COMMENT '业务类型',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- 用户通知偏好表
CREATE TABLE IF NOT EXISTS user_notification_preference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    book_id BIGINT,
    email_enabled TINYINT DEFAULT 0,
    sms_enabled TINYINT DEFAULT 0,
    in_app_enabled TINYINT DEFAULT 1,
    push_enabled TINYINT DEFAULT 1,
    subscribed_types TEXT COMMENT '订阅类型',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_book (user_id, book_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知偏好表';

-- 插入默认通知模板数据
INSERT INTO notification_template (name, code, type, content, title_template, biz_type, is_enabled) VALUES
('预算预警', 'BUDGET_ALERT', 3, '预算"{budgetName}"已达到{progress}%，请注意控制支出。', '预算预警', 'budget', 1),
('预算超支', 'BUDGET_OVERDUE', 3, '预算"{budgetName}"已超支（{progress}%），请及时调整预算。', '预算超支', 'budget', 1),
('周期账单执行', 'RECURRING_BILL_EXECUTED', 3, '周期账单"{billName}"已执行，金额：{amount}元。', '周期账单执行', 'recurring', 1),
('储蓄目标完成', 'SAVINGS_COMPLETED', 3, '恭喜！储蓄目标"{targetName}"已完成，已存{amount}元。', '储蓄目标完成', 'savings', 1);

-- 插入默认数据（可选）
-- INSERT INTO users (id, username, email, password_hash, nickname) VALUES 
-- (1, 'admin', 'admin@example.com', '$2a$10$...', '管理员');
