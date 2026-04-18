# 听我 - 音频知识服务平台

## 项目简介

「听我」是一个音频引流 + 1对1咨询变现的专业知识服务平台。创作者通过发布音频吸引用户，用户可向创作者进行一对一付费咨询。

技术栈如下：

- **后端**：Spring Boot 4.0.4、Spring Security + JWT、MyBatis-Plus、MySQL、Redis、RabbitMQ、MinIO、阿里云 DashScope
- **前端**：Vue 3、Vite、Element Plus、Axios

核心功能如下：

- 用户/创作者/管理员多角色权限
- 音频稿件管理（发布、审核、转码）
- AI 音频转写与摘要（异步任务，支持确认）
- AI 智能排期（预览 + 批量生成）
- 虚拟币充值、购买音频、咨询预约
- 收藏、点赞、评论、关注
- 数据大盘与退款审核

## QuickStart

前端只需拉取分支，然后 `npm install`，安装好依赖后 `npm run dev`。后端比较麻烦。

### 环境要求

- JDK 17+
- MySQL：持久化数据
- Redis：存储临时数据 + 缓存数据
- RabbitMQ：用于异步任务
- MinIO：存储文件
- 阿里云 DashScope API Key：用于 AI 功能
- 支付宝沙盒：用于支付功能测试

### 配置字段

[application.yaml](./src/main/resources/application.yaml) 敏感字段需要自行配置，可以通过环境变量、application-dev.yaml 等方式添加。

### 初始化数据库

启动数据库后，在 `src/resources` 目录下，在终端连接数据库：

```bash
mysql -h 127.0.0.1 -P 3306 -u root -p
```

然后运行：

```bash
source schema.sql;
source data.sql;
```

导入表和测试数据。

### 运行后端

在项目根目录下运行：

```bash
./mvnw spring-boot:run
```

## 分支说明

本仓库包含三个独立分支，分别存放不同部分的代码，便于按需获取：

| 分支       | 内容                    | 用途                |
| ---------- | ----------------------- | ------------------- |
| `main`     | 后端 Spring Boot 源代码 | 后端服务开发与部署  |
| `docs`     | 接口文档（Markdown）    | 查看或编辑 API 文档 |
| `frontend` | 前端 Vue 3 源代码       | 前端页面开发        |

### 使用方式

请根据所需模块，**分别克隆对应分支到不同目录**，而不是在同一个仓库中切换分支。

```bash
git clone --branch main --single-branch https://github.com/AK47are/listen-to-me.git backend
git clone --branch docs --single-branch https://github.com/AK47are/listen-to-me.git docs
git clone --branch frontend --single-branch https://github.com/AK47are/listen-to-me.git frontend
```

> 提示：使用 `--single-branch` 仅下载指定分支，节省空间和时间。
