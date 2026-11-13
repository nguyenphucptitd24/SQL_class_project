
package ktx.service;

import ktx.database.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class ThuePhongService {

    public long themHopDong(String maSV, String soPhong, LocalDate ngayBatDau,
                            LocalDate ngayKy, LocalDate ngayHetHan, BigDecimal tienCoc) throws SQLException {
        if (blank(maSV) || blank(soPhong) || ngayBatDau == null || ngayKy == null
                || tienCoc == null || tienCoc.signum() < 0
                || ngayBatDau.isAfter(LocalDate.now()) || ngayKy.isAfter(ngayBatDau)
                || (ngayHetHan != null && ngayHetHan.isBefore(ngayBatDau))) {
            throw new IllegalArgumentException("Thong tin hop dong khong hop le.");
        }

        String maSinhVien = maSV.trim();
        String soPhongTrim = soPhong.trim();
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT MaSV FROM SinhVien WHERE MaSV = ? FOR UPDATE")) {
                    p.setString(1, maSinhVien);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new SQLException("Sinh vien khong ton tai.");
                    }
                }

                int sucChua;
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT SoNguoiToiDa FROM Phong WHERE SoPhong = ? FOR UPDATE")) {
                    p.setString(1, soPhongTrim);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new SQLException("Phong khong ton tai.");
                        sucChua = r.getInt(1);
                    }
                }

                try (PreparedStatement p = c.prepareStatement(
                        "SELECT COUNT(*) FROM ThuePhong WHERE MaSV = ? AND NgayKetThuc IS NULL")) {
                    p.setString(1, maSinhVien);
                    try (ResultSet r = p.executeQuery()) {
                        r.next();
                        if (r.getInt(1) > 0) throw new SQLException("Sinh vien dang co hop dong.");
                    }
                }

                try (PreparedStatement p = c.prepareStatement(
                        "SELECT COUNT(*) FROM ThuePhong WHERE SoPhong = ? AND NgayKetThuc IS NULL")) {
                    p.setString(1, soPhongTrim);
                    try (ResultSet r = p.executeQuery()) {
                        r.next();
                        if (r.getInt(1) >= sucChua) throw new SQLException("Phong da du suc chua.");
                    }
                }

                long maThue;
                String sql = "INSERT INTO ThuePhong(MaSV, SoPhong, NgayBatDau, NgayKyHopDong, "
                           + "NgayHetHanHopDong, TienCoc) VALUES(?, ?, ?, ?, ?, ?)";
                try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1, maSinhVien);
                    p.setString(2, soPhongTrim);
                    p.setDate(3, Date.valueOf(ngayBatDau));
                    p.setDate(4, Date.valueOf(ngayKy));
                    if (ngayHetHan == null) p.setNull(5, Types.DATE);
                    else p.setDate(5, Date.valueOf(ngayHetHan));
                    p.setBigDecimal(6, tienCoc);
                    p.executeUpdate();
                    try (ResultSet keys = p.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Khong lay duoc ma hop dong.");
                        maThue = keys.getLong(1);
                    }
                }

                c.commit();
                return maThue;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void capNhatHopDong(long maThue, LocalDate ngayKy,
                               LocalDate ngayHetHan, BigDecimal tienCoc) throws SQLException {
        if (maThue <= 0 || ngayKy == null || tienCoc == null || tienCoc.signum() < 0) {
            throw new IllegalArgumentException("Thong tin hop dong khong hop le.");
        }
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT NgayBatDau FROM ThuePhong WHERE MaThue = ?")) {
            p.setLong(1, maThue);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Hop dong khong ton tai.");
                LocalDate ngayBatDau = r.getDate(1).toLocalDate();
                if (ngayKy.isAfter(ngayBatDau)
                        || (ngayHetHan != null && ngayHetHan.isBefore(ngayBatDau))) {
                    throw new IllegalArgumentException("Ngay ky va ngay het han khong hop le.");
                }
            }
        }
        String sql = "UPDATE ThuePhong SET NgayKyHopDong = ?, NgayHetHanHopDong = ?, TienCoc = ? "
                   + "WHERE MaThue = ? AND NgayKetThuc IS NULL";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, Date.valueOf(ngayKy));
            if (ngayHetHan == null) p.setNull(2, Types.DATE);
            else p.setDate(2, Date.valueOf(ngayHetHan));
            p.setBigDecimal(3, tienCoc);
            p.setLong(4, maThue);
            if (p.executeUpdate() == 0) throw new SQLException("Hop dong khong ton tai hoac da ket thuc.");
        }
    }

    public void ketThucHopDong(long maThue, LocalDate ngayKetThuc) throws SQLException {
        if (maThue <= 0 || ngayKetThuc == null || ngayKetThuc.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Thong tin ket thuc hop dong khong hop le.");
        }
        String sql = "UPDATE ThuePhong SET NgayKetThuc = ? WHERE MaThue = ? "
                   + "AND NgayKetThuc IS NULL AND NgayBatDau <= ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, Date.valueOf(ngayKetThuc));
            p.setLong(2, maThue);
            p.setDate(3, Date.valueOf(ngayKetThuc));
            if (p.executeUpdate() == 0) throw new SQLException("Hop dong khong ton tai hoac ngay ket thuc khong hop le.");
        }
    }

    // Xep phong cho sinh vien
    public void xepPhong(String maSV, String soPhong, LocalDate ngay) throws SQLException {

        if (blank(maSV) || blank(soPhong) || ngay == null)
            throw new IllegalArgumentException("Nhap du MaSV, SoPhong va ngay bat dau.");

        if (ngay.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Ngay bat dau khong duoc o tuong lai.");

        try (Connection c = DatabaseConnection.getConnection()) {

            c.setAutoCommit(false);

            try {
                // Khoa sinh vien de tranh xep 2 phong dong thoi
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT MaSV FROM SinhVien WHERE MaSV=? FOR UPDATE")) {

                    p.setString(1, maSV.trim());

                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next())
                            throw new SQLException("Sinh vien khong ton tai.");
                    }
                }

                // Khoa phong de tranh vuot suc chua
                int max;

                try (PreparedStatement p = c.prepareStatement(
                        "SELECT SoNguoiToiDa FROM Phong WHERE SoPhong=? FOR UPDATE")) {

                    p.setString(1, soPhong.trim());

                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next())
                            throw new SQLException("Phong khong ton tai.");

                        max = r.getInt(1);
                    }
                }

                // Kiem tra sinh vien da thue phong chua
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT COUNT(*) FROM ThuePhong WHERE MaSV=? AND NgayKetThuc IS NULL")) {

                    p.setString(1, maSV.trim());

                    try (ResultSet r = p.executeQuery()) {
                        r.next();

                        if (r.getInt(1) > 0)
                            throw new SQLException("Sinh vien dang o mot phong.");
                    }
                }

                // Kiem tra suc chua phong
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT COUNT(*) FROM ThuePhong WHERE SoPhong=? AND NgayKetThuc IS NULL")) {

                    p.setString(1, soPhong.trim());

                    try (ResultSet r = p.executeQuery()) {
                        r.next();

                        if (r.getInt(1) >= max)
                            throw new SQLException("Phong da du suc chua.");
                    }
                }

                // Them thong tin thue phong
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO ThuePhong(MaSV, SoPhong, NgayBatDau, NgayKyHopDong, TienCoc) "
                                + "VALUES(?,?,?,?,0)")) {

                    p.setString(1, maSV.trim());
                    p.setString(2, soPhong.trim());
                    p.setDate(3, Date.valueOf(ngay));
                    p.setDate(4, Date.valueOf(ngay));

                    p.executeUpdate();
                }

                c.commit();

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;

            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    // Tra phong
    public void traPhong(String maSV, LocalDate ngayTra) throws SQLException {

        if (blank(maSV) || ngayTra == null)
            throw new IllegalArgumentException("Nhap MaSV va ngay tra.");

        if (ngayTra.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Ngay tra khong duoc o tuong lai.");

        String sql = "UPDATE ThuePhong SET NgayKetThuc=? "
                   + "WHERE MaSV=? AND NgayKetThuc IS NULL "
                   + "AND NgayBatDau<=?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setDate(1, Date.valueOf(ngayTra));
            p.setString(2, maSV.trim());
            p.setDate(3, Date.valueOf(ngayTra));

            if (p.executeUpdate() == 0)
                throw new SQLException("Khong co hop dong hop le hoac ngay tra truoc ngay bat dau.");
        }
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
