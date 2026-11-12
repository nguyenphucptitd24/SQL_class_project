
USE KTX;

-- 1. Them ngay ky hop dong
ALTER TABLE ThuePhong
ADD COLUMN NgayKyHopDong DATE NULL
COMMENT 'Ngay sinh vien va ky tuc xa ky hop dong';

-- 2. Them ngay het han hop dong
ALTER TABLE ThuePhong
ADD COLUMN NgayHetHanHopDong DATE NULL
COMMENT 'Ngay hop dong het hieu luc';

-- 3. Them tien dat coc
ALTER TABLE ThuePhong
ADD COLUMN TienCoc DECIMAL(12,2) NOT NULL DEFAULT 0
COMMENT 'So tien sinh vien dat coc';

-- 4. Cap nhat ngay ky cho cac hop dong cu
UPDATE ThuePhong
SET NgayKyHopDong = NgayBatDau
WHERE NgayKyHopDong IS NULL;

-- 5. Kiem tra du lieu
SELECT
    MaThue,
    MaSV,
    SoPhong,
    NgayBatDau,
    NgayKetThuc,
    NgayKyHopDong,
    NgayHetHanHopDong,
    TienCoc
FROM ThuePhong
ORDER BY MaThue;
