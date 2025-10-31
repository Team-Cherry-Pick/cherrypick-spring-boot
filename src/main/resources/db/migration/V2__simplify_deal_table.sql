-- Deal 게시 편의성 개선: 불필요한 컬럼 3개 삭제
ALTER TABLE deal
    DROP COLUMN shipping_rule,
    DROP COLUMN shipping_price,
    DROP COLUMN regular_price;

-- Step 1: 임시 카테고리(9999번) 생성 - 기존 Deal을 임시로 보관할 공간
INSERT INTO category (category_id, name, level) VALUES (9999, '기타', 0);

-- Step 2: 외래키 제약조건 제거 (category 테이블 수정을 위해 임시 제거)
ALTER TABLE deal DROP FOREIGN KEY FK1bkdq850eiksd1y5gs7bkag55;

-- Step 3: 모든 Deal을 임시 카테고리(9999)로 이동
UPDATE deal SET category_id = 9999;

-- Step 4: category_id의 AUTO_INCREMENT 속성 제거 (수동 ID 관리로 전환)
ALTER TABLE category MODIFY category_id BIGINT NOT NULL;

-- Step 5: 임시 카테고리(9999)를 제외한 기존 카테고리 전부 삭제
DELETE FROM category WHERE category_id != 9999;

-- Step 6: 새로운 카테고리 체계 구축 (ID 1~10)
INSERT INTO category (category_id, name, level) VALUES
                                                    (1, '식품', 0),
                                                    (2, '뷰티', 0),
                                                    (3, '패션', 0),
                                                    (4, '디지털/가전', 0),
                                                    (5, 'PC', 0),
                                                    (6, '게임', 0),
                                                    (7, '생활/인테리어', 0),
                                                    (8, '출산/유아동', 0),
                                                    (9, '상품권/패키지', 0),
                                                    (10, '기타', 0);

-- Step 7: 기존 Deal들을 새로운 "기타" 카테고리(10번)로 이동
UPDATE deal SET category_id = 10 WHERE category_id = 9999;

-- Step 8: 임시 카테고리(9999번) 삭제
DELETE FROM category WHERE category_id = 9999;

-- Step 9: 외래키 제약조건 복원
ALTER TABLE deal
    ADD CONSTRAINT FK1bkdq850eiksd1y5gs7bkag55
        FOREIGN KEY (category_id) REFERENCES category(category_id);