-- 周期账单主表
CREATE TABLE IF NOT EXISTS recurring_bills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '周期账单 ID',
    book_id BIGINT NOT NULL COMMENT '账本 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    name VARCHAR(100) NOT NULL COMMENT '账单名称',
    amount DECIMAL(15,2) NOT NULL COMMENT '金额',
    type INTEGER NOT NULL COMMENT '类型：1=支出 2=收入',
    category_id BIGINT COMMENT '分类 ID',
    account_id BIGINT COMMENT '账户 ID',
    period VARCHAR(20) NOT NULL COMMENT '周期：DAILY/WEEKLY/MONTHLY/YEARLY',
    weekday INTEGER COMMENT '星期几（WEEKLY 用，1-7）',
    day_of_month INTEGER COMMENT '每月几号（MONTHLY 用，1-31）',
    month_day VARCHAR(10) COMMENT '月日（YEARLY 用，格式 MM-DD）',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE COMMENT '结束日期（NULL 表示无限）',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/PAUSED/COMPLETED',
    note VARCHAR(200) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INTEGER DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status),
    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='周期账单表';

-- 周期账单执行记录表
CREATE TABLE IF NOT EXISTS recurring_bill_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '执行记录 ID',
    recurring_bill_id BIGINT NOT NULL COMMENT '周期账单 ID',
    scheduled_date DATE NOT NULL COMMENT '计划执行日期',
    actual_date DATE COMMENT '实际执行日期',
    transaction_id BIGINT COMMENT '生成的交易 ID',
    status VARCHAR(20) NOT NULL COMMENT '状态：PENDING/SUCCESS/FAILED/SKIPPED',
    error_message VARCHAR(500) COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_recurring_bill_id (recurring_bill_id),
    INDEX idx_scheduled_date (scheduled_date),
    INDEX idx_status (status),
    FOREIGN KEY (recurring_bill_id) REFERENCES recurring_bills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='周期账单执行记录表';
