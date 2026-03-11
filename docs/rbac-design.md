# RBAC Design

## Roles
- SUPER_ADMIN: Full access to all modules and data.
- ADMIN: User/role management, model management, reports, integrations.
- QUALITY_INSPECTOR: Image inspection, realtime monitor, records, reports.
- OPERATOR: Realtime monitor and basic detection.
- ENGINEER: Model and dataset maintenance plus inspection review.
- AUDITOR: Read-only access to records, reports, and audit logs.

## Functional Modules
- Dashboard
- Detection
- Realtime Monitor
- Inspection Records
- Reports
- Model Management
- Dataset Management
- User and Role Management
- System Settings
- Audit
- Integration

## Database Tables
- sys_user, sys_role, sys_permission
- sys_user_role, sys_role_permission
- sys_audit_log
- inspection_record
