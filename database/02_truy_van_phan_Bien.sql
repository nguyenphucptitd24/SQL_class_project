-- CAC TRUY VAN PHUC VU CUA BIEN
USE KTX;

-- 1. Danh sach sinh vien va phong dang o
SELECT sv.MaSV,sv.SoCMT,sv.NgaySinh,sv.Lop,sv.QueQuan,p.SoPhong,p.LoaiPhong,p.DonGia,tp.NgayBatDau
FROM SinhVien sv
LEFT JOIN ThuePhong tp ON tp.MaSV=sv.MaSV AND tp.NgayKetThuc IS NULL
LEFT JOIN Phong p ON p.SoPhong=tp.SoPhong
ORDER BY sv.MaSV;

-- 2. Danh sach phong, so nguoi dang o va so cho trong
SELECT p.SoPhong,p.LoaiPhong,p.DonGia,p.SoNguoiToiDa,
       COUNT(tp.MaThue) AS SoNguoiDangO,
       p.SoNguoiToiDa-COUNT(tp.MaThue) AS SoChoConTrong
FROM Phong p LEFT JOIN ThuePhong tp ON tp.SoPhong=p.SoPhong AND tp.NgayKetThuc IS NULL
GROUP BY p.SoPhong,p.LoaiPhong,p.DonGia,p.SoNguoiToiDa
ORDER BY p.SoPhong;

-- 3. Tim sinh vien theo ma, lop hoac que quan (doi tu khoa ? thanh gia tri can tim)
SELECT MaSV,SoCMT,NgaySinh,Lop,QueQuan
FROM SinhVien
WHERE MaSV LIKE '%SV001%' OR Lop LIKE '%SV001%' OR QueQuan LIKE '%SV001%';

-- 4. Lich su thue phong cua mot sinh vien
SELECT tp.MaThue,tp.MaSV,tp.SoPhong,tp.NgayBatDau,tp.NgayKetThuc,p.DonGia
FROM ThuePhong tp JOIN Phong p ON p.SoPhong=tp.SoPhong
WHERE tp.MaSV='SV001' ORDER BY tp.NgayBatDau DESC;

-- 5. Kiem tra phong con cho truoc khi xep (app van phai kiem tra lai trong transaction)
SELECT p.SoPhong,p.SoNguoiToiDa,COUNT(tp.MaThue) AS SoNguoiDangO
FROM Phong p LEFT JOIN ThuePhong tp ON tp.SoPhong=p.SoPhong AND tp.NgayKetThuc IS NULL
WHERE p.SoPhong='P101'
GROUP BY p.SoPhong,p.SoNguoiToiDa
HAVING COUNT(tp.MaThue)<p.SoNguoiToiDa;
