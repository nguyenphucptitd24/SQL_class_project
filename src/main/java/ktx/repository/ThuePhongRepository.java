package ktx.repository;

import ktx.database.DatabaseConnection;
import ktx.model.ThuePhong;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ThuePhongRepository {

    public List<ThuePhong> findAll() throws SQLException {
        String sql = "SELECT MaThue, MaSV, SoPhong, NgayBatDau, NgayKetThuc FROM ThuePhong ORDER BY MaThue";
        List<ThuePhong> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(new ThuePhong(
                    r.getLong("MaThue"),
                    r.getString("MaSV"),
                    r.getString("SoPhong"),
                    r.getDate("NgayBatDau").toLocalDate(),
                    r.getDate("NgayKetThuc") == null ? null : r.getDate("NgayKetThuc").toLocalDate()
                ));
            }
        }
        return list;
    }

    public List<ThuePhong> findActiveByStudent(String maSV) throws SQLException {
        String sql = "SELECT MaThue, MaSV, SoPhong, NgayBatDau, NgayKetThuc FROM ThuePhong WHERE MaSV = ? AND NgayKetThuc IS NULL";
        List<ThuePhong> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSV);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(mapRow(r));
                }
            }
        }
        return list;
    }

    public List<ThuePhong> findActiveByRoom(String soPhong) throws SQLException {
        String sql = "SELECT MaThue, MaSV, SoPhong, NgayBatDau, NgayKetThuc FROM ThuePhong WHERE SoPhong = ? AND NgayKetThuc IS NULL";
        List<ThuePhong> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, soPhong);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(mapRow(r));
                }
            }
        }
        return list;
    }

    public boolean existsActiveByStudent(String maSV) throws SQLException {
        String sql = "SELECT 1 FROM ThuePhong WHERE MaSV = ? AND NgayKetThuc IS NULL LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSV);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public void insert(ThuePhong thuePhong) throws SQLException {
        String sql = "INSERT INTO ThuePhong(MaSV, SoPhong, NgayBatDau, NgayKetThuc) VALUES(?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, thuePhong.maSV());
            p.setString(2, thuePhong.soPhong());
            p.setDate(3, Date.valueOf(thuePhong.ngayBatDau()));
            if (thuePhong.ngayKetThuc() == null) {
                p.setNull(4, java.sql.Types.DATE);
            } else {
                p.setDate(4, Date.valueOf(thuePhong.ngayKetThuc()));
            }
            p.executeUpdate();
        }
    }

    public boolean closeByStudent(String maSV, LocalDate ngayKetThuc) throws SQLException {
        String sql = "UPDATE ThuePhong SET NgayKetThuc = ? WHERE MaSV = ? AND NgayKetThuc IS NULL";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, Date.valueOf(ngayKetThuc));
            p.setString(2, maSV);
            return p.executeUpdate() > 0;
        }
    }

    private ThuePhong mapRow(ResultSet r) throws SQLException {
        LocalDate ngayKetThuc = r.getDate("NgayKetThuc") == null ? null : r.getDate("NgayKetThuc").toLocalDate();
        return new ThuePhong(
            r.getLong("MaThue"),
            r.getString("MaSV"),
            r.getString("SoPhong"),
            r.getDate("NgayBatDau").toLocalDate(),
            ngayKetThuc
        );
    }
}
