CREATE TABLE role (
                      role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      name VARCHAR(50) NOT NULL UNIQUE COMMENT '역할 이름 (ADMIN, CLIENT,
  MODERATOR 등)',
                      description VARCHAR(255) COMMENT '역할 설명',
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE
                                  CURRENT_TIMESTAMP,
                      INDEX idx_name (name)
);

CREATE TABLE user_role (
                           user_id BIGINT NOT NULL COMMENT 'FK: user.user_id',
                           role_id BIGINT NOT NULL COMMENT 'FK: role.role_id',
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '역할 부여 일시',
                           PRIMARY KEY (user_id, role_id),
                           FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
                           FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE,
                           INDEX idx_user_id (user_id),
                           INDEX idx_role_id (role_id)
);

INSERT INTO role (name, description) VALUES
                                         ('ADMIN', '관리자 권한 - 모든 기능 접근 가능'),
                                         ('CLIENT', '일반 사용자 권한');

INSERT INTO user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM user u
         JOIN role r ON u.role = r.name
WHERE u.role IS NOT NULL;
