# Spring Boot 用户鉴权和文件上传示例

这是一个适合学习的最小后端系统，包含：

- 用户注册和登录
- BCrypt 密码加密
- JWT Token 鉴权
- H2 内存数据库
- 文件上传、列表、下载
- 每个用户只能看到和下载自己的文件

## 运行

```bash
mvn spring-boot:run
```

服务默认启动在 `http://localhost:8080`。

前端页面地址：`http://localhost:8080`

Vue 前端源码在 `frontend/`：

```bash
cd frontend
npm install
npm run dev
```

开发模式会启动 Vite：`http://127.0.0.1:5173`，并把 `/api` 请求代理到 Spring Boot。

把 Vue 打包进 Spring Boot 静态目录：

```bash
cd frontend
npm run build
```

H2 控制台地址：`http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:fileupload`
- User Name: `sa`
- Password: 留空

## 接口演示

### 1. 注册

```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123456"}'
```

### 2. 登录并保存 Token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123456"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')
```

### 3. 访问受保护接口

```bash
curl -i http://localhost:8080/api/me \
  -H "Authorization: Bearer $TOKEN"
```

### 4. 上传文件

```bash
curl -i -X POST http://localhost:8080/api/files \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@README.md"
```

### 5. 查看我的文件

```bash
curl -i http://localhost:8080/api/files \
  -H "Authorization: Bearer $TOKEN"
```

### 6. 下载文件

把上一步返回的 `id` 替换到 URL 中：

```bash
curl -L -o downloaded-file \
  http://localhost:8080/api/files/1/download \
  -H "Authorization: Bearer $TOKEN"
```

## 代码学习路线

建议按这个顺序读：

1. `FileuploadAuthApplication`：Spring Boot 启动入口。
2. `SecurityConfig`：哪些接口放行，哪些接口需要登录。
3. `AuthController` / `AuthService`：注册、登录、生成 JWT。
4. `JwtAuthenticationFilter` / `JwtService`：每次请求如何解析 Token。
5. `FileController` / `FileStorageService`：文件怎么保存到磁盘，元数据怎么存数据库。
6. `AppUser` / `StoredFile`：JPA 实体和表结构。
7. `src/main/resources/static`：前端页面如何调用后端接口。

## 注意

这个项目是学习示例。真实生产环境还需要：

- 把 JWT 密钥放到环境变量或密钥管理系统中
- 使用 MySQL/PostgreSQL 等持久化数据库
- 限制文件类型并做病毒扫描
- 文件存储迁移到对象存储，例如 S3、OSS、COS
- 为下载接口增加审计日志和限流
