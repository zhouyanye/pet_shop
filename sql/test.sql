INSERT INTO admin (username, password, nickname, role, status)
VALUES ('admin', 'admin123', '超级管理员', 1, 1)
ON DUPLICATE KEY UPDATE
                     password='admin123', nickname='超级管理员', role=1, status=1;
