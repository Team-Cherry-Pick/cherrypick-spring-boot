-- SNS_CURATOR 권한 추가
-- SNS_CURATOR가 좋아요한 글은 SNS에 자동 게시됩니다.
INSERT INTO role (name, description, created_at, updated_at)
VALUES ('SNS_CURATOR', 'SNS 게시 권한을 가진 큐레이터', NOW(), NOW());

-- COMMUNITY_CRAWLER 권한 추가
-- 커뮤니티에서 핫딜을 크롤링하는 봇 권한
INSERT INTO role (name, description, created_at, updated_at)
VALUES ('COMMUNITY_CRAWLER', '커뮤니티 핫딜 크롤링 봇 권한', NOW(), NOW());

-- 34번 유저에게 COMMUNITY_CRAWLER 권한 부여
INSERT INTO user_role (user_id, role_id)
VALUES (34, (SELECT role_id FROM role WHERE name = 'COMMUNITY_CRAWLER'));