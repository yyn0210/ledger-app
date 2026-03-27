# Task Plan: 分类管理模块开发

**Task ID**: task-20260324-030003
**Assigned to**: Chisong (赤松)
**Started**: 2026-03-24T00:00:00Z
**Completed**: 2026-03-24T01:00:00Z

## Steps

- [x] Step 1: 阅读任务规格 (spec.md)
- [x] Step 2: 创建 CategoryType 枚举
- [x] Step 3: 创建 Category 实体类
- [x] Step 4: 创建 CategoryRepository (Mapper)
- [x] Step 5: 创建 DTO（CreateCategoryRequest, UpdateCategoryRequest, CategoryResponse）
- [x] Step 6: 创建 CategoryService 接口
- [x] Step 7: 创建 CategoryServiceImpl 实现
- [x] Step 8: 创建 CategoryController
- [x] Step 9: 创建系统预设分类初始化脚本
- [x] Step 10: 更新 plan.md 完成状态

## 交付物

### 代码文件
```
backend/src/main/java/com/ledger/app/modules/category/
├── controller/
│   └── CategoryController.java          # 6 个 API 接口
├── service/
│   ├── CategoryService.java             # 服务接口
│   └── impl/
│       └── CategoryServiceImpl.java     # 服务实现
├── entity/
│   └── Category.java                    # 分类实体
├── repository/
│   └── CategoryRepository.java          # Mapper 接口
├── dto/
│   ├── request/
│   │   ├── CreateCategoryRequest.java   # 创建请求
│   │   └── UpdateCategoryRequest.java   # 更新请求
│   └── response/
│       └── CategoryResponse.java        # 响应 DTO
└── enums/
    └── CategoryType.java                # 分类类型枚举
```

### 资源文件
```
backend/src/main/resources/data/
└── init-categories.sql                  # 系统预设分类初始化脚本
```

## API 接口清单

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/categories?bookId={id}&type={type} | 获取分类列表（带父子层级） |
| GET | /api/categories/{id}?bookId={id} | 获取分类详情 |
| POST | /api/categories | 创建分类 |
| PUT | /api/categories/{id}?bookId={id} | 更新分类 |
| DELETE | /api/categories/{id}?bookId={id} | 删除分类 |
| GET | /api/categories/system?type={type} | 获取系统预设分类 |

## 核心功能

- ✅ 父子分类层级支持（最多两级）
- ✅ 系统预设分类（支出 10 个 + 收入 5 个）
- ✅ 权限控制（只能操作自己账本内的分类）
- ✅ 软删除支持
- ✅ 系统预设分类不可删除/修改
- ✅ 分类名称重复检查
- ✅ 删除前检查子分类

## 待补充

- ⏳ 删除前检查交易记录引用（依赖交易模块完成后实现）
- ⏳ 单元测试（覆盖率>80%）

## 技术要点

1. **父子层级组装**: 先获取一级分类，再为每个一级分类查询子分类
2. **权限验证**: 通过 bookId 验证用户权限
3. **系统预设保护**: is_system=1 的分类不可删除/修改
4. **软删除**: 使用 MyBatis Plus 的@TableLogic 注解

## 下一步

继续开发 **账户模块 (account)** 或 **交易模块 (transaction)**
