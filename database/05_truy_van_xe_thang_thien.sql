-- CAC TRUY VAN PHUC VU MODULE QUAN LY XE VE THANG
-- Chay sau database/01_ktx_schema.sql (MySQL 8+).
USE KTX;

-- 1. Danh sach xe, chu xe va trang thai ve thang
SELECT x.MaXe,x.BienSo,x.LoaiXe,x.MaSV,sv.Lop,x.NgayDangKy,x.NgayHetHan,x.DonGiaThang,
       CASE
         WHEN x.NgayDangKy>CURRENT_DATE() THEN 'Chua den ngay dang ky'
         WHEN x.NgayHetHan IS NOT NULL AND x.NgayHetHan<CURRENT_DATE() THEN 'Het han'
         ELSE 'Dang hoat dong'
       END AS TrangThai
FROM XeThang x
JOIN SinhVien sv ON sv.MaSV=x.MaSV
ORDER BY x.MaSV,x.MaXe;

-- 2. Lich su tung thao tac lay/gui trong mot thang
SET @thang_dau := '2026-10-01';
SET @thang_sau := DATE_ADD(@thang_dau, INTERVAL 1 MONTH);
SELECT sk.MaSuKien,sk.MaLuot,sk.MaXe,x.BienSo,x.MaSV,sv.Lop,sk.LoaiSuKien,
       sk.ThoiGian,sk.MienPhi,sk.SoTienPhatSinh
FROM SuKienXe sk
JOIN XeThang x ON x.MaXe=sk.MaXe
JOIN SinhVien sv ON sv.MaSV=x.MaSV
WHERE sk.ThoiGian>=@thang_dau AND sk.ThoiGian<@thang_sau
ORDER BY sk.ThoiGian DESC,sk.MaSuKien DESC;

-- 3. So luot mien phi, tinh phi va tong phi theo sinh vien trong thang
SELECT sv.MaSV,sv.Lop,
       COALESCE(v.SoXeThang,0) AS SoXeThang,
       COALESCE(v.TienVeThang,0) AS TienVeThang,
       COALESCE(p.TongSoLuot,0) AS TongSoLuot,
       COALESCE(p.SoLuotMienPhi,0) AS SoLuotMienPhi,
       COALESCE(p.SoLuotTinhPhi,0) AS SoLuotTinhPhi,
       COALESCE(p.PhiPhatSinh,0) AS PhiPhatSinh,
       COALESCE(v.TienVeThang,0)+COALESCE(p.PhiPhatSinh,0) AS TongPhaiTra
FROM SinhVien sv
LEFT JOIN (
    SELECT MaSV,COUNT(*) AS SoXeThang,SUM(DonGiaThang) AS TienVeThang
    FROM XeThang
    WHERE NgayDangKy<@thang_sau AND (NgayHetHan IS NULL OR NgayHetHan>=@thang_dau)
    GROUP BY MaSV
) v ON v.MaSV=sv.MaSV
LEFT JOIN (
    SELECT x.MaSV,COUNT(sk.MaSuKien) AS TongSoLuot,
           SUM(CASE WHEN sk.MienPhi=TRUE THEN 1 ELSE 0 END) AS SoLuotMienPhi,
           SUM(CASE WHEN sk.MienPhi=FALSE THEN 1 ELSE 0 END) AS SoLuotTinhPhi,
           SUM(sk.SoTienPhatSinh) AS PhiPhatSinh
    FROM XeThang x JOIN SuKienXe sk ON sk.MaXe=x.MaXe
    WHERE sk.ThoiGian>=@thang_dau AND sk.ThoiGian<@thang_sau
    GROUP BY x.MaSV
) p ON p.MaSV=sv.MaSV
ORDER BY sv.MaSV;

-- 4. Kiem tra xe dang o ngoai KTX (chua co thao tac gui sau lan lay gan nhat)
SELECT x.MaXe,x.BienSo,x.MaSV,sv.Lop,lx.ThoiGianLay
FROM LuotXe lx
JOIN XeThang x ON x.MaXe=lx.MaXe
JOIN SinhVien sv ON sv.MaSV=x.MaSV
WHERE lx.ThoiGianGui IS NULL
ORDER BY lx.ThoiGianLay;

-- Mien phi duoc tinh rieng tren tung thao tac LAY hoac GUI:
-- hai thao tac dau tien cua moi xe moi ngay mien phi; cac thao tac tiep theo 3.000 VND.
-- Ve thang duoc tinh tron 1 thang neu ngay dang ky va het han giao voi thang bao cao.
