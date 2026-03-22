# 全平台智能记账 App - 设计系统规范

## 1. 色彩系统

### 1.1 品牌色

| 名称 | Token | 色值 | 使用场景 |
|------|-------|------|----------|
| Primary | `--color-primary` | #18A058 | 主色调，按钮、链接、成功状态 |
| Primary Hover | `--color-primary-hover` | #169252 | 悬停状态 |
| Primary Active | `--color-primary-active` | #14844A | 按下状态 |
| Primary Light | `--color-primary-light` | #E8F7F0 | 浅色背景 |

### 1.2 功能色

| 名称 | Token | 色值 | 使用场景 |
|------|-------|------|----------|
| Success | `--color-success` | #18A058 | 收入、成功提示 |
| Warning | `--color-warning` | #F0A020 | 警告、预算预警 |
| Error | `--color-error` | #D03050 | 支出、错误提示 |
| Info | `--color-info` | #2080F0 | 信息提示、链接 |

### 1.3 中性色

| 名称 | Token | 色值 | 使用场景 |
|------|-------|------|----------|
| Text Primary | `--color-text-primary` | #1F2329 | 主标题、正文 |
| Text Secondary | `--color-text-secondary` | #4E5969 | 副标题、说明文字 |
| Text Tertiary | `--color-text-tertiary` | #86909C | 辅助信息、占位符 |
| Border | `--color-border` | #DEE0E3 | 边框、分割线 |
| Background | `--color-bg` | #F5F7F9 | 页面背景 |
| Surface | `--color-surface` | #FFFFFF | 卡片、弹窗背景 |

### 1.4 收支颜色

| 名称 | Token | 色值 | 使用场景 |
|------|-------|------|----------|
| Income | `--color-income` | #18A058 | 收入金额显示 |
| Expense | `--color-expense` | #D03050 | 支出金额显示 |

---

## 2. 字体系统

### 2.1 字体系列

```css
--font-family-base: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
--font-family-mono: 'SF Mono', 'Roboto Mono', 'Menlo', monospace;
--font-family-number: 'DIN Alternate', 'Roboto', sans-serif;
```

### 2.2 字号规范

| 名称 | Token | 字号 | 行高 | 使用场景 |
|------|-------|------|------|----------|
| H1 | `--font-size-h1` | 32px | 40px | 页面大标题 |
| H2 | `--font-size-h2` | 24px | 32px | 模块标题 |
| H3 | `--font-size-h3` | 20px | 28px | 子标题 |
| H4 | `--font-size-h4` | 18px | 26px | 小标题 |
| Body Large | `--font-size-body-lg` | 16px | 24px | 正文大号 |
| Body | `--font-size-body` | 14px | 22px | 正文字号 |
| Body Small | `--font-size-body-sm` | 13px | 20px | 正文小号 |
| Caption | `--font-size-caption` | 12px | 18px | 说明文字、标注 |

### 2.3 字重

| 名称 | Token | 字重 | 使用场景 |
|------|-------|------|----------|
| Regular | `--font-weight-regular` | 400 | 正文 |
| Medium | `--font-weight-medium` | 500 | 强调文字 |
| SemiBold | `--font-weight-semibold` | 600 | 标题 |
| Bold | `--font-weight-bold` | 700 | 大标题、金额数字 |

---

## 3. 间距系统

### 3.1 基础间距

以 4px 为基础单位：

| 名称 | Token | 数值 | 使用场景 |
|------|-------|------|----------|
| XS | `--spacing-xs` | 4px | 极小间距 |
| S | `--spacing-sm` | 8px | 小间距 |
| M | `--spacing-md` | 12px | 中间距 |
| L | `--spacing-lg` | 16px | 大间距 |
| XL | `--spacing-xl` | 24px | 超大间距 |
| 2XL | `--spacing-2xl` | 32px | 极大间距 |
| 3XL | `--spacing-3xl` | 48px | 页面边距 |

### 3.2 组件间距

| 组件 | 内边距 | 外边距 |
|------|--------|--------|
| 按钮 | 12px 24px | 8px |
| 输入框 | 10px 16px | 16px |
| 卡片 | 16px | 16px |
| 列表项 | 16px | 0 |

---

## 4. 圆角系统

| 名称 | Token | 数值 | 使用场景 |
|------|-------|------|----------|
| None | `--radius-none` | 0 | 无圆角 |
| Small | `--radius-sm` | 4px | 小按钮、标签 |
| Medium | `--radius-md` | 8px | 按钮、输入框、卡片 |
| Large | `--radius-lg` | 12px | 大卡片、弹窗 |
| XL | `--radius-xl` | 16px | 模态框 |
| Full | `--radius-full` | 9999px | 圆形头像、徽章 |

---

## 5. 阴影系统

| 名称 | Token | 值 | 使用场景 |
|------|-------|-----|----------|
| Shadow S | `--shadow-sm` | 0 1px 2px rgba(0,0,0,0.05) | 小按钮、徽章 |
| Shadow M | `--shadow-md` | 0 2px 8px rgba(0,0,0,0.08) | 卡片、下拉菜单 |
| Shadow L | `--shadow-lg` | 0 4px 16px rgba(0,0,0,0.12) | 弹窗、模态框 |
| Shadow XL | `--shadow-xl` | 0 8px 32px rgba(0,0,0,0.16) | 悬浮元素 |

---

## 6. 图标系统

### 6.1 图标尺寸

| 名称 | 尺寸 | 使用场景 |
|------|------|----------|
| XS | 12px | 小标签、行内图标 |
| S | 16px | 按钮图标、表单图标 |
| M | 20px | 导航图标、列表图标 |
| L | 24px | 功能图标、分类图标 |
| XL | 32px | 空状态插图 |

### 6.2 分类图标

| 分类 | 图标 | 颜色 |
|------|------|------|
| 餐饮 | 🍕 | #FF6B6B |
| 交通 | 🚗 | #4ECDC4 |
| 购物 | 🛍️ | #FFE66D |
| 娱乐 | 🎬 | #FF8C94 |
| 居住 | 🏠 | #95E1D3 |
| 医疗 | 🏥 | #F38181 |
| 教育 | 📚 | #AA96DA |
| 工资 | 💰 | #18A058 |
| 投资 | 📈 | #2080F0 |
| 其他 | 📦 | #86909C |

---

## 7. 组件规范

### 7.1 按钮

| 类型 | 尺寸 | 高度 | 内边距 | 字号 |
|------|------|------|--------|------|
| Primary | Large | 48px | 0 32px | 16px |
| Primary | Medium | 40px | 0 24px | 14px |
| Primary | Small | 32px | 0 16px | 13px |
| Secondary | - | 同上 | 同上 | 同上 |
| Text | - | 32px | 0 12px | 14px |

### 7.2 输入框

| 状态 | 高度 | 边框颜色 | 背景色 |
|------|------|----------|--------|
| Default | 40px | #DEE0E3 | #FFFFFF |
| Hover | 40px | #2080F0 | #FFFFFF |
| Focus | 40px | #2080F0 | #FFFFFF |
| Disabled | 40px | #DEE0E3 | #F5F7F9 |
| Error | 40px | #D03050 | #FFFFFF |

### 7.3 卡片

| 属性 | 值 |
|------|-----|
| 背景色 | #FFFFFF |
| 圆角 | 8px |
| 阴影 | 0 2px 8px rgba(0,0,0,0.08) |
| 内边距 | 16px |

---

## 8. 响应式断点

| 断点 | 宽度 | 设备 |
|------|------|------|
| Mobile S | 320px | 小屏手机 |
| Mobile M | 375px | iPhone |
| Mobile L | 428px | iPhone Max |
| Tablet | 768px | iPad |
| Desktop | 1024px | 桌面 |
| Desktop L | 1440px | 大桌面 |

---

## 9. 动效规范

### 9.1 过渡时间

| 名称 | 时长 | 使用场景 |
|------|------|----------|
| Fast | 150ms | 简单状态变化 |
| Normal | 250ms | 一般过渡 |
| Slow | 350ms | 复杂动画 |

### 9.2 缓动函数

```css
--ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);
--ease-out: cubic-bezier(0, 0, 0.2, 1);
--ease-in: cubic-bezier(0.4, 0, 1, 1);
```

### 9.3 常用动画

| 动画 | 时长 | 缓动 | 使用场景 |
|------|------|------|----------|
| Fade In | 250ms | ease-out | 页面加载 |
| Slide Up | 250ms | ease-out | 弹窗出现 |
| Scale | 200ms | ease-in-out | 按钮点击 |
| Shake | 400ms | ease-in-out | 错误提示 |

---

## 10. 无障碍设计

### 10.1 对比度要求

| 内容类型 | 最小对比度 | WCAG 级别 |
|----------|------------|-----------|
| 正文文本 | 4.5:1 | AA |
| 大文本 | 3:1 | AA |
| UI 组件 | 3:1 | AA |

### 10.2 焦点样式

```css
:focus-visible {
  outline: 2px solid #2080F0;
  outline-offset: 2px;
}
```

---

## 11. 深色模式（可选）

| 元素 | 浅色模式 | 深色模式 |
|------|----------|----------|
| 背景 | #F5F7F9 | #1A1A1A |
| 表面 | #FFFFFF | #2D2D2D |
| 主文本 | #1F2329 | #E5E6EB |
| 次文本 | #4E5969 | #86909C |
| 边框 | #DEE0E3 | #4E5969 |

---

**文档版本**: v1.0  
**创建时间**: 2026-03-22  
**最后更新**: 2026-03-22
