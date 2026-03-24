-- 全平台智能记账应用 - 数据库初始化脚本
-- PostgreSQL

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar_url VARCHAR(255),
    phone VARCHAR(20),
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 账本表
CREATE TABLE IF NOT EXISTS books (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20),
    type INTEGER DEFAULT 1,
    is_default INTEGER DEFAULT 0,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(50),
    type INTEGER NOT NULL,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 账户表
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    type INTEGER NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(10) DEFAULT 'CNY',
    icon VARCHAR(50),
    color VARCHAR(20),
    is_include BOOLEAN DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 交易记录表
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    type INTEGER NOT NULL,
    transaction_date DATE NOT NULL,
    description VARCHAR(255),
    location VARCHAR(100),
    merchant VARCHAR(100),
    tags VARCHAR(255),
    attachment_urls TEXT,
    is_transfer BOOLEAN DEFAULT false,
    transfer_to_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 预算表
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT,
    category_id BIGINT,
    amount DECIMAL(15,2) NOT NULL,
    budget_type INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    warning_threshold DECIMAL(5,2) DEFAULT 80.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_books_user_id ON books(user_id);
CREATE INDEX IF NOT EXISTS idx_categories_book_id ON categories(book_id);
CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_book_id ON transactions(book_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_budgets_user_id ON budgets(user_id);

-- 插入预置分类（book_id=0 表示全局预置）
-- 支出分类 (type=1)
INSERT INTO categories (id, book_id, parent_id, name, icon, type, sort_order, is_preset) VALUES
(1, 0, 0, '餐饮', '🍕', 1, 1, 1),
(2, 0, 0, '交通', '🚗', 1, 2, 1),
(3, 0, 0, '购物', '🛍️', 1, 3, 1),
(4, 0, 0, '娱乐', '🎬', 1, 4, 1),
(5, 0, 0, '居住', '🏠', 1, 5, 1),
(6, 0, 0, '医疗', '🏥', 1, 6, 1),
(7, 0, 0, '教育', '📚', 1, 7, 1),
(8, 0, 0, '通讯', '📱', 1, 8, 1),
(9, 0, 0, '人情', '🧧', 1, 9, 1),
(10, 0, 0, '其他', '📦', 1, 10, 1)
ON CONFLICT (id) DO NOTHING;

-- 收入分类 (type=2)
INSERT INTO categories (id, book_id, parent_id, name, icon, type, sort_order, is_preset) VALUES
(11, 0, 0, '工资', '💰', 2, 1, 1),
(12, 0, 0, '奖金', '🎁', 2, 2, 1),
(13, 0, 0, '理财', '📈', 2, 3, 1),
(14, 0, 0, '兼职', '💼', 2, 4, 1),
(15, 0, 0, '其他', '📦', 2, 5, 1)
ON CONFLICT (id) DO NOTHING;

-- 插入默认数据（可选）
-- INSERT INTO users (id, username, email, password_hash, nickname) VALUES 
-- (1, 'admin', 'admin@example.com', '$2a$10$...', '管理员');
