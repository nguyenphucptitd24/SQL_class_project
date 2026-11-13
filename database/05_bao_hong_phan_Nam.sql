-- =====================================================================
-- MODULE BÁO HỎNG VÀ BẢO TRÌ PHÒNG - PHẦN CỦA NAM (Task 4.2: Thiết kế CSDL)
-- Chạy SAU 01_ktx_schema.sql (cần bảng Phong, SinhVien và dữ liệu mẫu phòng P101-P103,
-- sinh viên SV001-SV003). Chạy trong MySQL Workbench (có dùng DELIMITER cho trigger).
-- MySQL 8.0.16+ (cần để CHECK có hiệu lực). Chạy lại nhiều lần được.
-- Lưu file ở dạng UTF-8 để giữ đúng tiếng Việt có dấu.
-- =====================================================================
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;   -- trùng collation của CSDL KTX, tránh lỗi "Illegal mix of collations" khi so sánh với hằng/biến
USE KTX;

-- ---------------------------------------------------------------------
-- 1. THIẾT KẾ
--
-- BaoHong(MaBaoHong PK, SoPhong FK -> Phong, MaSV FK -> SinhVien NULL được,
--         LoaiSuCo, MoTa, MucDo, TrangThai, NgayBao, NgayXuLy, NhanVienXuLy, KetQuaXuLy)
-- LichSuBaoHong(MaLichSu PK, MaBaoHong FK -> BaoHong, TrangThaiCu, TrangThaiMoi,
--               NhanVien, GhiChu, ThoiGian)
--
-- Quan hệ: Phong 1-N BaoHong; SinhVien 1-N BaoHong (MaSV có thể NULL khi sự cố do
--          nhân viên phát hiện); BaoHong 1-N LichSuBaoHong.
--
-- So với danh sách cột dự kiến ban đầu, có 2 điểm bổ sung (cần trưởng nhóm duyệt):
--   * Thêm cột KetQuaXuLy: mô tả kết quả xử lý (cột MoTa là mô tả SỰ CỐ, không dùng lẫn).
--   * Thêm bảng LichSuBaoHong: lưu từng lần đổi trạng thái. Nếu chỉ có một cột TrangThai
--     và một NgayXuLy thì không thể "lưu lịch sử xử lý" như yêu cầu.
--
-- Không lưu trùng thông tin:
--   * Người báo chỉ lưu MaSV (họ tên, lớp... lấy bằng phép nối với SinhVien); phòng chỉ lưu SoPhong.
--   * Nhân viên xử lý là chuỗi tự do vì hệ thống chưa có bảng nhân viên. BaoHong giữ người xử lý
--     hiện tại, LichSuBaoHong giữ người thực hiện ở từng bước (là dữ liệu lịch sử, không trùng).
--
-- Danh mục giá trị (đổi ở đúng một chỗ: các CHECK bên dưới, so sánh nhị phân để bắt buộc đúng dấu):
--   LoaiSuCo : Điện, Nước, Thiết bị, Vệ sinh, Khác
--   MucDo    : Thấp, Trung bình, Cao, Khẩn cấp   (Cao và Khẩn cấp là mức cảnh báo ở giao diện)
--   TrangThai: Mới báo, Đang xử lý, Đã hoàn thành, Từ chối
--   Chuyển trạng thái hợp lệ: Mới báo -> Đang xử lý | Từ chối ; Đang xử lý -> Đã hoàn thành | Từ chối.
--
-- Ý nghĩa các cột thời gian và người xử lý:
--   NgayBao  : lúc báo sự cố (luôn có).
--   NgayXuLy : lúc KẾT THÚC xử lý (Đã hoàn thành hoặc Từ chối); NULL khi còn Mới báo / Đang xử lý.
--              Thời gian xử lý = NgayXuLy - NgayBao.
--   NhanVienXuLy: NULL khi Mới báo; bắt buộc từ Đang xử lý trở đi.
--   KetQuaXuLy: bắt buộc khi Đã hoàn thành hoặc Từ chối (ghi kết quả hoặc lý do từ chối).
--
-- Chỉ mục: theo phòng (SoPhong), trạng thái (TrangThai) và ngày báo (NgayBao); MaSV đã có chỉ mục
-- tự tạo bởi khóa ngoại.
--
-- Các quy tắc cần truy vấn bảng khác nên do KhachThamService/BaoHongService kiểm tra (task 4.3):
--   * Sinh viên chỉ báo hỏng cho phòng mình đang ở (ThuePhong có hiệu lực tại NgayBao).
--   * Thông báo lỗi thân thiện khi đổi trạng thái sai thứ tự (trigger bên dưới chỉ là lớp chặn cuối).
--   * Ghi dòng LichSuBaoHong trong cùng transaction với mỗi lần tạo/đổi trạng thái.
-- ---------------------------------------------------------------------

-- 2. TẠO BẢNG
CREATE TABLE IF NOT EXISTS BaoHong (
  MaBaoHong BIGINT PRIMARY KEY AUTO_INCREMENT,
  SoPhong VARCHAR(20) NOT NULL,
  MaSV VARCHAR(20) NULL COMMENT 'NULL nghia la su co do nhan vien phat hien',
  LoaiSuCo VARCHAR(100) NOT NULL,
  MoTa TEXT NOT NULL,
  MucDo VARCHAR(20) NOT NULL DEFAULT 'Trung bình',
  TrangThai VARCHAR(30) NOT NULL DEFAULT 'Mới báo',
  NgayBao DATETIME NOT NULL,
  NgayXuLy DATETIME NULL COMMENT 'Thoi diem ket thuc xu ly (hoan thanh hoac tu choi); NULL nghia la chua ket thuc',
  NhanVienXuLy VARCHAR(100) NULL,
  KetQuaXuLy VARCHAR(500) NULL,
  CONSTRAINT fk_baohong_phong FOREIGN KEY (SoPhong) REFERENCES Phong(SoPhong),
  CONSTRAINT fk_baohong_sv FOREIGN KEY (MaSV) REFERENCES SinhVien(MaSV),
  CONSTRAINT chk_baohong_loai CHECK (LoaiSuCo COLLATE utf8mb4_bin IN ('Điện', 'Nước', 'Thiết bị', 'Vệ sinh', 'Khác')),
  CONSTRAINT chk_baohong_mucdo CHECK (MucDo COLLATE utf8mb4_bin IN ('Thấp', 'Trung bình', 'Cao', 'Khẩn cấp')),
  CONSTRAINT chk_baohong_trangthai CHECK (TrangThai COLLATE utf8mb4_bin IN ('Mới báo', 'Đang xử lý', 'Đã hoàn thành', 'Từ chối')),
  CONSTRAINT chk_baohong_ngay CHECK (NgayXuLy IS NULL OR NgayXuLy >= NgayBao),
  CONSTRAINT chk_baohong_dulieu CHECK (
        (TrangThai = 'Mới báo' AND NhanVienXuLy IS NULL AND NgayXuLy IS NULL)
     OR (TrangThai = 'Đang xử lý' AND NhanVienXuLy IS NOT NULL AND NgayXuLy IS NULL)
     OR (TrangThai IN ('Đã hoàn thành', 'Từ chối') AND NhanVienXuLy IS NOT NULL AND NgayXuLy IS NOT NULL
         AND KetQuaXuLy IS NOT NULL AND CHAR_LENGTH(TRIM(KetQuaXuLy)) > 0)),
  INDEX idx_baohong_phong (SoPhong),
  INDEX idx_baohong_trangthai (TrangThai),
  INDEX idx_baohong_ngaybao (NgayBao)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS LichSuBaoHong (
  MaLichSu BIGINT PRIMARY KEY AUTO_INCREMENT,
  MaBaoHong BIGINT NOT NULL,
  TrangThaiCu VARCHAR(30) NULL COMMENT 'NULL nghia la dong ghi nhan luc tao moi',
  TrangThaiMoi VARCHAR(30) NOT NULL,
  NhanVien VARCHAR(100) NULL,
  GhiChu VARCHAR(500) NULL,
  ThoiGian DATETIME NOT NULL,
  CONSTRAINT fk_lichsubh_baohong FOREIGN KEY (MaBaoHong) REFERENCES BaoHong(MaBaoHong) ON DELETE CASCADE,
  CONSTRAINT chk_lichsubh_trangthai CHECK (TrangThaiMoi COLLATE utf8mb4_bin IN ('Mới báo', 'Đang xử lý', 'Đã hoàn thành', 'Từ chối') AND (TrangThaiCu IS NULL OR TrangThaiCu COLLATE utf8mb4_bin IN ('Mới báo', 'Đang xử lý', 'Đã hoàn thành', 'Từ chối'))),
  INDEX idx_lichsubh_baohong (MaBaoHong, ThoiGian)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. TRIGGER BẢO VỆ LỊCH SỬ (lớp chặn cuối cùng ở mức CSDL)
--   * Chỉ xóa được báo hỏng còn ở trạng thái Mới báo (lịch sử đi kèm bị xóa theo).
--     Báo hỏng đã xử lý/hoàn thành/từ chối không xóa được.
--   * Không cho đổi trạng thái sai thứ tự (ví dụ mở lại báo hỏng đã hoàn thành).
-- ---------------------------------------------------------------------
DELIMITER $$

DROP TRIGGER IF EXISTS trg_baohong_chan_xoa$$
CREATE TRIGGER trg_baohong_chan_xoa BEFORE DELETE ON BaoHong
FOR EACH ROW
BEGIN
  IF OLD.TrangThai <> 'Mới báo' THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Chỉ được xóa báo hỏng ở trạng thái Mới báo; báo hỏng đã xử lý phải giữ lại để lưu lịch sử.';
  END IF;
END$$

DROP TRIGGER IF EXISTS trg_baohong_chan_doi_trang_thai$$
CREATE TRIGGER trg_baohong_chan_doi_trang_thai BEFORE UPDATE ON BaoHong
FOR EACH ROW
BEGIN
  IF NEW.TrangThai <> OLD.TrangThai THEN
    IF NOT ((OLD.TrangThai = 'Mới báo' AND NEW.TrangThai IN ('Đang xử lý', 'Từ chối'))
         OR (OLD.TrangThai = 'Đang xử lý' AND NEW.TrangThai IN ('Đã hoàn thành', 'Từ chối'))) THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Chỉ được chuyển: Mới báo -> Đang xử lý/Từ chối; Đang xử lý -> Đã hoàn thành/Từ chối.';
    END IF;
  END IF;
END$$

DELIMITER ;

-- ---------------------------------------------------------------------
-- 4. DỮ LIỆU MẪU (giả lập): 8 báo hỏng ở đủ 4 trạng thái, 4 mức độ, 5 loại sự cố,
--    trong đó 1 báo hỏng do nhân viên phát hiện (MaSV = NULL) ở phòng chưa có người ở.
--    Mỗi báo hỏng có dòng lịch sử tạo mới và từng bước xử lý.
-- ---------------------------------------------------------------------
INSERT INTO BaoHong(SoPhong, MaSV, LoaiSuCo, MoTa, MucDo, TrangThai, NgayBao, NgayXuLy, NhanVienXuLy, KetQuaXuLy)
SELECT v.p, v.sv, v.loai, v.mt, v.md, v.tt, v.nb, v.nx, v.nv, v.kq
FROM (
    SELECT 'P101' AS p, 'SV001' AS sv, 'Điện' AS loai, 'Ổ cắm gần giường tầng 2 bị chập, có mùi khét' AS mt, 'Cao' AS md, 'Đã hoàn thành' AS tt, '2026-09-12 08:30:00' AS nb, '2026-09-12 15:00:00' AS nx, 'Trần Văn Bảo' AS nv, 'Đã thay ổ cắm mới và kiểm tra an toàn điện toàn phòng' AS kq
    UNION ALL SELECT 'P101', 'SV002', 'Nước', 'Vòi nước ở lavabo bị rò, nhỏ giọt liên tục', 'Trung bình', 'Đã hoàn thành', '2026-09-28 19:10:00', '2026-09-30 10:00:00', 'Lê Thị Hoa', 'Đã thay gioăng vòi nước'
    UNION ALL SELECT 'P102', 'SV003', 'Thiết bị', 'Quạt trần kêu to và quay chậm', 'Thấp', 'Đã hoàn thành', '2026-10-05 21:00:00', '2026-10-08 09:30:00', 'Trần Văn Bảo', 'Đã tra dầu và thay tụ quạt'
    UNION ALL SELECT 'P102', 'SV003', 'Vệ sinh', 'Cống nhà vệ sinh thoát nước chậm', 'Trung bình', 'Từ chối', '2026-10-14 07:45:00', '2026-10-14 14:00:00', 'Lê Thị Hoa', 'Kiểm tra thực tế thấy cống thoát nước bình thường, không có sự cố'
    UNION ALL SELECT 'P101', 'SV001', 'Điện', 'Đèn phòng chập chờn, lúc sáng lúc tắt', 'Thấp', 'Đang xử lý', '2026-10-27 20:15:00', NULL, 'Trần Văn Bảo', NULL
    UNION ALL SELECT 'P103', NULL, 'Nước', 'Nhân viên phát hiện rò rỉ đường ống âm tường, trần bị ẩm', 'Khẩn cấp', 'Đang xử lý', '2026-11-02 09:00:00', NULL, 'Lê Thị Hoa', NULL
    UNION ALL SELECT 'P102', 'SV003', 'Thiết bị', 'Cửa sổ bị kẹt, không đóng được', 'Cao', 'Mới báo', '2026-11-03 22:30:00', NULL, NULL, NULL
    UNION ALL SELECT 'P101', 'SV002', 'Khác', 'Khóa tủ cá nhân bị hỏng', 'Thấp', 'Mới báo', '2026-11-04 08:05:00', NULL, NULL, NULL
) AS v
WHERE NOT EXISTS (SELECT 1 FROM BaoHong b WHERE b.SoPhong = v.p AND b.NgayBao = v.nb);

INSERT INTO LichSuBaoHong(MaBaoHong, TrangThaiCu, TrangThaiMoi, NhanVien, GhiChu, ThoiGian)
SELECT b.MaBaoHong, v.cu, v.moi, v.nv, v.ghi, v.tg
FROM (
    SELECT 'P101' AS p, '2026-09-12 08:30:00' AS nb, NULL AS cu, 'Mới báo' AS moi, NULL AS nv, 'Sinh viên gửi báo hỏng' AS ghi, '2026-09-12 08:30:00' AS tg
    UNION ALL SELECT 'P101', '2026-09-12 08:30:00', 'Mới báo', 'Đang xử lý', 'Trần Văn Bảo', 'Đã tiếp nhận, cử nhân viên đến kiểm tra', '2026-09-12 09:15:00'
    UNION ALL SELECT 'P101', '2026-09-12 08:30:00', 'Đang xử lý', 'Đã hoàn thành', 'Trần Văn Bảo', 'Đã thay ổ cắm mới và kiểm tra an toàn điện toàn phòng', '2026-09-12 15:00:00'
    UNION ALL SELECT 'P101', '2026-09-28 19:10:00', NULL, 'Mới báo', NULL, 'Sinh viên gửi báo hỏng', '2026-09-28 19:10:00'
    UNION ALL SELECT 'P101', '2026-09-28 19:10:00', 'Mới báo', 'Đang xử lý', 'Lê Thị Hoa', 'Đã tiếp nhận, hẹn xử lý trong ngày làm việc kế tiếp', '2026-09-29 08:00:00'
    UNION ALL SELECT 'P101', '2026-09-28 19:10:00', 'Đang xử lý', 'Đã hoàn thành', 'Lê Thị Hoa', 'Đã thay gioăng vòi nước', '2026-09-30 10:00:00'
    UNION ALL SELECT 'P102', '2026-10-05 21:00:00', NULL, 'Mới báo', NULL, 'Sinh viên gửi báo hỏng', '2026-10-05 21:00:00'
    UNION ALL SELECT 'P102', '2026-10-05 21:00:00', 'Mới báo', 'Đang xử lý', 'Trần Văn Bảo', 'Đã tiếp nhận', '2026-10-07 08:30:00'
    UNION ALL SELECT 'P102', '2026-10-05 21:00:00', 'Đang xử lý', 'Đã hoàn thành', 'Trần Văn Bảo', 'Đã tra dầu và thay tụ quạt', '2026-10-08 09:30:00'
    UNION ALL SELECT 'P102', '2026-10-14 07:45:00', NULL, 'Mới báo', NULL, 'Sinh viên gửi báo hỏng', '2026-10-14 07:45:00'
    UNION ALL SELECT 'P102', '2026-10-14 07:45:00', 'Mới báo', 'Từ chối', 'Lê Thị Hoa', 'Kiểm tra thực tế thấy cống thoát nước bình thường, không có sự cố', '2026-10-14 14:00:00'
    UNION ALL SELECT 'P101', '2026-10-27 20:15:00', NULL, 'Mới báo', NULL, 'Sinh viên gửi báo hỏng', '2026-10-27 20:15:00'
    UNION ALL SELECT 'P101', '2026-10-27 20:15:00', 'Mới báo', 'Đang xử lý', 'Trần Văn Bảo', 'Đã tiếp nhận, chờ linh kiện thay thế', '2026-10-28 08:00:00'
    UNION ALL SELECT 'P103', '2026-11-02 09:00:00', NULL, 'Mới báo', NULL, 'Nhân viên phát hiện và ghi nhận sự cố', '2026-11-02 09:00:00'
    UNION ALL SELECT 'P103', '2026-11-02 09:00:00', 'Mới báo', 'Đang xử lý', 'Lê Thị Hoa', 'Đã khóa van tổng khu vực, đang kiểm tra đường ống', '2026-11-02 09:30:00'
    UNION ALL SELECT 'P102', '2026-11-03 22:30:00', NULL, 'Mới báo', NULL, 'Sinh viên gửi báo hỏng', '2026-11-03 22:30:00'
    UNION ALL SELECT 'P101', '2026-11-04 08:05:00', NULL, 'Mới báo', NULL, 'Sinh viên gửi báo hỏng', '2026-11-04 08:05:00'
) AS v
JOIN BaoHong b ON b.SoPhong = v.p AND b.NgayBao = v.nb
WHERE NOT EXISTS (SELECT 1 FROM LichSuBaoHong l
                  WHERE l.MaBaoHong = b.MaBaoHong AND l.ThoiGian = v.tg AND l.TrangThaiMoi = v.moi);

-- ---------------------------------------------------------------------
-- 5. KIỂM TRA SAU KHI CHẠY
-- ---------------------------------------------------------------------
-- 4.1 Cấu trúc và chỉ mục
SHOW CREATE TABLE BaoHong;
SHOW INDEX FROM BaoHong;
SHOW CREATE TABLE LichSuBaoHong;

-- 4.2 Số dòng dữ liệu mẫu: kỳ vọng 8 báo hỏng và 17 dòng lịch sử (nếu chưa thêm dữ liệu khác)
SELECT (SELECT COUNT(*) FROM BaoHong) AS SoBaoHong, (SELECT COUNT(*) FROM LichSuBaoHong) AS SoDongLichSu;

-- 4.3 Nhất quán giữa BaoHong và lịch sử: cả hai truy vấn dưới đây phải trả về 0 dòng
-- (a) trạng thái hiện tại phải trùng trạng thái ở dòng lịch sử mới nhất
SELECT b.MaBaoHong, b.TrangThai
FROM BaoHong b
JOIN LichSuBaoHong l ON l.MaLichSu = (SELECT l2.MaLichSu FROM LichSuBaoHong l2
                                      WHERE l2.MaBaoHong = b.MaBaoHong
                                      ORDER BY l2.ThoiGian DESC, l2.MaLichSu DESC LIMIT 1)
WHERE l.TrangThaiMoi <> b.TrangThai;
-- (b) báo hỏng đã kết thúc: NgayXuLy phải bằng thời gian của dòng lịch sử cuối
SELECT b.MaBaoHong
FROM BaoHong b
WHERE b.NgayXuLy IS NOT NULL
  AND b.NgayXuLy <> (SELECT MAX(l.ThoiGian) FROM LichSuBaoHong l WHERE l.MaBaoHong = b.MaBaoHong);

-- 4.4 Thử vi phạm ràng buộc: mỗi câu dưới đây PHẢI báo lỗi (bỏ dấu -- ở đầu dòng để thử, mỗi lần một câu)
-- (a) mức độ ngoài danh mục -> lỗi chk_baohong_mucdo
-- INSERT INTO BaoHong(SoPhong, MaSV, LoaiSuCo, MoTa, MucDo, TrangThai, NgayBao)
-- VALUES ('P101', 'SV001', 'Điện', 'Thử mức độ sai', 'Gấp', 'Mới báo', '2026-11-04 09:00:00');
-- (b) loại sự cố viết không dấu -> lỗi chk_baohong_loai (so sánh nhị phân nên 'Dien' bị từ chối)
-- INSERT INTO BaoHong(SoPhong, MaSV, LoaiSuCo, MoTa, MucDo, TrangThai, NgayBao)
-- VALUES ('P101', 'SV001', 'Dien', 'Thử loại không dấu', 'Thấp', 'Mới báo', '2026-11-04 09:00:00');
-- (c) ngày xử lý trước ngày báo -> lỗi chk_baohong_ngay
-- INSERT INTO BaoHong(SoPhong, MaSV, LoaiSuCo, MoTa, MucDo, TrangThai, NgayBao, NgayXuLy, NhanVienXuLy, KetQuaXuLy)
-- VALUES ('P101', 'SV001', 'Điện', 'Thử ngày sai', 'Thấp', 'Đã hoàn thành', '2026-11-04 09:00:00', '2026-11-04 08:00:00', 'Trần Văn Bảo', 'Thử');
-- (d) hoàn thành mà thiếu kết quả xử lý -> lỗi chk_baohong_dulieu
-- INSERT INTO BaoHong(SoPhong, MaSV, LoaiSuCo, MoTa, MucDo, TrangThai, NgayBao, NgayXuLy, NhanVienXuLy)
-- VALUES ('P101', 'SV001', 'Điện', 'Thử thiếu kết quả', 'Thấp', 'Đã hoàn thành', '2026-11-04 09:00:00', '2026-11-04 10:00:00', 'Trần Văn Bảo');
-- (e) phòng không tồn tại -> lỗi khóa ngoại fk_baohong_phong
-- INSERT INTO BaoHong(SoPhong, MaSV, LoaiSuCo, MoTa, MucDo, TrangThai, NgayBao)
-- VALUES ('P999', NULL, 'Điện', 'Thử phòng không có', 'Thấp', 'Mới báo', '2026-11-04 09:00:00');
-- (f) xóa báo hỏng đã hoàn thành -> trigger chặn
-- DELETE FROM BaoHong WHERE TrangThai = 'Đã hoàn thành' LIMIT 1;
-- (g) mở lại báo hỏng đã hoàn thành -> trigger chặn
-- UPDATE BaoHong SET TrangThai = 'Mới báo', NhanVienXuLy = NULL, NgayXuLy = NULL, KetQuaXuLy = NULL
-- WHERE TrangThai = 'Đã hoàn thành' LIMIT 1;
