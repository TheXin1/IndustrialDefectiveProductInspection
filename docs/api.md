# API 文档（RBAC + 推理服务）

## 基础信息
- Base URL: `/api/v1`
- Content-Type: `application/json`
- 统一响应结构：`ApiResponse<T>`

```json
{
  "success": true,
  "message": "ok",
  "data": {}
}
```

## 通用错误
- 400：业务错误（`message` 为错误码）
- 500：系统错误（`message=internal_error`）

常见错误码：
- `username_password_required`
- `invalid_credentials`
- `user_disabled`
- `user_not_found`
- `username_exists`
- `role_not_found`
- `role_code_exists`
- `role_code_name_required`
- `built_in_role_cannot_delete`
- `invalid_request`
- `image_required`
- `file_read_failed`
- `inference_empty_response`
- `inference_service_unavailable`

---

## 数据结构

### User
- `id` Long
- `username` String
- `displayName` String
- `email` String
- `phone` String
- `status` Integer (1=启用, 0=禁用)
- `lastLoginAt` String (ISO-8601)
- `createdAt` String (ISO-8601)
- `updatedAt` String (ISO-8601)
- `passwordHash` 不返回

### Role
- `id` Long
- `code` String
- `name` String
- `description` String
- `builtIn` Integer (1=内置不可删)

### Permission
- `id` Long
- `code` String
- `name` String
- `type` String (`MODULE` / `ACTION`)
- `parentId` Long
- `path` String
- `method` String
- `description` String
- `sortOrder` Integer

### LoginResponse
- `user` User
- `roles` Role[]
- `permissions` Permission[]

### InferenceDetectResponse
```json
{
  "success": true,
  "data": {
    "description": "...",
    "localization_image_base64": "...",
    "has_anomaly": true
  }
}
```

---

## 1. 登录

### POST /auth/login

请求体：
```json
{
  "username": "admin",
  "password": "123456"
}
```

响应：
```json
{
  "success": true,
  "message": "ok",
  "data": {
    "user": { "...": "..." },
    "roles": [ { "...": "..." } ],
    "permissions": [ { "...": "..." } ]
  }
}
```

---

## 2. 用户管理

### GET /users?status=1
查询用户列表（可选 `status` 过滤）

响应：`User[]`

### GET /users/{id}
查询用户详情

响应：`User`

### POST /users
创建用户

请求体：
```json
{
  "username": "qc01",
  "password": "123456",
  "displayName": "质检员1",
  "email": "qc01@test.com",
  "phone": "13800000000",
  "status": 1,
  "roleIds": [3]
}
```

响应：`User`

### PUT /users/{id}
更新用户

请求体：
```json
{
  "displayName": "质检员A",
  "email": "qc01@company.com",
  "phone": "13800000001",
  "status": 1,
  "roleIds": [3, 4]
}
```

响应：`User`

### DELETE /users/{id}
删除用户

响应：`null`

### GET /users/{id}/roles
查询用户角色列表

响应：`Role[]`

### POST /users/{id}/roles
重新分配用户角色（覆盖式）

请求体：
```json
{
  "roleIds": [2, 3]
}
```

响应：`null`

---

## 3. 角色管理

### GET /roles
查询角色列表

响应：`Role[]`

### GET /roles/{id}
查询角色详情

响应：`Role`

### POST /roles
创建角色

请求体：
```json
{
  "code": "CUSTOM_ROLE",
  "name": "自定义角色",
  "description": "自定义说明",
  "builtIn": 0,
  "permissionIds": [1, 2, 101, 201]
}
```

响应：`Role`

### PUT /roles/{id}
更新角色

请求体：
```json
{
  "name": "自定义角色A",
  "description": "说明更新",
  "permissionIds": [1, 2, 101]
}
```

响应：`Role`

### DELETE /roles/{id}
删除角色（内置角色禁止删除）

响应：`null`

### GET /roles/{id}/permissions
查询角色权限 ID 列表

响应：`Long[]`

### POST /roles/{id}/permissions
重新分配角色权限（覆盖式）

请求体：
```json
{
  "permissionIds": [1, 2, 101, 201]
}
```

响应：`null`

---

## 4. 权限查询

### GET /permissions
查询全部权限

响应：`Permission[]`

---

## 5. 推理服务（封装 api_server.py）

### GET /inference/health
检查推理服务健康状态（转发到 `api_server.py /api/v1/health`）

响应：
```json
{
  "success": true,
  "message": "ok",
  "data": {
    "status": "ok",
    "model_loaded": true
  }
}
```

### GET /inference/info
获取模型路径信息（转发到 `api_server.py /api/v1/info`）

响应：
```json
{
  "success": true,
  "message": "ok",
  "data": {
    "imagebind_ckpt": "...",
    "vicuna_ckpt": "...",
    "anomalygpt_ckpt": "...",
    "delta_ckpt": "..."
  }
}
```

### GET /inference/startup_status
获取模型加载进度（转发到 `api_server.py /api/v1/startup_status`）

响应：
```json
{
  "success": true,
  "message": "ok",
  "data": {
    "state": "ready",
    "step": 6,
    "total": 6,
    "message": "...",
    "started_at": 0,
    "finished_at": 0,
    "error": null
  }
}
```

### POST /inference/detect
图像检测（转发到 `api_server.py /api/v1/detect`）

Content-Type: `multipart/form-data`

表单字段：
- `image` 必填，图片文件
- `normal_image` 可选，参考正常图
- `prompt` 可选，文本提示
- `max_tgt_len` 可选，整数
- `top_p` 可选，浮点数
- `temperature` 可选，浮点数

响应：
```json
{
  "success": true,
  "message": "ok",
  "data": {
    "success": true,
    "data": {
      "description": "...",
      "localization_image_base64": "...",
      "has_anomaly": true
    }
  }
}
```

说明：`data` 内部结构保持与 Python 推理服务一致，外层由 Java 统一封装。
