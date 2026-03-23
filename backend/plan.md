# Task Plan: 账本应用 - 用户认证模块开发

**Task ID**: task-20260323-100500-ledger-auth-module
**Assigned to**: 后端 1
**Started**: 2026-03-23T02:05:00Z

## Steps

- [x] Step 1: 更新 schema.sql - 创建用户表结构（已存在）
- [x] Step 2: 创建 User 实体类
- [x] Step 3: 创建 UserRepository 数据访问层
- [x] Step 4: 创建 AuthService 服务层（登录/注册/JWT）
- [x] Step 5: 创建 AuthController 控制器
- [x] Step 6: 配置 Spring Security + JWT 过滤器
- [x] Step 7: 更新 pom.xml 添加安全依赖
- [x] Step 8: 创建 API 测试文档 (API-TEST.md)
- [x] Step 9: 推送代码到 MinIO 并汇报

## Notes

- 编译需要 JDK 17+ 环境（当前环境只有 JRE）
- 代码结构完整，可在完整 JDK 环境中编译运行
- JWT 密钥需要在生产环境修改

## Notes

- 使用 BCrypt 密码加密
- JWT Token 有效期 24 小时
- API 路径：/api/auth/*
- 需要添加的依赖：spring-boot-starter-security, jjwt, mybatis-plus
