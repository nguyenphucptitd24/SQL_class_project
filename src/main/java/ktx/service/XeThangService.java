package ktx.service;

import ktx.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class XeThangService {
    public static final BigDecimal DON_GIA_THANG = new BigDecimal("100000");
    public static final BigDecimal PHI_VUOT_LUOT = new BigDecimal("3000");
    private static final ZoneId KTX_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public void dangKyXe(String maXe, String maSV, String bienSo, String loaiXe,
                         LocalDate ngayHetHan) throws SQLException {
        requireText(maXe, "Ma xe");
        requireText(maSV, "Ma sinh vien");
        requireText(bienSo, "Bien so");
        requireText(loaiXe, "Loai xe");
        LocalDate ngayDangKy = LocalDate.now(KTX_ZONE);
        if (ngayHetHan != null && ngayHetHan.isBefore(ngayDangKy)) {
            throw new IllegalArgumentException("Ngay het han khong duoc truoc ngay dang ky.");
        }

        transaction(c -> {
            try (PreparedStatement p = c.prepareStatement("SELECT MaSV FROM SinhVien WHERE MaSV=? FOR UPDATE")) {
                p.setString(1, maSV.trim());
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new SQLException("Sinh vien khong ton tai.");
                }
            }
            if (countActiveVehicles(c, maSV.trim(), null) >= 2) {
                throw new SQLException("Moi sinh vien chi duoc dang ky toi da 2 xe thang dang hoat dong.");
            }
            try (PreparedStatement p = c.prepareStatement("""
                    INSERT INTO XeThang(MaXe,MaSV,BienSo,LoaiXe,NgayDangKy,NgayHetHan,DonGiaThang)
                    VALUES(?,?,?,?,?,?,?)
                    """)) {
                p.setString(1, maXe.trim());
                p.setString(2, maSV.trim());
                p.setString(3, bienSo.trim());
                p.setString(4, loaiXe.trim());
                p.setDate(5, Date.valueOf(ngayDangKy));
                if (ngayHetHan == null) p.setNull(6, java.sql.Types.DATE);
                else p.setDate(6, Date.valueOf(ngayHetHan));
                p.setBigDecimal(7, DON_GIA_THANG);
                p.executeUpdate();
            }
            return null;
        });
    }

    public void suaXe(String maXe, String bienSo, String loaiXe, LocalDate ngayHetHan) throws SQLException {
        requireText(maXe, "Ma xe");
        requireText(bienSo, "Bien so");
        requireText(loaiXe, "Loai xe");
        transaction(c -> {
            String maSV = findVehicleOwner(c, maXe);
            try (PreparedStatement p = c.prepareStatement("SELECT MaSV FROM SinhVien WHERE MaSV=? FOR UPDATE")) {
                p.setString(1, maSV);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new SQLException("Sinh vien khong ton tai.");
                }
            }
            Registration registration = lockVehicle(c, maXe);
            if (!registration.maSV().equals(maSV)) {
                throw new SQLException("Chu xe da thay doi; hay tai lai danh sach.");
            }
            if (ngayHetHan != null && ngayHetHan.isBefore(registration.ngayDangKy())) {
                throw new IllegalArgumentException("Ngay het han khong duoc truoc ngay dang ky.");
            }
            LocalDate today = LocalDate.now(KTX_ZONE);
            boolean remainsActive = !registration.ngayDangKy().isAfter(today)
                && (ngayHetHan == null || !ngayHetHan.isBefore(today));
            if (remainsActive && countActiveVehicles(c, maSV, maXe.trim()) >= 2) {
                throw new SQLException("Sinh vien chi duoc co toi da 2 xe ve thang dang hoat dong.");
            }
            try (PreparedStatement p = c.prepareStatement(
                    "UPDATE XeThang SET BienSo=?,LoaiXe=?,NgayHetHan=? WHERE MaXe=?")) {
                p.setString(1, bienSo.trim());
                p.setString(2, loaiXe.trim());
                if (ngayHetHan == null) p.setNull(3, java.sql.Types.DATE);
                else p.setDate(3, Date.valueOf(ngayHetHan));
                p.setString(4, maXe.trim());
                if (p.executeUpdate() == 0) throw new SQLException("Khong tim thay xe.");
            }
            return null;
        });
    }

    public void ketThucDangKy(String maXe) throws SQLException {
        requireText(maXe, "Ma xe");
        transaction(c -> {
            Registration registration = lockVehicle(c, maXe);
            LocalDate homNay = LocalDate.now(KTX_ZONE);
            if (registration.ngayDangKy().isAfter(homNay)) {
                throw new SQLException("Khong the ket thuc dang ky truoc ngay bat dau.");
            }
            if (hasOpenParking(c, maXe)) {
                throw new SQLException("Xe dang o ngoai KTX; hay ghi nhan gui xe truoc khi ket thuc dang ky.");
            }
            try (PreparedStatement p = c.prepareStatement("""
                    UPDATE XeThang SET NgayHetHan=?
                    WHERE MaXe=? AND (NgayHetHan IS NULL OR NgayHetHan>=?)
                    """)) {
                p.setDate(1, Date.valueOf(homNay));
                p.setString(2, maXe.trim());
                p.setDate(3, Date.valueOf(homNay));
                if (p.executeUpdate() == 0) throw new SQLException("Dang ky xe da het han.");
            }
            return null;
        });
    }

    public BigDecimal layXe(String maXe) throws SQLException {
        requireText(maXe, "Ma xe");
        LocalDateTime now = LocalDateTime.now(KTX_ZONE);
        return transaction(c -> {
            Registration registration = lockVehicle(c, maXe);
            LocalDate today = now.toLocalDate();
            if (registration.ngayDangKy().isAfter(today)
                || (registration.ngayHetHan() != null && registration.ngayHetHan().isBefore(today))) {
                throw new SQLException("Dang ky ve thang cua xe khong co hieu luc hom nay.");
            }
            if (hasOpenParking(c, maXe)) {
                throw new SQLException("Xe dang o ngoai KTX; khong the ghi nhan lay xe lan nua.");
            }
            boolean free = countEventsToday(c, maXe, today) < 2;
            BigDecimal fee = free ? BigDecimal.ZERO : PHI_VUOT_LUOT;
            long maLuot;
            try (PreparedStatement p = c.prepareStatement("""
                    INSERT INTO LuotXe(MaXe,ThoiGianLay,SoLuotMienPhiSuDung,SoTienPhatSinh)
                    VALUES(?,?,?,?)
                    """, PreparedStatement.RETURN_GENERATED_KEYS)) {
                p.setString(1, maXe.trim());
                p.setTimestamp(2, Timestamp.valueOf(now));
                p.setInt(3, free ? 1 : 0);
                p.setBigDecimal(4, fee);
                p.executeUpdate();
                try (ResultSet keys = p.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Khong lay duoc ma luot xe moi.");
                    maLuot = keys.getLong(1);
                }
            }
            insertEvent(c, maLuot, maXe, "LAY", now, free, fee);
            return fee;
        });
    }

    public BigDecimal guiXe(String maXe) throws SQLException {
        requireText(maXe, "Ma xe");
        LocalDateTime now = LocalDateTime.now(KTX_ZONE);
        return transaction(c -> {
            lockVehicle(c, maXe);
            long maLuot;
            Timestamp thoiGianLay;
            try (PreparedStatement p = c.prepareStatement("""
                    SELECT MaLuot,ThoiGianLay FROM LuotXe
                    WHERE MaXe=? AND ThoiGianGui IS NULL
                    ORDER BY ThoiGianLay DESC LIMIT 1 FOR UPDATE
                    """)) {
                p.setString(1, maXe.trim());
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new SQLException("Khong co luot lay xe dang mo de ghi nhan gui xe.");
                    maLuot = r.getLong("MaLuot");
                    thoiGianLay = r.getTimestamp("ThoiGianLay");
                }
            }
            if (thoiGianLay == null || now.isBefore(thoiGianLay.toLocalDateTime())) {
                throw new SQLException("Thoi gian gui xe khong hop le.");
            }
            boolean free = countEventsToday(c, maXe, now.toLocalDate()) < 2;
            BigDecimal fee = free ? BigDecimal.ZERO : PHI_VUOT_LUOT;
            try (PreparedStatement p = c.prepareStatement("""
                    UPDATE LuotXe
                    SET ThoiGianGui=?,SoLuotMienPhiSuDung=SoLuotMienPhiSuDung+?,
                        SoTienPhatSinh=SoTienPhatSinh+?
                    WHERE MaLuot=? AND ThoiGianGui IS NULL
                    """)) {
                p.setTimestamp(1, Timestamp.valueOf(now));
                p.setInt(2, free ? 1 : 0);
                p.setBigDecimal(3, fee);
                p.setLong(4, maLuot);
                if (p.executeUpdate() == 0) throw new SQLException("Luot gui xe da duoc ghi nhan.");
            }
            insertEvent(c, maLuot, maXe, "GUI", now, free, fee);
            return fee;
        });
    }

    private void insertEvent(Connection c, long maLuot, String maXe, String loai,
                             LocalDateTime thoiGian, boolean mienPhi, BigDecimal fee) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("""
                INSERT INTO SuKienXe(MaLuot,MaXe,LoaiSuKien,ThoiGian,MienPhi,SoTienPhatSinh)
                VALUES(?,?,?,?,?,?)
                """)) {
            p.setLong(1, maLuot);
            p.setString(2, maXe.trim());
            p.setString(3, loai);
            p.setTimestamp(4, Timestamp.valueOf(thoiGian));
            p.setBoolean(5, mienPhi);
            p.setBigDecimal(6, fee);
            p.executeUpdate();
        }
    }

    private int countEventsToday(Connection c, String maXe, LocalDate date) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("""
                SELECT COUNT(*) FROM SuKienXe
                WHERE MaXe=? AND ThoiGian>=? AND ThoiGian<?
                """)) {
            p.setString(1, maXe.trim());
            p.setTimestamp(2, Timestamp.valueOf(date.atStartOfDay()));
            p.setTimestamp(3, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    private boolean hasOpenParking(Connection c, String maXe) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT 1 FROM LuotXe WHERE MaXe=? AND ThoiGianGui IS NULL LIMIT 1")) {
            p.setString(1, maXe.trim());
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    private String findVehicleOwner(Connection c, String maXe) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT MaSV FROM XeThang WHERE MaXe=?")) {
            p.setString(1, maXe.trim());
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Xe thang khong ton tai.");
                return r.getString("MaSV");
            }
        }
    }

    private int countActiveVehicles(Connection c, String maSV, String excludedMaXe) throws SQLException {
        String sql = """
            SELECT MaXe FROM XeThang
            WHERE MaSV=? AND NgayDangKy<=CURRENT_DATE()
              AND (NgayHetHan IS NULL OR NgayHetHan>=CURRENT_DATE())
            """ + (excludedMaXe == null ? "" : " AND MaXe<>?") + " FOR UPDATE";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSV);
            if (excludedMaXe != null) p.setString(2, excludedMaXe);
            try (ResultSet r = p.executeQuery()) {
                int count = 0;
                while (r.next()) count++;
                return count;
            }
        }
    }

    private Registration lockVehicle(Connection c, String maXe) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT NgayDangKy,NgayHetHan FROM XeThang WHERE MaXe=? FOR UPDATE")) {
            p.setString(1, maXe.trim());
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Xe thang khong ton tai.");
                Date expiry = r.getDate("NgayHetHan");
                return new Registration(
                    r.getString("MaSV"),
                    r.getDate("NgayDangKy").toLocalDate(),
                    expiry == null ? null : expiry.toLocalDate()
                );
            }
        }
    }

    private <T> T transaction(SqlWork<T> work) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                try {
                    c.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " khong duoc de trong.");
    }

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection connection) throws SQLException;
    }

    private record Registration(String maSV, LocalDate ngayDangKy, LocalDate ngayHetHan) {}
}
