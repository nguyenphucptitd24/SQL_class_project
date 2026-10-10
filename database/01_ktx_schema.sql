-- PROJECT 2: QUAN LY KY TUC XA - MySQL 8+
-- Luu y: neu tai khoan khong co quyen CREATE DATABASE, bo qua CREATE DATABASE/USE
-- va chon schema do giang vien cap truoc khi chay cac lenh CREATE TABLE.
CREATE DATABASE IF NOT EXISTS KTX CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE KTX;

CREATE TABLE IF NOT EXISTS SinhVien (
  MaSV VARCHAR(20) PRIMARY KEY,
  SoCMT VARCHAR(20) NOT NULL UNIQUE,
  NgaySinh DATE NOT NULL,
  Lop VARCHAR(50) NOT NULL,
  QueQuan VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Phong (
  SoPhong VARCHAR(20) PRIMARY KEY,
  LoaiPhong VARCHAR(50) NOT NULL,
  DonGia DECIMAL(12,2) NOT NULL,
  SoNguoiToiDa INT NOT NULL,
  CONSTRAINT chk_phong_dongia CHECK (DonGia >= 0),
  CONSTRAINT chk_phong_succhua CHECK (SoNguoiToiDa > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ThuePhong (
  MaThue BIGINT PRIMARY KEY AUTO_INCREMENT,
  MaSV VARCHAR(20) NOT NULL,
  SoPhong VARCHAR(20) NOT NULL,
  NgayBatDau DATE NOT NULL,
  NgayKetThuc DATE NULL COMMENT 'NULL nghia la dang thue',
  CONSTRAINT fk_thue_sv FOREIGN KEY (MaSV) REFERENCES SinhVien(MaSV),
  CONSTRAINT fk_thue_phong FOREIGN KEY (SoPhong) REFERENCES Phong(SoPhong),
  CONSTRAINT chk_thue_ngay CHECK (NgayKetThuc IS NULL OR NgayKetThuc >= NgayBatDau),
  INDEX idx_thue_sv_hieuluc (MaSV, NgayKetThuc),
  INDEX idx_thue_phong_hieuluc (SoPhong, NgayKetThuc)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS DichVu (
  MaDV VARCHAR(20) PRIMARY KEY,
  TenDV VARCHAR(100) NOT NULL,
  DonGia DECIMAL(12,2) NOT NULL,
  MoTa VARCHAR(255),
  CONSTRAINT chk_dv_dongia CHECK (DonGia >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS SuDungDichVu (
  MaSuDung BIGINT PRIMARY KEY AUTO_INCREMENT,
  MaSV VARCHAR(20) NOT NULL,
  MaDV VARCHAR(20) NOT NULL,
  ThoiGianSuDung DATETIME NOT NULL,
  SoLuong INT NOT NULL DEFAULT 1,
  DonGiaApDung DECIMAL(12,2) NOT NULL COMMENT 'Chot gia tai thoi diem su dung',
  CONSTRAINT fk_sddv_sv FOREIGN KEY (MaSV) REFERENCES SinhVien(MaSV),
  CONSTRAINT fk_sddv_dv FOREIGN KEY (MaDV) REFERENCES DichVu(MaDV),
  CONSTRAINT chk_sddv_soluong CHECK (SoLuong > 0),
  CONSTRAINT chk_sddv_dongia CHECK (DonGiaApDung >= 0),
  INDEX idx_sddv_thoigian (ThoiGianSuDung)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS XeThang (
  MaXe VARCHAR(20) PRIMARY KEY,
  MaSV VARCHAR(20) NOT NULL,
  BienSo VARCHAR(20) NOT NULL UNIQUE,
  LoaiXe VARCHAR(50) NOT NULL,
  NgayDangKy DATE NOT NULL,
  NgayHetHan DATE NULL,
  DonGiaThang DECIMAL(12,2) NOT NULL DEFAULT 100000,
  CONSTRAINT fk_xethang_sv FOREIGN KEY (MaSV) REFERENCES SinhVien(MaSV),
  CONSTRAINT chk_xethang_ngay CHECK (NgayHetHan IS NULL OR NgayHetHan >= NgayDangKy),
  CONSTRAINT chk_xethang_gia CHECK (DonGiaThang >= 0),
  INDEX idx_xethang_sv_hieuluc (MaSV, NgayHetHan)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS LuotXe (
  MaLuot BIGINT PRIMARY KEY AUTO_INCREMENT,
  MaXe VARCHAR(20) NOT NULL,
  ThoiGianLay DATETIME NULL,
  ThoiGianGui DATETIME NULL,
  SoLuotMienPhiSuDung INT NOT NULL DEFAULT 0 COMMENT 'So luot mien phi da dung trong ngay',
  SoTienPhatSinh DECIMAL(12,2) NOT NULL DEFAULT 0,
  CONSTRAINT fk_luotxe_xe FOREIGN KEY (MaXe) REFERENCES XeThang(MaXe),
  CONSTRAINT chk_luotxe_luot CHECK (SoLuotMienPhiSuDung BETWEEN 0 AND 2),
  CONSTRAINT chk_luotxe_tien CHECK (SoTienPhatSinh >= 0),
  INDEX idx_luotxe_thoigianlay (ThoiGianLay),
  INDEX idx_luotxe_thoigiangui (ThoiGianGui)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Khach (
  SoCMT VARCHAR(20) PRIMARY KEY,
  HoTen VARCHAR(100) NOT NULL,
  NgaySinh DATE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS LuotTham (
  MaLuotTham BIGINT PRIMARY KEY AUTO_INCREMENT,
  SoCMTKhach VARCHAR(20) NOT NULL,
  MaSV VARCHAR(20) NOT NULL,
  ThoiGianDen DATETIME NOT NULL,
  CONSTRAINT fk_luottham_khach FOREIGN KEY (SoCMTKhach) REFERENCES Khach(SoCMT),
  CONSTRAINT fk_luottham_sv FOREIGN KEY (MaSV) REFERENCES SinhVien(MaSV),
  INDEX idx_luottham_thoigianden (ThoiGianDen),
  INDEX idx_luottham_sv (MaSV)
) ENGINE=InnoDB;

-- Du lieu mau: CMT TEST la du lieu gia lap, khong phai giay to that.
INSERT IGNORE INTO SinhVien(MaSV,SoCMT,NgaySinh,Lop,QueQuan) VALUES
('SV001','TEST000001','2006-08-22','D24CN01','Nghe An'),
('SV002','TEST000002','2006-10-10','D24CN02','Ha Noi'),
('SV003','TEST000003','2006-05-15','D24CN01','Thanh Hoa');
INSERT IGNORE INTO Phong(SoPhong,LoaiPhong,DonGia,SoNguoiToiDa) VALUES
('P101','Phong 4 nguoi',1500000,4),('P102','Phong 6 nguoi',1200000,6),('P103','Phong 8 nguoi',1000000,8);
INSERT IGNORE INTO DichVu(MaDV,TenDV,DonGia,MoTa) VALUES
('DV001','Giat la',30000,'Don gia mau moi lan'),('DV002','Trong xe',50000,'Don gia mau tham khao');
INSERT IGNORE INTO Khach(SoCMT,HoTen,NgaySinh) VALUES
('GUEST0001','Nguyen Van Khach','2000-01-01');
INSERT INTO ThuePhong(MaSV,SoPhong,NgayBatDau,NgayKetThuc)
SELECT 'SV001','P101','2026-09-01',NULL WHERE NOT EXISTS(SELECT 1 FROM ThuePhong WHERE MaSV='SV001' AND NgayKetThuc IS NULL);
INSERT INTO ThuePhong(MaSV,SoPhong,NgayBatDau,NgayKetThuc)
SELECT 'SV002','P101','2026-09-01',NULL WHERE NOT EXISTS(SELECT 1 FROM ThuePhong WHERE MaSV='SV002' AND NgayKetThuc IS NULL);
INSERT INTO ThuePhong(MaSV,SoPhong,NgayBatDau,NgayKetThuc)
SELECT 'SV003','P102','2026-09-05',NULL WHERE NOT EXISTS(SELECT 1 FROM ThuePhong WHERE MaSV='SV003' AND NgayKetThuc IS NULL);

CREATE OR REPLACE VIEW V_SinhVienDangO AS
SELECT sv.MaSV,sv.SoCMT,sv.NgaySinh,sv.Lop,sv.QueQuan,p.SoPhong,p.LoaiPhong,p.DonGia,tp.NgayBatDau
FROM SinhVien sv JOIN ThuePhong tp ON tp.MaSV=sv.MaSV AND tp.NgayKetThuc IS NULL
JOIN Phong p ON p.SoPhong=tp.SoPhong;

-- Truy van tham khao: danh sach phong va suc chua con lai.
SELECT p.SoPhong,p.LoaiPhong,p.DonGia,p.SoNguoiToiDa,
       COUNT(tp.MaThue) AS SoNguoiDangO,
       p.SoNguoiToiDa-COUNT(tp.MaThue) AS SoChoConTrong
FROM Phong p LEFT JOIN ThuePhong tp ON tp.SoPhong=p.SoPhong AND tp.NgayKetThuc IS NULL
GROUP BY p.SoPhong,p.LoaiPhong,p.DonGia,p.SoNguoiToiDa ORDER BY p.SoPhong;

-- Tinh tien phong theo thang tron cho thang can lap hoa don.
-- Thang 9/2026: chi can thoi gian thue giao voi thang nay la tinh tron 1 thang.
SET @thang_dau = '2026-09-01';
SET @thang_sau = '2026-10-01';
SELECT sv.MaSV,sv.Lop,p.SoPhong,p.DonGia AS TienPhongThang
FROM ThuePhong tp
JOIN SinhVien sv ON sv.MaSV=tp.MaSV
JOIN Phong p ON p.SoPhong=tp.SoPhong
WHERE tp.NgayBatDau < @thang_sau
  AND COALESCE(tp.NgayKetThuc, '9999-12-31') >= @thang_dau
ORDER BY sv.MaSV;

-- Ghi chu nghiep vu:
-- 1) App can kiem tra khong xep vuot suc chua, va moi SV chi co mot hop dong dang o.
-- 2) App can khoa dong Phong trong transaction khi xep phong de giam nguy co hai thao tac dong thoi.
-- 3) App module xe thang can kiem tra toi da 2 xe dang dang ky tren moi MaSV.
-- 4) LuotXe can duoc ghi theo tung lan lay/gui; phan tinh 2 luot mien phi/ngay va 3.000d/luot phat sinh do module xe xu ly.
