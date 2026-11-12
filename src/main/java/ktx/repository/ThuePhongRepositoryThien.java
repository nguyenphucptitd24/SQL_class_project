
package ktx.service;

import ktx.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class ThuePhongService {

    // =====================================================
    // 1. XEP PHONG - GIU LAI CHUC NANG CU
    // =====================================================

    public void xepPhong(String maSV, String soPhong, LocalDate ngay)
            throws SQLException {

        if (blank(maSV) || blank(soPhong) || ngay == null) {
            throw new IllegalArgumentException(
                "Nhap du MaSV, SoPhong va ngay bat dau."
            );
        }

        if (ngay.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Ngay bat dau khong duoc o tuong lai."
            );
        }

        transaction(c -> {
            kiemTraSinhVien(c, maSV);
            int sucChua = laySucChuaPhong(c, soPhong);

            if (daThuePhong(c, maSV)) {
                throw new SQLException("Sinh vien dang o mot phong.");
            }

            if (demNguoiTrongPhong(c, soPhong) >= sucChua) {
                throw new SQLException("Phong da du suc chua.");
            }

            String sql = """
                INSERT INTO ThuePhong(MaSV, SoPhong, NgayBatDau)
                VALUES (?, ?, ?)
                """;

            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, maSV.trim());
                p.setString(2, soPhong.trim());
                p.setDate(3, Date.valueOf(ngay));
                p.executeUpdate();
            }

            return null;
        });
    }

    // =====================================================
    // 2. TAO HOP DONG LUU TRU
    // =====================================================

    public long themHopDong(
            String maSV,
            String soPhong,
            LocalDate ngayBatDau,
            LocalDate ngayKyHopDong,
            LocalDate ngayHetHanHopDong,
            BigDecimal tienCoc
    ) throws SQLException {

        kiemTraThongTinHopDong(
            maSV,
            soPhong,
            ngayBatDau,
            ngayKyHopDong,
            ngayHetHanHopDong,
            tienCoc
        );

        return transaction(c -> {

            // Kiem tra sinh vien va phong
            kiemTraSinhVien(c, maSV);

            int sucChua = laySucChuaPhong(c, soPhong);

            // Mot sinh vien khong duoc co hai hop dong dang o
            if (daThuePhong(c, maSV)) {
                throw new SQLException(
                    "Sinh vien da co hop dong thue phong dang hoat dong."
                );
            }

            // Kiem tra suc chua cua phong
            if (demNguoiTrongPhong(c, soPhong) >= sucChua) {
                throw new SQLException("Phong da du suc chua.");
            }

            String sql = """
                INSERT INTO ThuePhong(
                    MaSV,
                    SoPhong,
                    NgayBatDau,
                    NgayKyHopDong,
                    NgayHetHanHopDong,
                    TienCoc
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement p = c.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {

                p.setString(1, maSV.trim());
                p.setString(2, soPhong.trim());
                p.setDate(3, Date.valueOf(ngayBatDau));
                p.setDate(4, Date.valueOf(ngayKyHopDong));
                p.setDate(5, Date.valueOf(ngayHetHanHopDong));
                p.setBigDecimal(6, tienCoc);

                p.executeUpdate();

                try (ResultSet r = p.getGeneratedKeys()) {
                    if (r.next()) {
                        return r.getLong(1);
                    }
                }
            }

            throw new SQLException("Khong tao duoc ma hop dong.");
        });
    }

    // =====================================================
    // 3. CAP NHAT HOP DONG
    // =====================================================

    public void capNhatHopDong(
            long maThue,
            String maSV,
            String soPhong,
            LocalDate ngayBatDau,
            LocalDate ngayKyHopDong,
            LocalDate ngayHetHanHopDong,
            BigDecimal tienCoc
    ) throws SQLException {

        if (maThue <= 0) {
            throw new IllegalArgumentException("Ma hop dong khong hop le.");
        }

        kiemTraThongTinHopDong(
            maSV,
            soPhong,
            ngayBatDau,
            ngayKyHopHopDong(ngayKyHopDong),
            ngayHetHanHopDong,
            tienCoc
        );

        transaction(c -> {

            // Kiem tra hop dong co ton tai khong
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT MaThue FROM ThuePhong WHERE MaThue=? FOR UPDATE")) {

                p.setLong(1, maThue);

                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) {
                        throw new SQLException("Hop dong khong ton tai.");
                    }
                }
            }

            kiemTraSinhVien(c, maSV);

            int sucChua = laySucChuaPhong(c, soPhong);

            // Kiem tra sinh vien co hop dong khac dang hoat dong khong
            String sqlKiemTra = """
                SELECT COUNT(*)
                FROM ThuePhong
                WHERE MaSV = ?
                  AND NgayKetThuc IS NULL
                  AND MaThue <> ?
                """;

            try (PreparedStatement p = c.prepareStatement(sqlKiemTra)) {
                p.setString(1, maSV.trim());
                p.setLong(2, maThue);

                try (ResultSet r = p.executeQuery()) {
                    r.next();

                    if (r.getInt(1) > 0) {
                        throw new SQLException(
                            "Sinh vien da co hop dong khac dang hoat dong."
                        );
                    }
                }
            }

            // Dem so nguoi trong phong, khong tinh hop dong dang sua
            String sqlDem = """
                SELECT COUNT(*)
                FROM ThuePhong
                WHERE SoPhong = ?
                  AND NgayKetThuc IS NULL
                  AND MaThue <> ?
                """;

            try (PreparedStatement p = c.prepareStatement(sqlDem)) {
                p.setString(1, soPhong.trim());
                p.setLong(2, maThue);

                try (ResultSet r = p.executeQuery()) {
                    r.next();

                    if (r.getInt(1) >= sucChua) {
                        throw new SQLException("Phong da du suc chua.");
                    }
                }
            }

            String sql = """
                UPDATE ThuePhong
                SET MaSV = ?,
                    SoPhong = ?,
                    NgayBatDau = ?,
                    NgayKyHopDong = ?,
                    NgayHetHanHopDong = ?,
                    TienCoc = ?
                WHERE MaThue = ?
                  AND NgayKetThuc IS NULL
                """;

            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, maSV.trim());
                p.setString(2, soPhong.trim());
                p.setDate(3, Date.valueOf(ngayBatDau));
                p.setDate(4, Date.valueOf(ngayKyHopDong));
                p.setDate(5, Date.valueOf(ngayHetHanHopDong));
                p.setBigDecimal(6, tienCoc);
                p.setLong(7, maThue);

                if (p.executeUpdate() == 0) {
                    throw new SQLException(
                        "Khong the cap nhat hop dong da ket thuc."
                    );
                }
            }

            return null;
        });
    }

    // =====================================================
    // 4. KET THUC HOP DONG
    // =====================================================

    public void ketThucHopDong(long maThue, LocalDate ngayKetThuc)
            throws SQLException {

        if (maThue <= 0 || ngayKetThuc == null) {
            throw new IllegalArgumentException(
                "Ma hop dong va ngay ket thuc khong duoc de trong."
            );
        }

        if (ngayKetThuc.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Ngay ket thuc khong duoc o tuong lai."
            );
        }

        transaction(c -> {

            String sql = """
                UPDATE ThuePhong
                SET NgayKetThuc = ?
                WHERE MaThue = ?
                  AND NgayKetThuc IS NULL
                  AND NgayBatDau <= ?
                """;

            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setDate(1, Date.valueOf(ngayKetThuc));
                p.setLong(2, maThue);
                p.setDate(3, Date.valueOf(ngayKetThuc));

                if (p.executeUpdate() == 0) {
                    throw new SQLException(
                        "Hop dong khong ton tai, da ket thuc hoac ngay ket thuc khong hop le."
                    );
                }
            }

            return null;
        });
    }

    // =====================================================
    // 5. TRA PHONG - GIU LAI CHUC NANG CU
    // =====================================================

    public void traPhong(String maSV, LocalDate ngayTra)
            throws SQLException {

        if (blank(maSV) || ngayTra == null) {
            throw new IllegalArgumentException(
                "Nhap MaSV va ngay tra."
            );
        }

        if (ngayTra.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Ngay tra khong duoc o tuong lai."
            );
        }

        String sql = """
            UPDATE ThuePhong
            SET NgayKetThuc = ?
            WHERE MaSV = ?
              AND NgayKetThuc IS NULL
              AND NgayBatDau <= ?
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setDate(1, Date.valueOf(ngayTra));
            p.setString(2, maSV.trim());
            p.setDate(3, Date.valueOf(ngayTra));

            if (p.executeUpdate() == 0) {
                throw new SQLException(
                    "Khong co hop dong hop le hoac ngay tra truoc ngay bat dau."
                );
            }
        }
    }

    // =====================================================
    // 6. CAC HAM KIEM TRA DU LIEU
    // =====================================================

    private void kiemTraThongTinHopDong(
            String maSV,
            String soPhong,
            LocalDate ngayBatDau,
            LocalDate ngayKyHopDong,
            LocalDate ngayHetHanHopDong,
            BigDecimal tienCoc
    ) {

        if (blank(maSV) || blank(soPhong)
                || ngayBatDau == null
                || ngayKyHopDong == null
                || ngayHetHanHopDong == null
                || tienCoc == null) {

            throw new IllegalArgumentException(
                "Vui long nhap day du thong tin hop dong."
            );
        }

        if (ngayBatDau.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Ngay bat dau khong duoc o tuong lai."
            );
        }

        if (ngayKyHopDong.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Ngay ky hop dong khong duoc o tuong lai."
            );
        }

        if (ngayHetHanHopDong.isBefore(ngayBatDau)) {
            throw new IllegalArgumentException(
                "Ngay het han phai sau ngay bat dau."
            );
        }

        if (ngayKyHopDong.isAfter(ngayHetHanHopDong)) {
            throw new IllegalArgumentException(
                "Ngay ky khong the sau ngay het han."
            );
        }

        if (tienCoc.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Tien coc khong duoc la so am."
            );
        }
    }

    private void kiemTraSinhVien(Connection c, String maSV)
            throws SQLException {

        try (PreparedStatement p = c.prepareStatement(
                "SELECT MaSV FROM SinhVien WHERE MaSV=? FOR UPDATE")) {

            p.setString(1, maSV.trim());

            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    throw new SQLException("Sinh vien khong ton tai.");
                }
            }
        }
    }

    private int laySucChuaPhong(Connection c, String soPhong)
            throws SQLException {

        try (PreparedStatement p = c.prepareStatement(
                "SELECT SoNguoiToiDa FROM Phong WHERE SoPhong=? FOR UPDATE")) {

            p.setString(1, soPhong.trim());

            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    throw new SQLException("Phong khong ton tai.");
                }

                return r.getInt("SoNguoiToiDa");
            }
        }
    }

    private boolean daThuePhong(Connection c, String maSV)
            throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM ThuePhong
            WHERE MaSV = ? AND NgayKetThuc IS NULL
            """;

        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSV.trim());

            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1) > 0;
            }
        }
    }

    private int demNguoiTrongPhong(Connection c, String soPhong)
            throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM ThuePhong
            WHERE SoPhong = ? AND NgayKetThuc IS NULL
            """;

        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, soPhong.trim());

            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    // =====================================================
    // 7. QUAN LY GIAO DICH DATABASE
    // =====================================================

    private <T> T transaction(SqlWork<T> work) throws SQLException {

        try (Connection c = DatabaseConnection.getConnection()) {

            c.setAutoCommit(false);

            try {
                T result = work.run(c);
                c.commit();
                return result;

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;

            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection c) throws SQLException;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private LocalDate ngayKyHopHopDong(LocalDate ngayKy) {
        return ngayKy;
    }
}
