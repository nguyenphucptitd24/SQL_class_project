package ktx.repository;

import ktx.database.DatabaseConnection;
import ktx.model.SuKienXe;
import ktx.model.ThongKePhiXe;
import ktx.model.XeThang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class XeThangRepository {
    public List<XeThang> findAll() throws SQLException {
        String sql = """
            SELECT x.MaXe,x.MaSV,sv.Lop,x.BienSo,x.LoaiXe,x.NgayDangKy,x.NgayHetHan,
                   x.DonGiaThang,
                   (x.NgayDangKy<=CURRENT_DATE() AND
                    (x.NgayHetHan IS NULL OR x.NgayHetHan>=CURRENT_DATE())) AS DangHoatDong
            FROM XeThang x JOIN SinhVien sv ON sv.MaSV=x.MaSV
            ORDER BY x.MaSV,x.MaXe
            """;
        List<XeThang> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                out.add(new XeThang(
                    r.getString("MaXe"),
                    r.getString("MaSV"),
                    r.getString("Lop"),
                    r.getString("BienSo"),
                    r.getString("LoaiXe"),
                    r.getDate("NgayDangKy").toLocalDate(),
                    r.getDate("NgayHetHan") == null ? null : r.getDate("NgayHetHan").toLocalDate(),
                    r.getBigDecimal("DonGiaThang"),
                    r.getBoolean("DangHoatDong")
                ));
            }
        }
        return out;
    }

    public List<SuKienXe> findEvents(YearMonth month) throws SQLException {
        String sql = """
            SELECT sk.MaSuKien,sk.MaLuot,sk.MaXe,x.BienSo,x.MaSV,sv.Lop,sk.LoaiSuKien,
                   sk.ThoiGian,sk.MienPhi,sk.SoTienPhatSinh
            FROM SuKienXe sk
            JOIN XeThang x ON x.MaXe=sk.MaXe
            JOIN SinhVien sv ON sv.MaSV=x.MaSV
            WHERE sk.ThoiGian>=? AND sk.ThoiGian<?
            ORDER BY sk.ThoiGian DESC,sk.MaSuKien DESC
            """;
        List<SuKienXe> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, java.sql.Timestamp.valueOf(month.atDay(1).atStartOfDay()));
            p.setTimestamp(2, java.sql.Timestamp.valueOf(month.plusMonths(1).atDay(1).atStartOfDay()));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    out.add(new SuKienXe(
                        r.getLong("MaSuKien"),
                        r.getLong("MaLuot"),
                        r.getString("MaXe"),
                        r.getString("BienSo"),
                        r.getString("MaSV"),
                        r.getString("Lop"),
                        r.getString("LoaiSuKien"),
                        r.getTimestamp("ThoiGian").toLocalDateTime(),
                        r.getBoolean("MienPhi"),
                        r.getBigDecimal("SoTienPhatSinh")
                    ));
                }
            }
        }
        return out;
    }

    public List<ThongKePhiXe> findMonthlyFees(YearMonth month) throws SQLException {
        String sql = """
            SELECT sv.MaSV,sv.Lop,
                   COALESCE(v.SoXeThang,0) AS SoXeThang,
                   COALESCE(v.TienVeThang,0) AS TienVeThang,
                   COALESCE(p.SoLuot,0) AS SoLuot,
                   COALESCE(p.SoLuotMienPhi,0) AS SoLuotMienPhi,
                   COALESCE(p.SoLuotTinhPhi,0) AS SoLuotTinhPhi,
                   COALESCE(p.PhiPhatSinh,0) AS PhiPhatSinh,
                   COALESCE(v.TienVeThang,0)+COALESCE(p.PhiPhatSinh,0) AS TongPhaiTra
            FROM SinhVien sv
            LEFT JOIN (
                SELECT MaSV,COUNT(*) AS SoXeThang,SUM(DonGiaThang) AS TienVeThang
                FROM XeThang
                WHERE NgayDangKy<? AND (NgayHetHan IS NULL OR NgayHetHan>=?)
                GROUP BY MaSV
            ) v ON v.MaSV=sv.MaSV
            LEFT JOIN (
                SELECT x.MaSV,COUNT(sk.MaSuKien) AS SoLuot,
                       SUM(CASE WHEN sk.MienPhi=TRUE THEN 1 ELSE 0 END) AS SoLuotMienPhi,
                       SUM(CASE WHEN sk.MienPhi=FALSE THEN 1 ELSE 0 END) AS SoLuotTinhPhi,
                       SUM(sk.SoTienPhatSinh) AS PhiPhatSinh
                FROM XeThang x
                JOIN SuKienXe sk ON sk.MaXe=x.MaXe AND sk.ThoiGian>=? AND sk.ThoiGian<?
                GROUP BY x.MaSV
            ) p ON p.MaSV=sv.MaSV
            ORDER BY sv.MaSV
            """;
        List<ThongKePhiXe> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, java.sql.Timestamp.valueOf(month.plusMonths(1).atDay(1).atStartOfDay()));
            p.setDate(2, java.sql.Date.valueOf(month.atDay(1)));
            p.setTimestamp(3, java.sql.Timestamp.valueOf(month.atDay(1).atStartOfDay()));
            p.setTimestamp(4, java.sql.Timestamp.valueOf(month.plusMonths(1).atDay(1).atStartOfDay()));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    out.add(new ThongKePhiXe(
                        r.getString("MaSV"),
                        r.getString("Lop"),
                        r.getLong("SoXeThang"),
                        r.getBigDecimal("TienVeThang"),
                        r.getLong("SoLuot"),
                        r.getLong("SoLuotMienPhi"),
                        r.getLong("SoLuotTinhPhi"),
                        r.getBigDecimal("PhiPhatSinh"),
                        r.getBigDecimal("TongPhaiTra")
                    ));
                }
            }
        }
        return out;
    }

}
