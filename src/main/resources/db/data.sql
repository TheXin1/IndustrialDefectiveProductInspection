INSERT INTO sys_role (id, code, name, description, built_in)
VALUES
    (1, 'SUPER_ADMIN', 'Super Admin', 'Full access to all modules', 1),
    (2, 'ADMIN', 'Admin', 'User, role, model and system management', 1),
    (3, 'QUALITY_INSPECTOR', 'Quality Inspector', 'Image inspection and result review', 1),
    (4, 'OPERATOR', 'Operator', 'Production line operator', 1),
    (5, 'ENGINEER', 'Engineer', 'Model and dataset maintenance', 1),
    (6, 'AUDITOR', 'Auditor', 'Audit and report review', 1);

INSERT INTO sys_permission (id, code, name, type, parent_id, path, method, description, sort_order)
VALUES
    (1, 'dashboard', 'Dashboard', 'MODULE', NULL, '/dashboard', NULL, 'Overview metrics', 1),
    (2, 'detect', 'Detection', 'MODULE', NULL, '/detect', NULL, 'Image detection', 2),
    (3, 'realtime', 'Realtime Monitor', 'MODULE', NULL, '/realtime', NULL, 'Realtime monitoring', 3),
    (4, 'records', 'Inspection Records', 'MODULE', NULL, '/records', NULL, 'History records', 4),
    (5, 'reports', 'Reports', 'MODULE', NULL, '/reports', NULL, 'Reports and statistics', 5),
    (6, 'model', 'Model Management', 'MODULE', NULL, '/model', NULL, 'Model version management', 6),
    (7, 'dataset', 'Dataset Management', 'MODULE', NULL, '/dataset', NULL, 'Dataset and feedback', 7),
    (8, 'user_access', 'User and Role', 'MODULE', NULL, '/access', NULL, 'User and role management', 8),
    (9, 'system', 'System Settings', 'MODULE', NULL, '/system', NULL, 'System settings', 9),
    (10, 'audit', 'Audit', 'MODULE', NULL, '/audit', NULL, 'Audit logs', 10),
    (11, 'integration', 'Integration', 'MODULE', NULL, '/integration', NULL, 'MES/WMS/PLC integration', 11),

    (101, 'dashboard:view', 'Dashboard View', 'ACTION', 1, NULL, 'GET', 'View dashboard', 101),
    (201, 'detect:image', 'Detect Image', 'ACTION', 2, '/api/v1/detect/image', 'POST', 'Single image detect', 201),
    (202, 'detect:stream', 'Detect Stream', 'ACTION', 2, '/api/v1/detect/stream', 'POST', 'Stream detect', 202),
    (301, 'realtime:view', 'Realtime View', 'ACTION', 3, NULL, 'GET', 'View realtime monitor', 301),
    (302, 'realtime:alarm', 'Realtime Alarm', 'ACTION', 3, NULL, 'POST', 'Trigger alarm', 302),
    (401, 'records:view', 'Records View', 'ACTION', 4, NULL, 'GET', 'View inspection records', 401),
    (402, 'records:export', 'Records Export', 'ACTION', 4, NULL, 'POST', 'Export inspection records', 402),
    (501, 'reports:view', 'Reports View', 'ACTION', 5, NULL, 'GET', 'View reports', 501),
    (502, 'reports:export', 'Reports Export', 'ACTION', 5, NULL, 'POST', 'Export reports', 502),
    (601, 'model:manage', 'Model Manage', 'ACTION', 6, NULL, 'POST', 'Manage model version', 601),
    (701, 'dataset:manage', 'Dataset Manage', 'ACTION', 7, NULL, 'POST', 'Manage dataset and feedback', 701),
    (801, 'user:manage', 'User Manage', 'ACTION', 8, NULL, 'POST', 'Manage users', 801),
    (802, 'role:manage', 'Role Manage', 'ACTION', 8, NULL, 'POST', 'Manage roles', 802),
    (803, 'permission:view', 'Permission View', 'ACTION', 8, NULL, 'GET', 'View permissions', 803),
    (901, 'system:setting', 'System Setting', 'ACTION', 9, NULL, 'POST', 'System settings', 901),
    (1001, 'audit:view', 'Audit View', 'ACTION', 10, NULL, 'GET', 'View audit logs', 1001),
    (1101, 'integration:manage', 'Integration Manage', 'ACTION', 11, NULL, 'POST', 'Manage integrations', 1101);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11),
    (2, 101), (2, 201), (2, 202), (2, 301), (2, 302), (2, 401), (2, 402), (2, 501), (2, 502),
    (2, 601), (2, 701), (2, 801), (2, 802), (2, 803), (2, 901), (2, 1001), (2, 1101);

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (3, 1), (3, 2), (3, 3), (3, 4), (3, 5),
    (3, 101), (3, 201), (3, 202), (3, 301), (3, 401), (3, 501);

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (4, 1), (4, 2), (4, 3),
    (4, 101), (4, 201), (4, 301);

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (5, 1), (5, 2), (5, 4), (5, 5), (5, 6), (5, 7),
    (5, 101), (5, 201), (5, 401), (5, 501), (5, 601), (5, 701);

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (6, 4), (6, 5), (6, 10),
    (6, 401), (6, 501), (6, 1001);
