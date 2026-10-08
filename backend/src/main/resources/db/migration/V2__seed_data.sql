-- 种子账号：密码为 BCrypt 哈希，明文登录口令见《本地运行指南.md》
-- admin    / admin123      -> ADMIN
-- manager  / manager123    -> MANAGER
-- researcher / researcher123 -> RESEARCHER
INSERT INTO sys_user (username, password_hash, real_name, role, enabled) VALUES
('admin', '$2a$10$B.06p8DTrfRkNm8oiMwGL.4Ur8Gkb6UOZ15CfuQd3mXO8IB2WM9we', '系统管理员', 'ADMIN', 1),
('manager', '$2a$10$klHhy6zxTQhjV5XzfQnoP.4pJfPJy33zyn50ebreEx7KGrWQD0XRC', '王审批', 'MANAGER', 1),
('researcher', '$2a$10$cdTAXCGEJ8sk.QDp7fAyw.r5BM43J5uf6Ooeqg1032BJ6SCRsG/QW', '李研究员', 'RESEARCHER', 1);
