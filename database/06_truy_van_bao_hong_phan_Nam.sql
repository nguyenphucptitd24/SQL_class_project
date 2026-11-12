-- =====================================================================
-- MODULE BÁO HỎNG VÀ BẢO TRÌ PHÒNG - PHẦN CỦA NAM (Task 4.4: Truy vấn SQL)
-- Chạy SAU 01_ktx_schema.sql và 05_bao_hong_phan_Nam.sql.
-- Kết quả kỳ vọng ghi bên dưới mỗi truy vấn ứng với dữ liệu mẫu trong file 05.
--
-- Quy ước:
--   * Thống kê theo kỳ lấy theo NgayBao (ngày báo) nằm trong [@tu, @den], gồm cả ngày @den.
--   * "Chưa hoàn thành" = Mới báo + Đang xử lý. "Đã kết thúc" = Đã hoàn thành + Từ chối.
--   * Thời gian xử lý = NgayXuLy - NgayBao; chỉ tính báo hỏng ĐÃ HOÀN THÀNH
--     (báo hỏng bị từ chối không tính vì không có công việc sửa chữa).
--   * Khi sắp xếp theo mức độ: Khẩn cấp, Cao, Trung bình, Thấp.
-- =====================================================================
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;   -- trùng collation của CSDL KTX, tránh lỗi "Illegal mix of collations" khi so sánh với hằng/biến
USE KTX;

-- ---------------------------------------------------------------------
-- Tham số: đổi giá trị ở đây rồi chạy các truy vấn bên dưới
-- ---------------------------------------------------------------------
SET @tu       := '2026-09-01';
SET @den      := '2026-11-30';                          -- gồm cả ngày này
SET @den_sau  := DATE_ADD(@den, INTERVAL 1 DAY);        -- cận trên không gồm, để không sót báo hỏng trong ngày cuối
SET @so_phong := 'P101';
SET @ma_sv    := 'SV001';
SET @ma_bao_hong := 1;

-- ---------------------------------------------------------------------
-- Q1. Danh sách báo hỏng MỚI TIẾP NHẬN (chưa ai xử lý), mức độ nặng nhất lên trước
-- ---------------------------------------------------------------------
SELECT b.MaBaoHong, b.SoPhong, COALESCE(b.MaSV, '(nhân viên phát hiện)') AS NguoiBao,
       b.LoaiSuCo, b.MucDo, b.NgayBao, SUBSTRING(b.MoTa, 1, 60) AS MoTaNgan
FROM BaoHong b
WHERE b.TrangThai = 'Mới báo'
ORDER BY CASE b.MucDo WHEN 'Khẩn cấp' THEN 1 WHEN 'Cao' THEN 2 WHEN 'Trung bình' THEN 3 ELSE 4 END,
         b.NgayBao;
-- Kỳ vọng: 2 dòng, theo thứ tự mã 7 (P102, Cao), 8 (P101, Thấp).

-- ---------------------------------------------------------------------
-- Q2. Sự cố ĐANG XỬ LÝ / CHƯA HOÀN THÀNH (Mới báo + Đang xử lý), kèm số giờ đã chờ
-- ---------------------------------------------------------------------
SELECT b.MaBaoHong, b.SoPhong, b.LoaiSuCo, b.MucDo, b.TrangThai, b.NhanVienXuLy, b.NgayBao,
       TIMESTAMPDIFF(HOUR, b.NgayBao, NOW()) AS DaChoGio
FROM BaoHong b
WHERE b.TrangThai IN ('Mới báo', 'Đang xử lý')
ORDER BY CASE b.MucDo WHEN 'Khẩn cấp' THEN 1 WHEN 'Cao' THEN 2 WHEN 'Trung bình' THEN 3 ELSE 4 END,
         b.NgayBao;
-- Kỳ vọng: 4 dòng, theo thứ tự mã 6 (Khẩn cấp), 7 (Cao), 5 (Thấp, báo sớm hơn), 8 (Thấp).

-- ---------------------------------------------------------------------
-- Q3. Lịch sử báo hỏng THEO PHÒNG (mới nhất trước), kèm thời gian xử lý nếu đã kết thúc
-- ---------------------------------------------------------------------
SELECT b.MaBaoHong, b.NgayBao, b.LoaiSuCo, b.MucDo, b.TrangThai,
       COALESCE(b.MaSV, '(nhân viên phát hiện)') AS NguoiBao,
       b.NhanVienXuLy, b.NgayXuLy,
       ROUND(TIMESTAMPDIFF(MINUTE, b.NgayBao, b.NgayXuLy) / 60, 1) AS ThoiGianXuLyGio,
       b.KetQuaXuLy
FROM BaoHong b
WHERE b.SoPhong = @so_phong
ORDER BY b.NgayBao DESC;
-- Kỳ vọng với P101: 4 dòng, theo thứ tự mã 8, 5, 2, 1.

-- ---------------------------------------------------------------------
-- Q4. Lịch sử báo hỏng THEO SINH VIÊN (mới nhất trước)
-- ---------------------------------------------------------------------
SELECT b.MaBaoHong, b.SoPhong, b.NgayBao, b.LoaiSuCo, b.MucDo, b.TrangThai, b.NgayXuLy, b.KetQuaXuLy
FROM BaoHong b
WHERE b.MaSV = @ma_sv
ORDER BY b.NgayBao DESC;
-- Kỳ vọng với SV001: 2 dòng, theo thứ tự mã 5 (Đang xử lý), 1 (Đã hoàn thành).

-- ---------------------------------------------------------------------
-- Q5. Thống kê sự cố THEO LOẠI trong kỳ
-- ---------------------------------------------------------------------
SELECT b.LoaiSuCo,
       COUNT(*) AS SoSuCo,
       SUM(b.TrangThai IN ('Mới báo', 'Đang xử lý')) AS ChuaHoanThanh,
       SUM(b.TrangThai = 'Đã hoàn thành') AS DaHoanThanh,
       SUM(b.TrangThai = 'Từ chối') AS TuChoi
FROM BaoHong b
WHERE b.NgayBao >= @tu AND b.NgayBao < @den_sau
GROUP BY b.LoaiSuCo
ORDER BY SoSuCo DESC, b.LoaiSuCo;
-- Kỳ vọng 01/09-30/11/2026 (tổng 8): Điện 2 (chưa 1, xong 1), Nước 2 (chưa 1, xong 1),
--   Thiết bị 2 (chưa 1, xong 1), Khác 1 (chưa 1), Vệ sinh 1 (từ chối 1).

-- ---------------------------------------------------------------------
-- Q6. Thống kê sự cố THEO MỨC ĐỘ trong kỳ
-- ---------------------------------------------------------------------
SELECT b.MucDo,
       COUNT(*) AS SoSuCo,
       SUM(b.TrangThai IN ('Mới báo', 'Đang xử lý')) AS ChuaHoanThanh,
       SUM(b.TrangThai = 'Đã hoàn thành') AS DaHoanThanh,
       SUM(b.TrangThai = 'Từ chối') AS TuChoi
FROM BaoHong b
WHERE b.NgayBao >= @tu AND b.NgayBao < @den_sau
GROUP BY b.MucDo
ORDER BY CASE b.MucDo WHEN 'Khẩn cấp' THEN 1 WHEN 'Cao' THEN 2 WHEN 'Trung bình' THEN 3 ELSE 4 END;
-- Kỳ vọng: Khẩn cấp 1, Cao 2, Trung bình 2, Thấp 3.

-- ---------------------------------------------------------------------
-- Q7. Thống kê sự cố THEO THÁNG trong kỳ
-- ---------------------------------------------------------------------
SELECT DATE_FORMAT(b.NgayBao, '%Y-%m') AS Thang,
       COUNT(*) AS SoSuCo,
       SUM(b.TrangThai IN ('Mới báo', 'Đang xử lý')) AS ChuaHoanThanh,
       SUM(b.TrangThai = 'Đã hoàn thành') AS DaHoanThanh,
       SUM(b.TrangThai = 'Từ chối') AS TuChoi
FROM BaoHong b
WHERE b.NgayBao >= @tu AND b.NgayBao < @den_sau
GROUP BY DATE_FORMAT(b.NgayBao, '%Y-%m')
ORDER BY Thang;
-- Kỳ vọng: 2026-09 = 2, 2026-10 = 3, 2026-11 = 3.

-- ---------------------------------------------------------------------
-- Q8. THỜI GIAN XỬ LÝ TRUNG BÌNH (giờ) của các báo hỏng đã hoàn thành trong kỳ
--     Chỉ tính dòng có đủ NgayBao và NgayXuLy. Nếu chưa có báo hỏng nào hoàn thành thì kết quả NULL.
-- ---------------------------------------------------------------------
SELECT COUNT(*) AS SoBaoHongDaHoanThanh,
       ROUND(AVG(TIMESTAMPDIFF(MINUTE, b.NgayBao, b.NgayXuLy)) / 60, 1) AS TrungBinhGio
FROM BaoHong b
WHERE b.TrangThai = 'Đã hoàn thành' AND b.NgayXuLy IS NOT NULL
  AND b.NgayBao >= @tu AND b.NgayBao < @den_sau;
-- Kỳ vọng: 3 báo hỏng (6,5 giờ; 38,8 giờ; 60,5 giờ) -> trung bình 35,3 giờ.

-- Q8b. Cùng chỉ số, tách theo mức độ
SELECT b.MucDo, COUNT(*) AS SoBaoHong,
       ROUND(AVG(TIMESTAMPDIFF(MINUTE, b.NgayBao, b.NgayXuLy)) / 60, 1) AS TrungBinhGio
FROM BaoHong b
WHERE b.TrangThai = 'Đã hoàn thành' AND b.NgayXuLy IS NOT NULL
  AND b.NgayBao >= @tu AND b.NgayBao < @den_sau
GROUP BY b.MucDo
ORDER BY CASE b.MucDo WHEN 'Khẩn cấp' THEN 1 WHEN 'Cao' THEN 2 WHEN 'Trung bình' THEN 3 ELSE 4 END;
-- Kỳ vọng: Cao 1 báo hỏng (6,5 giờ), Trung bình 1 (38,8 giờ), Thấp 1 (60,5 giờ).

-- ---------------------------------------------------------------------
-- Q9. Các bước xử lý (lịch sử bảo trì) của MỘT báo hỏng
-- ---------------------------------------------------------------------
SELECT l.ThoiGian, COALESCE(l.TrangThaiCu, '(tạo mới)') AS TuTrangThai, l.TrangThaiMoi AS SangTrangThai,
       l.NhanVien, l.GhiChu
FROM LichSuBaoHong l
WHERE l.MaBaoHong = @ma_bao_hong
ORDER BY l.ThoiGian, l.MaLichSu;
-- Kỳ vọng với mã 1: 3 dòng (tạo mới -> Mới báo; Mới báo -> Đang xử lý; Đang xử lý -> Đã hoàn thành).

-- ---------------------------------------------------------------------
-- Q10. Cảnh báo: sự cố mức CAO / KHẨN CẤP chưa hoàn thành (giao diện hiển thị cảnh báo từ danh sách này)
-- ---------------------------------------------------------------------
SELECT b.MaBaoHong, b.SoPhong, b.LoaiSuCo, b.MucDo, b.TrangThai, b.NgayBao
FROM BaoHong b
WHERE b.MucDo IN ('Cao', 'Khẩn cấp') AND b.TrangThai IN ('Mới báo', 'Đang xử lý')
ORDER BY CASE b.MucDo WHEN 'Khẩn cấp' THEN 1 ELSE 2 END, b.NgayBao;
-- Kỳ vọng: 2 dòng, mã 6 (Khẩn cấp, Đang xử lý) và mã 7 (Cao, Mới báo).
