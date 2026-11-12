
package ktx.repository;

import ktx.database.DatabaseConnection;
import ktx.model.ThuePhong;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ThuePhongRepository {

    // Lay danh sach tat ca hop dong
    public List<ThuePhong> findAll() throws SQLException {
        String sql = """
            SELECT MaThue, MaSV, SoPhong, NgayBatDau, NgayKetThuc,
                   NgayKyHopDong, NgayHetHanHopDong, TienCoc
            FROM ThuePhong
            ORDER BY MaThue
            """;

        List<ThuePhong> list = new ArrayList<>();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {

            while (r.next()) {
                list.add(mapRow(r));
            }
        }

        return list;
    }

    // Tim hop dong theo ma thue
    public ThuePhong findById(Long maThue) throws SQLException {
        String sql = """
            SELECT MaThue, MaSV, SoPhong, NgayBatDau, NgayKetThuc,
                   NgayKyHopDong, NgayHetHanHopDong, TienCoc
            FROM ThuePhong
            WHERE MaThue = ?
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setLong(1, maThue);

            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return mapRow(r);
                }
            }
        }

        return null;
    }

    // Lay danh sach hop dong dang co hieu luc
    public List<ThuePhong> findActiveByStudent(String maSV) throws SQLException {
        String sql = """
            SELECT MaThue, MaSV, SoPhong, NgayBatDau, NgayKetThuc,
                   NgayKyHopDong, NgayHetHanHopDong, TienCoc
            FROM ThuePhong
            WHERE MaSV = ? AND NgayKetThuc IS NULL
            """;

        List<ThuePhong> list = new ArrayList<>();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, maSV);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(mapRow(r));
                }
            }
        }

        return list;
    }

    // Kiem tra sinh vien co hop dong dang thue hay khong
    public boolean existsActiveByStudent(String maSV) throws SQLException {
        String sql = """
            SELECT 1
            FROM ThuePhong
            WHERE MaSV = ? AND NgayKetThuc IS NULL
            LIMIT 1
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, maSV);

            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    // Dem so sinh vien dang o trong phong
    public int countActiveInRoom(String soPhong) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM ThuePhong
            WHERE SoPhong = ? AND NgayKetThuc IS NULL
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, soPhong);

            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return r.getInt(1);
                }
            }
        }

        return 0;
    }

    // Them hop dong moi
    public void insert(ThuePhong thuePhong) throws SQLException {
        String sql = """
            INSERT INTO ThuePhong(
                MaSV, SoPhong, NgayBatDau, NgayKetThuc,
                NgayKyHopDong, NgayHetHanHopDong, TienCoc
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, thuePhong.maSV());
            p.setString(2, thuePhong.soPhong());
            p.setDate(3, Date.valueOf(thuePhong.ngayBatDau()));

            setDateOrNull(p, 4, thuePhong.ngayKetThuc());
            setDateOrNull(p, 5, thuePhong.ngayKyHopDong());
            setDateOrNull(p, 6, thuePhong.ngayHetHanHopDong());

            if (thuePhong.tienCoc() == null) {
                p.setNull(7, Types.DECIMAL);
            } else {
                p.setBigDecimal(7, thuePhong.tienCoc());
            }

            p.executeUpdate();
        }
    }

    // Cap nhat thong tin hop dong
    public boolean update(ThuePhong thuePhong) throws SQLException {
        String sql = """
            UPDATE ThuePhong
            SET MaSV = ?,
                SoPhong = ?,
                NgayBatDau = ?,
                NgayKetThuc = ?,
                NgayKyHopDong = ?,
                NgayHetHanHopDong = ?,
                TienCoc = ?
            WHERE MaThue = ?
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, thuePhong.maSV());
            p.setString(2, thuePhong.soPhong());
            p.setDate(3, Date.valueOf(thuePhong.ngayBatDau()));

            setDateOrNull(p, 4, thuePhong.ngayKetThuc());
            setDateOrNull(p, 5, thuePhong.ngayKyHopDong());
            setDateOrNull(p, 6, thuePhong.ngayHetHanHopDong());

            if (thuePhong.tienCoc() == null) {
                p.setNull(7, Types.DECIMAL);
            } else {
                p.setBigDecimal(7, thuePhong.tienCoc());
            }

            p.setLong(8, thuePhong.maThue());

            return p.executeUpdate() > 0;
        }
    }

    // Ket thuc hop dong theo ma sinh vien
    public boolean closeByStudent(
            String maSV,
            java.time.LocalDate ngayKetThuc
    ) throws SQLException {

        String sql = """
            UPDATE ThuePhong
            SET NgayKetThuc = ?
            WHERE MaSV = ? AND NgayKetThuc IS NULL
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setDate(1, Date.valueOf(ngayKetThuc));
            p.setString(2, maSV);

            return p.executeUpdate() > 0;
        }
    }

    // Chuyen LocalDate thanh SQL Date, cho phep gia tri null
    private void setDateOrNull(
            PreparedStatement p,
            int index,
            java.time.LocalDate value
    ) throws SQLException {

        if (value == null) {
            p.setNull(index, Types.DATE);
        } else {
            p.setDate(index, Date.valueOf(value));
        }
    }

    // Chuyen mot dong du lieu trong database thanh doi tuong ThuePhong
    private ThuePhong mapRow(ResultSet r) throws SQLException {

        return new ThuePhong(
            r.getLong("MaThue"),
            r.getString("MaSV"),
            r.getString("SoPhong"),

            r.getDate("NgayBatDau") == null
                ? null
                : r.getDate("NgayBatDau").toLocalDate(),

            r.getDate("NgayKetThuc") == null
                ? null
                : r.getDate("NgayKetThuc").toLocalDate(),

            r.getDate("NgayKyHopDong") == null
                ? null
                : r.getDate("NgayKyHopDong").toLocalDate(),

            r.getDate("NgayHetHanHopDong") == null
                ? null
                : r.getDate("NgayHetHanHopDong").toLocalDate(),

            r.getBigDecimal("TienCoc")
        );
    }
}
