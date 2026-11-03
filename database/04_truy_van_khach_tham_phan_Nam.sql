-- =====================================================================
-- MODULE QUẢN LÝ KHÁCH THĂM - PHẦN CỦA NAM (Task 4.3: Truy vấn SQL)
-- Chạy SAU 01_ktx_schema.sql và 03_khach_tham_phan_Nam.sql.
-- Kết quả kỳ vọng ghi bên dưới ứng với dữ liệu mẫu trong file 03.
--
-- Quy ước thống kê:
--   * Tuần tính từ Thứ Hai đến Chủ nhật (WEEKDAY(): Thứ Hai = 0).
--   * Một lượt thăm thuộc kỳ nào là do GIỜ ĐẾN nằm trong kỳ đó.
--   * Kỳ dùng dạng nửa mở [đầu kỳ, đầu kỳ kế tiếp) để không bỏ sót lượt
--     nào trong ngày cuối kỳ (ví dụ Chủ nhật 23:30).
-- =====================================================================
USE KTX;

-- ---------------------------------------------------------------------
-- Chọn kỳ thống kê: đổi @ngay thành một ngày bất kỳ thuộc tuần/tháng cần xem
-- ---------------------------------------------------------------------
SET @ngay        := '2026-10-28';
SET @dau_tuan    := DATE_SUB(@ngay, INTERVAL WEEKDAY(@ngay) DAY);         -- Thứ Hai
SET @cuoi_tuan   := DATE_ADD(@dau_tuan, INTERVAL 7 DAY);                  -- Thứ Hai tuần sau (không gồm)
SET @dau_thang   := DATE_FORMAT(@ngay, '%Y-%m-01');
SET @cuoi_thang  := DATE_ADD(@dau_thang, INTERVAL 1 MONTH);

-- ---------------------------------------------------------------------
-- Q1. Lịch sử khách thăm của một sinh viên
-- ---------------------------------------------------------------------
SET @ma_sv := 'SV001';
SELECT lt.MaLuotTham, k.SoCMT AS CMT_Khach, k.HoTen AS TenKhach,
       lt.ThoiGianDen, lt.ThoiGianDi,
       IF(lt.ThoiGianDi IS NULL, 'Đang ở trong KTX',
          CONCAT(TIMESTAMPDIFF(MINUTE, lt.ThoiGianDen, lt.ThoiGianDi), ' phút')) AS ThoiLuong
FROM LuotTham lt
JOIN Khach k ON k.SoCMT = lt.SoCMTKhach
WHERE lt.MaSV = @ma_sv
ORDER BY lt.ThoiGianDen DESC;
-- Kỳ vọng với SV001: 3 dòng, mới nhất trước (27/10 15:00 GUEST0002, 26/10 08:30 GUEST0001, 20/10 14:00 GUEST0001)

-- ---------------------------------------------------------------------
-- Q2. THEO TUẦN: sinh viên + khách đến thăm họ + số lần mỗi khách đến
--     (yêu cầu đề bài: "thông tin sinh viên cùng thông tin các khách đến thăm
--      họ trong tuần, cùng số lần mỗi khách đến chơi")
-- ---------------------------------------------------------------------
SELECT sv.MaSV, sv.SoCMT AS CMT_SV, sv.NgaySinh AS NgaySinhSV, sv.Lop, sv.QueQuan,
       k.SoCMT AS CMT_Khach, k.HoTen AS TenKhach, k.NgaySinh AS NgaySinhKhach,
       COUNT(*) AS SoLanDen
FROM LuotTham lt
JOIN SinhVien sv ON sv.MaSV = lt.MaSV
JOIN Khach k     ON k.SoCMT = lt.SoCMTKhach
WHERE lt.ThoiGianDen >= @dau_tuan AND lt.ThoiGianDen < @cuoi_tuan
GROUP BY sv.MaSV, sv.SoCMT, sv.NgaySinh, sv.Lop, sv.QueQuan, k.SoCMT, k.HoTen, k.NgaySinh
ORDER BY sv.MaSV, SoLanDen DESC, k.HoTen;
-- Kỳ vọng tuần 26/10-01/11/2026 (@ngay = 2026-10-28): 4 dòng, mỗi dòng 1 lần:
--   SV001-GUEST0001, SV001-GUEST0002, SV002-GUEST0003, SV003-GUEST0002
-- Đổi @ngay = '2026-10-25' (Chủ nhật): tuần 19-25/10, 3 dòng:
--   SV001-GUEST0001, SV002-GUEST0001, SV002-GUEST0003 (mỗi dòng 1 lần)

-- ---------------------------------------------------------------------
-- Q3. THEO THÁNG: cùng nội dung như Q2 nhưng trong cả tháng
-- ---------------------------------------------------------------------
SELECT sv.MaSV, sv.SoCMT AS CMT_SV, sv.NgaySinh AS NgaySinhSV, sv.Lop, sv.QueQuan,
       k.SoCMT AS CMT_Khach, k.HoTen AS TenKhach, k.NgaySinh AS NgaySinhKhach,
       COUNT(*) AS SoLanDen
FROM LuotTham lt
JOIN SinhVien sv ON sv.MaSV = lt.MaSV
JOIN Khach k     ON k.SoCMT = lt.SoCMTKhach
WHERE lt.ThoiGianDen >= @dau_thang AND lt.ThoiGianDen < @cuoi_thang
GROUP BY sv.MaSV, sv.SoCMT, sv.NgaySinh, sv.Lop, sv.QueQuan, k.SoCMT, k.HoTen, k.NgaySinh
ORDER BY sv.MaSV, SoLanDen DESC, k.HoTen;
-- Kỳ vọng tháng 10/2026: 5 dòng:
--   SV001-GUEST0001 = 2, SV001-GUEST0002 = 1, SV002-GUEST0001 = 1,
--   SV002-GUEST0003 = 2, SV003-GUEST0002 = 1

-- ---------------------------------------------------------------------
-- Q4. Đếm số lần MỖI KHÁCH đến KTX trong tháng (tổng, không tách theo sinh viên)
-- ---------------------------------------------------------------------
SELECT k.SoCMT, k.HoTen, k.NgaySinh,
       COUNT(*) AS TongSoLan, COUNT(DISTINCT lt.MaSV) AS SoSinhVienDuocTham
FROM LuotTham lt
JOIN Khach k ON k.SoCMT = lt.SoCMTKhach
WHERE lt.ThoiGianDen >= @dau_thang AND lt.ThoiGianDen < @cuoi_thang
GROUP BY k.SoCMT, k.HoTen, k.NgaySinh
ORDER BY TongSoLan DESC, k.HoTen;
-- Kỳ vọng tháng 10/2026: GUEST0001 = 3 lần (2 sinh viên), GUEST0003 = 2 lần (1 sinh viên),
--                        GUEST0002 = 2 lần (2 sinh viên). GUEST0004 không xuất hiện.

-- ---------------------------------------------------------------------
-- Q5. Số lượt thăm và số khách khác nhau của TỪNG sinh viên trong khoảng thời gian
--     (LEFT JOIN: sinh viên không có khách vẫn hiện với 0)
-- ---------------------------------------------------------------------
SET @tu_ngay  := '2026-10-01';
SET @den_ngay := '2026-10-31';                      -- gồm cả ngày này
SELECT sv.MaSV, sv.Lop, sv.QueQuan,
       COUNT(lt.MaLuotTham) AS SoLuotTham,
       COUNT(DISTINCT lt.SoCMTKhach) AS SoKhachKhacNhau
FROM SinhVien sv
LEFT JOIN LuotTham lt
       ON lt.MaSV = sv.MaSV
      AND lt.ThoiGianDen >= @tu_ngay
      AND lt.ThoiGianDen <  DATE_ADD(@den_ngay, INTERVAL 1 DAY)
GROUP BY sv.MaSV, sv.Lop, sv.QueQuan
ORDER BY SoLuotTham DESC, sv.MaSV;
-- Kỳ vọng 01-31/10/2026: SV001 = 3 lượt/2 khách, SV002 = 3 lượt/2 khách, SV003 = 1 lượt/1 khách.

-- ---------------------------------------------------------------------
-- Q6. Khách đang ở trong KTX (chưa ghi nhận giờ về)
-- ---------------------------------------------------------------------
SELECT lt.MaLuotTham, k.SoCMT, k.HoTen, lt.MaSV, lt.ThoiGianDen,
       TIMESTAMPDIFF(MINUTE, lt.ThoiGianDen, NOW()) AS DaOPhut
FROM LuotTham lt
JOIN Khach k ON k.SoCMT = lt.SoCMTKhach
WHERE lt.ThoiGianDi IS NULL
ORDER BY lt.ThoiGianDen;
-- Kỳ vọng: 1 dòng (GUEST0003 thăm SV002 từ 29/10/2026 09:30).

-- ---------------------------------------------------------------------
-- Q7. Tìm kiếm theo từ khóa (số CMT khách, tên khách hoặc mã sinh viên)
-- ---------------------------------------------------------------------
SET @tu_khoa := CONCAT('%', 'Mai', '%');
SELECT lt.MaLuotTham, k.SoCMT, k.HoTen, lt.MaSV, lt.ThoiGianDen, lt.ThoiGianDi
FROM LuotTham lt
JOIN Khach k ON k.SoCMT = lt.SoCMTKhach
WHERE lt.SoCMTKhach LIKE @tu_khoa OR k.HoTen LIKE @tu_khoa OR lt.MaSV LIKE @tu_khoa
ORDER BY lt.ThoiGianDen DESC;
-- Kỳ vọng với 'Mai': 2 dòng của khách Trần Thị Mai (GUEST0002).

-- ---------------------------------------------------------------------
-- Q8. Kiểm tra chồng lấn thời gian của một khách (dùng trong KhachThamService)
--     Trả về dòng nào => khoảng [@den, @di) bị trùng với lượt thăm đã có.
--     @di = NULL nghĩa là lượt mới chưa kết thúc.
-- ---------------------------------------------------------------------
SET @cmt := 'GUEST0001';
SET @den := '2026-10-20 15:00:00';
SET @di  := NULL;
SELECT lt.*
FROM LuotTham lt
WHERE lt.SoCMTKhach = @cmt
  AND lt.ThoiGianDen < COALESCE(@di, '9999-01-01 00:00:00')
  AND COALESCE(lt.ThoiGianDi, '9999-01-01 00:00:00') > @den;
-- Kỳ vọng: có dòng (khách GUEST0001 còn ở KTX đến 16:30 ngày 20/10 và các lượt sau đó).
