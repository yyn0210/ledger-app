-- 系统预设分类初始化脚本
-- 执行时机：用户创建第一个账本时自动初始化

-- 支出分类 (type=1)
INSERT INTO categories (id, book_id, parent_id, name, icon, type, sort_order, is_system, deleted) VALUES
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '餐饮', '🍜', 1, 1, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '交通', '🚗', 1, 2, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '购物', '🛍️', 1, 3, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '娱乐', '🎬', 1, 4, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '居住', '🏠', 1, 5, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '通讯', '📱', 1, 6, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '医疗', '🏥', 1, 7, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '教育', '📚', 1, 8, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '人情', '🧧', 1, 9, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '其他', '📦', 1, 10, 1, 0);

-- 收入分类 (type=2)
INSERT INTO categories (id, book_id, parent_id, name, icon, type, sort_order, is_system, deleted) VALUES
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '工资', '💰', 2, 1, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '奖金', '🎁', 2, 2, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '理财', '📈', 2, 3, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '兼职', '💼', 2, 4, 1, 0),
(CONCAT('sys_', UUID_SHORT()), #{bookId}, 0, '其他', '📦', 2, 5, 1, 0);
