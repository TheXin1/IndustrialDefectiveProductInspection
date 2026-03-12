# RBAC 设计说明

## 角色划分
- SUPER_ADMIN：超级管理员，拥有全部模块与数据的最高权限。
- ADMIN：系统管理员，负责用户/角色管理、模型管理、报表与集成配置。
- QUALITY_INSPECTOR：质检员，负责图像检测、实时监控、记录查看与报表。
- OPERATOR：产线操作员，负责实时监控与基础检测。
- ENGINEER：算法/设备工程师，负责模型与数据集维护，并可查看检测结果。
- AUDITOR：审计员，只读访问记录、报表与审计日志。

## 功能模块划分
- 仪表盘（Dashboard）
- 检测（Detection）
- 实时监控（Realtime Monitor）
- 检测记录（Inspection Records）
- 报表统计（Reports）
- 模型管理（Model Management）
- 数据集管理（Dataset Management）
- 用户与角色管理（User and Role Management）
- 系统设置（System Settings）
- 审计（Audit）
- 集成（Integration）

## 数据库表
- `sys_user`、`sys_role`、`sys_permission`
- `sys_user_role`、`sys_role_permission`
- `sys_audit_log`
- `inspection_record`
