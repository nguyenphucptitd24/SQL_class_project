USE KTX;

SET @ktx_alter_sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ThuePhong' AND COLUMN_NAME = 'NgayKyHopDong') = 0,
    'ALTER TABLE ThuePhong ADD COLUMN NgayKyHopDong DATE NULL COMMENT ''Ngay ky hop dong''',
    'SELECT 1'
);
PREPARE ktx_alter_stmt FROM @ktx_alter_sql;
EXECUTE ktx_alter_stmt;
DEALLOCATE PREPARE ktx_alter_stmt;

SET @ktx_alter_sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ThuePhong' AND COLUMN_NAME = 'NgayHetHanHopDong') = 0,
    'ALTER TABLE ThuePhong ADD COLUMN NgayHetHanHopDong DATE NULL COMMENT ''Ngay het han hop dong''',
    'SELECT 1'
);
PREPARE ktx_alter_stmt FROM @ktx_alter_sql;
EXECUTE ktx_alter_stmt;
DEALLOCATE PREPARE ktx_alter_stmt;

SET @ktx_alter_sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ThuePhong' AND COLUMN_NAME = 'TienCoc') = 0,
    'ALTER TABLE ThuePhong ADD COLUMN TienCoc DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''Tien dat coc''',
    'SELECT 1'
);
PREPARE ktx_alter_stmt FROM @ktx_alter_sql;
EXECUTE ktx_alter_stmt;
DEALLOCATE PREPARE ktx_alter_stmt;

UPDATE ThuePhong
SET NgayKyHopDong = NgayBatDau
WHERE NgayKyHopDong IS NULL;

SELECT MaThue, MaSV, SoPhong, NgayBatDau, NgayKetThuc,
       NgayKyHopDong, NgayHetHanHopDong, TienCoc
FROM ThuePhong
ORDER BY MaThue;
