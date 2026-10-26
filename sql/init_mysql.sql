-- ============================================================
-- Khoi tao CSDL cho he thong quan ly dat lich san bong da
--
-- Chay MOT LAN bang MySQL Workbench hoac:
--   mysql -u root -p < sql/init_mysql.sql
--
-- Diem can nhan manh khi demo: MOI DICH VU MOT DATABASE RIENG.
-- Khong database nao co khoa ngoai tro sang database khac.
-- Du lieu nghiep vu do Hibernate tu sinh bang (ddl-auto=update),
-- file nay chi tao database va cap quyen.
-- ============================================================

CREATE DATABASE IF NOT EXISTS auth_db     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS booking_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS field_db    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS payment_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tai khoan dung chung cho ca 4 dich vu (du an hoc tap nen dung chung cho gon).
CREATE USER IF NOT EXISTS 'football'@'localhost' IDENTIFIED BY 'football123';

GRANT ALL PRIVILEGES ON auth_db.*    TO 'football'@'localhost';
GRANT ALL PRIVILEGES ON booking_db.* TO 'football'@'localhost';
GRANT ALL PRIVILEGES ON field_db.*   TO 'football'@'localhost';
GRANT ALL PRIVILEGES ON payment_db.* TO 'football'@'localhost';

FLUSH PRIVILEGES;

-- ============================================================
-- NANG CAO (lam neu con thoi gian - rat an diem o Y 2b):
-- Thay vi mot tai khoan dung chung, tao 4 tai khoan rieng, moi
-- tai khoan CHI co quyen tren database cua minh. Luc do neu
-- payment-service co lo tro vao booking_db thi MySQL tu chan,
-- chung minh nguyen tac so huu du lieu o muc ha tang:
--
--   CREATE USER 'svc_payment'@'localhost' IDENTIFIED BY '...';
--   GRANT ALL PRIVILEGES ON payment_db.* TO 'svc_payment'@'localhost';
--   -- va KHONG cap quyen gi tren booking_db
-- ============================================================

SHOW DATABASES LIKE '%_db';
