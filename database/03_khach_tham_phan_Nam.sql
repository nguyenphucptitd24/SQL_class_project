-- =====================================================================
-- MODULE QUẢN LÝ KHÁCH THĂM - PHẦN CỦA NAM (Task 4.1: Thiết kế CSDL)
-- Chạy SAU 01_ktx_schema.sql. MySQL 8.0.16+ (cần để CHECK có hiệu lực).
-- Script chạy lại nhiều lần được (idempotent).
-- Lưu file ở dạng UTF-8 để giữ đúng tiếng Việt có dấu.
-- =====================================================================
USE KTX;

-- ---------------------------------------------------------------------
-- 1. THAY ĐỔI SO VỚI 01_ktx_schema.sql (cần báo Biên để gộp vào script chung)
--    Bảng LuotTham ban đầu chỉ có ThoiGianDen. Theo yêu cầu của nhóm,
--    module khách thăm lưu cả giờ đến và giờ về:
--      + Thêm cột ThoiGianDi DATETIME NULL (NULL = khách đang ở trong KTX,
--        cùng quy ước với ThuePhong.NgayKetThuc IS NULL).
--      + Thêm CHECK: giờ về không được trước giờ đến.
-- ---------------------------------------------------------------------

-- 1.1 Thêm cột ThoiGianDi nếu chưa có
SET @co_cot := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'LuotTham' AND COLUMN_NAME = 'ThoiGianDi');
SET @sql := IF(@co_cot = 0,
    'ALTER TABLE LuotTham ADD COLUMN ThoiGianDi DATETIME NULL COMMENT ''NULL nghia la khach dang o trong KTX'' AFTER ThoiGianDen',
    'SELECT ''Cot ThoiGianDi da ton tai'' AS ThongBao');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1.2 Thêm ràng buộc giờ về >= giờ đến nếu chưa có
SET @co_chk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
                WHERE CONSTRAINT_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'LuotTham'
                  AND CONSTRAINT_NAME = 'chk_luottham_thoigian');
SET @sql := IF(@co_chk = 0,
    'ALTER TABLE LuotTham ADD CONSTRAINT chk_luottham_thoigian CHECK (ThoiGianDi IS NULL OR ThoiGianDi >= ThoiGianDen)',
    'SELECT ''Rang buoc chk_luottham_thoigian da ton tai'' AS ThongBao');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 2. CẤU TRÚC CUỐI CÙNG CỦA 2 BẢNG (để đối chiếu / đưa vào báo cáo)
--
-- Khach(SoCMT PK, HoTen NOT NULL, NgaySinh NOT NULL)
--   - Khách được nhận diện duy nhất bằng SoCMT.
--
-- LuotTham(MaLuotTham PK AUTO_INCREMENT,
--          SoCMTKhach FK -> Khach(SoCMT),
--          MaSV       FK -> SinhVien(MaSV),
--          ThoiGianDen NOT NULL,
--          ThoiGianDi  NULL,  CHECK ThoiGianDi >= ThoiGianDen)
--   - Quan hệ: Khach 1-N LuotTham, SinhVien 1-N LuotTham.
--     => Khach và SinhVien là quan hệ N-N, LuotTham là bảng trung gian,
--        nên một khách có thể thăm nhiều sinh viên, và ngược lại.
--   - Khóa ngoại để mặc định RESTRICT: không xóa được khách/sinh viên
--     đã có lịch sử thăm (cùng nguyên tắc bảo toàn lịch sử của Biên).
--
-- Các quy tắc KHÔNG ép được bằng SQL thuần, sẽ kiểm tra ở KhachThamService
-- (task 4.2):
--   a) Sinh viên được thăm phải đang ở KTX (có ThuePhong.NgayKetThuc IS NULL).
--   b) Một khách chỉ có tối đa 1 lượt thăm đang mở (ThoiGianDi IS NULL).
--   c) Họ tên, CMT, ngày sinh không rỗng; ngày sinh trước hôm nay.
--   d) Không giới hạn số khách/sinh viên (theo thống nhất của nhóm).
-- ---------------------------------------------------------------------

-- ---------------------------------------------------------------------
-- 3. DỮ LIỆU MẪU (giả lập, không phải giấy tờ thật)
--    Tuần tính từ Thứ Hai đến Chủ nhật:
--      Tuần 19/10 - 25/10/2026 và tuần 26/10 - 01/11/2026.
--    Có đủ các trường hợp: 1 khách thăm nhiều sinh viên, 1 khách thăm
--    nhiều lần, lượt rơi đúng Thứ Hai / Chủ nhật, lượt khác tháng,
--    và 1 lượt chưa có giờ về.
-- ---------------------------------------------------------------------
INSERT IGNORE INTO Khach(SoCMT, HoTen, NgaySinh) VALUES
('GUEST0001', 'Nguyễn Văn Khách', '2000-01-01'),
('GUEST0002', 'Trần Thị Mai',     '1998-05-20'),
('GUEST0003', 'Lê Hoàng Nam',     '2001-11-03'),
('GUEST0004', 'Phạm Quốc Bảo',    '1995-07-15');

-- Mỗi dòng chỉ được chèn nếu chưa tồn tại lượt thăm trùng (khách, sinh viên, giờ đến)
INSERT INTO LuotTham(SoCMTKhach, MaSV, ThoiGianDen, ThoiGianDi)
SELECT v.cmt, v.masv, v.den, v.di
FROM (
    SELECT 'GUEST0001' AS cmt, 'SV003' AS masv, '2026-09-12 10:00:00' AS den, '2026-09-12 12:00:00' AS di  -- tháng 9
    UNION ALL SELECT 'GUEST0001', 'SV001', '2026-10-20 14:00:00', '2026-10-20 16:30:00'  -- tuần 19-25/10
    UNION ALL SELECT 'GUEST0001', 'SV002', '2026-10-22 09:00:00', '2026-10-22 11:00:00'  -- cùng khách, SV khác
    UNION ALL SELECT 'GUEST0003', 'SV002', '2026-10-25 19:00:00', '2026-10-25 20:30:00'  -- Chủ nhật (biên tuần)
    UNION ALL SELECT 'GUEST0001', 'SV001', '2026-10-26 08:30:00', '2026-10-26 10:00:00'  -- Thứ Hai (biên tuần)
    UNION ALL SELECT 'GUEST0002', 'SV001', '2026-10-27 15:00:00', '2026-10-27 17:00:00'  -- tuần 26/10-01/11
    UNION ALL SELECT 'GUEST0002', 'SV003', '2026-10-28 10:00:00', '2026-10-28 11:30:00'  -- cùng khách, SV khác
    UNION ALL SELECT 'GUEST0003', 'SV002', '2026-10-29 09:30:00', NULL                   -- đang ở trong KTX
) AS v
WHERE NOT EXISTS (
    SELECT 1 FROM LuotTham lt
    WHERE lt.SoCMTKhach = v.cmt AND lt.MaSV = v.masv AND lt.ThoiGianDen = v.den
);
-- GUEST0004 cố ý chưa có lượt thăm nào (để kiểm thử khách chưa từng đến).

-- ---------------------------------------------------------------------
-- 4. KIỂM TRA SAU KHI CHẠY
-- ---------------------------------------------------------------------
-- 4.1 Cấu trúc bảng LuotTham phải có cột ThoiGianDi
DESCRIBE LuotTham;

-- 4.2 Danh sách lượt thăm kèm tên khách (kỳ vọng 8 dòng nếu DB còn dữ liệu mẫu gốc)
SELECT lt.MaLuotTham, k.SoCMT, k.HoTen, lt.MaSV, lt.ThoiGianDen, lt.ThoiGianDi
FROM LuotTham lt
JOIN Khach k ON k.SoCMT = lt.SoCMTKhach
ORDER BY lt.ThoiGianDen;

-- 4.3 Thử vi phạm ràng buộc: câu này PHẢI báo lỗi "Check constraint
--     'chk_luottham_thoigian' is violated" (bỏ dấu -- ở đầu dòng để thử).
-- INSERT INTO LuotTham(SoCMTKhach, MaSV, ThoiGianDen, ThoiGianDi)
-- VALUES ('GUEST0001', 'SV001', '2026-10-29 10:00:00', '2026-10-29 09:00:00');
