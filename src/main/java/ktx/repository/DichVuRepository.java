package ktx.repository;

import ktx.database.DatabaseConnection;
import ktx.model.DichVu;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DichVuRepository {

    public List<DichVu> findAll() throws SQLException {
        List<DichVu> list = new ArrayList<>();
        String sql = "SELECT MaDV, TenDV, DonGia, MoTa FROM DichVu ORDER BY MaDV";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new DichVu(
                    rs.getString("MaDV"),
                    rs.getString("TenDV"),
                    rs.getBigDecimal("DonGia"),
                    rs.getString("MoTa")
                ));
            }
        }
        return list;
    }

    public boolean insert(DichVu dv) throws SQLException {
        String sql = "INSERT INTO DichVu (MaDV, TenDV, DonGia, MoTa) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dv.getMaDV());
            pstmt.setString(2, dv.getTenDV());
            pstmt.setBigDecimal(3, dv.getDonGia());
            pstmt.setString(4, dv.getMoTa());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean update(DichVu dv) throws SQLException {
        String sql = "UPDATE DichVu SET TenDV = ?, DonGia = ?, MoTa = ? WHERE MaDV = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dv.getTenDV());
            pstmt.setBigDecimal(2, dv.getDonGia());
            pstmt.setString(3, dv.getMoTa());
            pstmt.setString(4, dv.getMaDV());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(String maDV) throws SQLException {
        String sql = "DELETE FROM DichVu WHERE MaDV = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maDV);
            return pstmt.executeUpdate() > 0;
        }
    }

    public BigDecimal getTongTienTheoThang(int year, int month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(SoLuong * DonGiaApDung), 0) " +
                     "FROM SuDungDichVu " +
                     "WHERE YEAR(ThoiGianSuDung) = ? AND MONTH(ThoiGianSuDung) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, year);
            pstmt.setInt(2, month);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTongTienTheoSinhVienVaThang(String maSV, int year, int month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(SoLuong * DonGiaApDung), 0) " +
                     "FROM SuDungDichVu " +
                     "WHERE MaSV = ? AND YEAR(ThoiGianSuDung) = ? AND MONTH(ThoiGianSuDung) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maSV);
            pstmt.setInt(2, year);
            pstmt.setInt(3, month);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public List<String> getDanhSachDichVuTheoThang(int year, int month) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT CONCAT(MaDV, ' - ', FORMAT(SUM(SoLuong * DonGiaApDung), 0), ' VND') " +
                     "FROM SuDungDichVu " +
                     "WHERE YEAR(ThoiGianSuDung) = ? AND MONTH(ThoiGianSuDung) = ? " +
                     "GROUP BY MaDV ORDER BY MaDV";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, year);
            pstmt.setInt(2, month);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString(1));
                }
            }
        }
        return list;
    }

    public BigDecimal getTongTienTheoDichVuVaThang(String maDV, int year, int month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(SoLuong * DonGiaApDung), 0) FROM SuDungDichVu " +
                     "WHERE MaDV = ? AND YEAR(ThoiGianSuDung) = ? AND MONTH(ThoiGianSuDung) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maDV);
            pstmt.setInt(2, year);
            pstmt.setInt(3, month);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public boolean ghiNhanSuDung(String maSV, String maDV, int soLuong, LocalDate ngaySuDung) throws SQLException {
        String sql = "INSERT INTO SuDungDichVu (MaSV, MaDV, ThoiGianSuDung, SoLuong, DonGiaApDung) " +
                     "VALUES (?, ?, ?, ?, (SELECT DonGia FROM DichVu WHERE MaDV = ?))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maSV);
            pstmt.setString(2, maDV);
            pstmt.setString(3, ngaySuDung.atStartOfDay().toString());
            pstmt.setInt(4, soLuong);
            pstmt.setString(5, maDV);

            return pstmt.executeUpdate() > 0;
        }
    }
}
