# AI Emo 心理健康助手

AI Emo 是一个面向普通用户与后台管理员的心理健康辅助 Web 应用。它提供 AI 心理对话、情绪记录、心理知识内容浏览和运营数据查看等能力，帮助用户进行日常情绪觉察与心理健康知识获取。

> 本项目用于日常心理健康辅助，不替代专业医疗诊断、心理治疗或紧急危机干预服务。

## 功能概览

- 用户注册、登录与基于角色的访问控制
- AI 心理对话与会话记录管理
- 情绪日志创建、查看与后台汇总
- 心理健康知识分类、文章发布与阅读
- 管理后台的数据看板、咨询记录和内容管理
- 文件上传与资源访问

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、Vue Router、Element Plus |
| 后端 | Java 17、Spring Boot 3、Spring Security、Spring AI |
| 数据访问 | MyBatis-Plus、MySQL |
| 身份认证 | JWT |

## 项目结构

```text
ai-vue/                         Vue 前端项目
ai-springboot/                  Spring Boot 后端项目
mental_health_assistant.sql     数据库初始化脚本
```

## 本地运行

### 1. 初始化数据库

在 MySQL 中执行 `mental_health_assistant.sql`，创建项目所需的数据库和表结构。

### 2. 配置后端

将 `ai-springboot/src/main/resources/application.example.yml` 复制为 `application.yml`，并在本机填写数据库连接、AI 服务凭据和 JWT 密钥。

`application.yml` 仅供本地使用，已被 Git 忽略，请不要提交其中的真实凭据。

### 3. 启动后端

```bash
cd ai-springboot
./mvnw spring-boot:run
```

Windows 可使用 `mvnw.cmd spring-boot:run`。

### 4. 启动前端

```bash
cd ai-vue
npm install
npm run dev
```

## 配置与安全

- 公开仓库仅提供 `application.example.yml` 配置模板。
- 数据库密码、AI API Key、JWT 密钥等敏感信息通过本地 `application.yml` 或环境变量提供。
- 提交代码前请确认未包含 `.env`、私钥、访问令牌、生产数据库备份或用户隐私数据。
