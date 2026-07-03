-- =====================================================================
-- InventoryTransaction 대량 더미데이터 생성 스크립트
-- 목적: /api/inventory/transactions 등 조회 API의 대용량 데이터 환경
--       (페이지네이션 부재, 인덱스 부재로 인한 병목) 재현
--
-- 전제:
--  - DummyDataInitializer 로 기본 Section / ProductSKU / User 데이터가
--    이미 생성되어 있어야 함 (FK 참조 대상)
--  - MySQL 8.0+ (재귀 CTE, ROW_NUMBER() 윈도우 함수 사용)
--
-- 실행:
--   mysql -u root -p dummyDB < sql/generate-inventory-transactions.sql
--
-- 생성 건수 조절: 아래 @row_count 값만 변경 (기본 500,000건)
-- =====================================================================

USE dummyDB;

SET SESSION cte_max_recursion_depth = 1000000;

-- 생성할 row 수 (필요에 맞게 조정: 처음엔 10만 정도로 테스트 후 늘리는 걸 추천)
SET @row_count = 500000;

-- ---------------------------------------------------------------------
-- 1. 참조용 PK 목록에 순번(rn) 부여 (기존 Section / ProductSKU / User 재사용)
-- ---------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_sections;
CREATE TEMPORARY TABLE tmp_sections AS
SELECT section_id, ROW_NUMBER() OVER (ORDER BY section_id) AS rn
FROM Section;

DROP TEMPORARY TABLE IF EXISTS tmp_skus;
CREATE TEMPORARY TABLE tmp_skus AS
SELECT sku_id, ROW_NUMBER() OVER (ORDER BY sku_id) AS rn
FROM ProductSKU;

DROP TEMPORARY TABLE IF EXISTS tmp_users;
CREATE TEMPORARY TABLE tmp_users AS
SELECT user_id, ROW_NUMBER() OVER (ORDER BY user_id) AS rn
FROM User;

SET @section_count = (SELECT COUNT(*) FROM tmp_sections);
SET @sku_count     = (SELECT COUNT(*) FROM tmp_skus);
SET @user_count    = (SELECT COUNT(*) FROM tmp_users);

-- ---------------------------------------------------------------------
-- 2. transactionType / status 후보값을 순번으로 매핑 (임시 테이블)
--    엔티티의 TransactionType / InventoryStatus enum 값 그대로 사용
-- ---------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_transaction_types;
CREATE TEMPORARY TABLE tmp_transaction_types (rn INT PRIMARY KEY, value VARCHAR(30));
INSERT INTO tmp_transaction_types VALUES
  (1, 'INBOUND_RECEIPT'), (2, 'STACKING_COMPLETE'), (3, 'RETURN_RECEIPT'),
  (4, 'ALLOCATE'), (5, 'ALLOCATE_CANCEL'), (6, 'SHIPMENT_COMPLETE'),
  (7, 'MOVE_OUT'), (8, 'MOVE_IN'), (9, 'DEFECT_INBOUND'),
  (10, 'STOCK_ADJUSTMENT_PLUS'), (11, 'STOCK_ADJUSTMENT_MINUS');
SET @type_count = (SELECT COUNT(*) FROM tmp_transaction_types);

DROP TEMPORARY TABLE IF EXISTS tmp_statuses;
CREATE TEMPORARY TABLE tmp_statuses (rn INT PRIMARY KEY, value VARCHAR(20));
INSERT INTO tmp_statuses VALUES
  (1, 'UNALLOCATED'), (2, 'ALLOCATED'), (3, 'NORMAL'), (4, 'DEFECTIVE');
SET @status_count = (SELECT COUNT(*) FROM tmp_statuses);

-- ---------------------------------------------------------------------
-- 3. 재귀 CTE로 1 ~ @row_count 시퀀스를 만들고, 각 값을 모듈로 연산으로
--    기존 Section/SKU/User/Type/Status에 랜덤하게 매핑해서 벌크 insert
--    created_at은 최근 90일 사이로 분산 (실제 운영 데이터처럼 시간 분포를 줌)
-- ---------------------------------------------------------------------
INSERT INTO InventoryTransaction
  (section_id, sku_id, transactionType, quantity, status,
   before_quantity, after_quantity, reason, created_at, user_id)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < @row_count
)
SELECT
    ts.section_id,
    tk.sku_id,
    tt.value,
    q.quantity,
    st.value,
    q.before_quantity,
    q.before_quantity + q.quantity AS after_quantity,
    CONCAT('bulk-dummy-', s.n) AS reason,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60 * 24 * 90) MINUTE) AS created_at,
    tu.user_id
FROM seq s
JOIN tmp_sections ts ON ts.rn = ((s.n - 1) % @section_count) + 1
JOIN tmp_skus tk ON tk.rn = ((s.n * 7 - 1) % @sku_count) + 1          -- 시드값 다르게 섞기 위해 곱셈
JOIN tmp_users tu ON tu.rn = ((s.n * 13 - 1) % @user_count) + 1
JOIN tmp_transaction_types tt ON tt.rn = ((s.n * 3 - 1) % @type_count) + 1
JOIN tmp_statuses st ON st.rn = ((s.n * 5 - 1) % @status_count) + 1
JOIN (
    -- n별로 quantity / before_quantity 값을 안정적으로 생성 (seq와 1:1 조인)
    SELECT n,
           1 + (n % 50) AS quantity,
           100 + (n % 500) AS before_quantity
    FROM seq
) q ON q.n = s.n;

-- ---------------------------------------------------------------------
-- 4. 정리
-- ---------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_sections;
DROP TEMPORARY TABLE IF EXISTS tmp_skus;
DROP TEMPORARY TABLE IF EXISTS tmp_users;
DROP TEMPORARY TABLE IF EXISTS tmp_transaction_types;
DROP TEMPORARY TABLE IF EXISTS tmp_statuses;

SELECT COUNT(*) AS total_inventory_transactions FROM InventoryTransaction;
