# 优答·中小学答疑网 - 项目文档

## 项目简介

**优答·中小学答疑网** 是一个专为中小学生、老师和家长打造的在线学习社区平台。

### 核心功能

- **学习社区**：发帖交流、回帖评论、点赞收藏、话题分类
- **学习资料库**：资料上传、资料下载、分类搜索、标签筛选
- **AI智能答疑**：难题问答对话、图片识别解题
- **在线课程**：课程视频播放、学习进度记录、课程搜索
- **用户管理**：注册登录、个人资料修改、消息通知
- **系统管理**：用户管理、帖子审核、公告发布、数据统计

### 技术栈

| 层次 | 技术 |
|------|------|
| 前端 | Vue 3 + Ant Design Vue + Pinia + Vue Router |
| 后端 | Spring Boot + MyBatis-Plus + JWT |
| 数据库 | MySQL 8.0|
| 文件存储 | 本地存储|
| AI服务 | OpenAI API 留出地方 我自己配置api 例如gpt 等模型

---

## 文档目录

| 文档 | 说明 |
|------|------|
| [01-需求文档.md](./01-需求文档.md) | 项目背景、用户角色、功能需求、非功能需求、技术架构 |
| [02-功能文档.md](./02-功能文档.md) | 各模块详细功能说明、业务流程、页面列表 |
| [03-接口文档.md](./03-接口文档.md) | REST API接口规范、请求响应示例、错误码说明 |
| [04-数据库设计文档.md](./04-数据库设计文档.md) | 数据库表结构、ER图、初始化数据、索引设计 |

---

## 快速开始

### 1. 数据库初始化

```bash
# 创建数据库
CREATE DATABASE youda DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 导入表结构（参考04-数据库设计文档.md）
```

### 2. 后端启动

```bash
cd youda-backend
# 修改 application.yml 配置数据库连接
mvn spring-boot:run
```

### 3. 前端启动

```bash
cd youda-frontend
npm install
npm run dev
```

---

## 项目结构

```
youda-project/
├── docs/                          # 项目文档
│   ├── 01-需求文档.md
│   ├── 02-功能文档.md
│   ├── 03-接口文档.md
│   ├── 04-数据库设计文档.md
│   └── README.md
├── youda-backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/youda/
│   │   │   │       ├── controller/    # 控制器
│   │   │   │       ├── service/       # 服务层
│   │   │   │       ├── mapper/        # 数据访问层
│   │   │   │       ├── entity/        # 实体类
│   │   │   │       ├── dto/           # 数据传输对象
│   │   │   │       ├── vo/            # 视图对象
│   │   │   │       ├── config/        # 配置类
│   │   │   │       ├── utils/         # 工具类
│   │   │   │       └── common/        # 公共类
│   │   │   └── resources/
│   │   │       ├── mapper/            # MyBatis映射文件
│   │   │       └── application.yml    # 配置文件
│   │   └── test/
│   └── pom.xml
└── youda-frontend/                # 前端项目
    ├── src/
    │   ├── api/                   # API接口
    │   ├── assets/                # 静态资源
    │   ├── components/            # 公共组件
    │   ├── layouts/               # 布局组件
    │   ├── router/                # 路由配置
    │   ├── stores/                # Pinia状态管理
    │   ├── utils/                 # 工具函数
    │   └── views/                 # 页面组件
    │       ├── home/              # 首页
    │       ├── community/         # 社区
    │       ├── resources/         # 资料库
    │       ├── courses/           # 课程
    │       ├── ai-chat/           # AI问答
    │       ├── user/              # 用户中心
    │       └── admin/             # 管理后台
    ├── public/
    ├── index.html
    ├── package.json
    └── vite.config.js
```

---

## 版本记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2024-01-26 | 初始版本，完成全部文档编写 |

---

## 联系方式

如有问题或建议，欢迎联系！

**有问题，找优答！**
