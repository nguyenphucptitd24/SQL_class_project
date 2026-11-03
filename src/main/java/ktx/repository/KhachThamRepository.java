package ktx.repository;

import ktx.database.DatabaseConnection;
import ktx.model.Khach;
import ktx.model.LuotTham;
import ktx.model.SinhVien;
import ktx.model.ThongKeKhachTham;
import ktx.model.TongKhachTham;
import ktx.model.TongSinhVienDuocTham;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Truy cập bảng Khach và LuotTham. Toàn bộ SQL của module khách thăm nằm ở đây.
 * Các phương thức nhận Connection dùng cho transaction do KhachThamService điều khiển.
 */
public class KhachThamRepository {
    /** Mốc "vô cực" khi lượt thăm chưa có giờ về (dùng để kiểm tra chồng lấn thời gian). */
    private static final Timestamp VO_CUC = Timestamp.valueOf(LocalDateTime.of(9999, 1, 1, 0, 0));

    private static final String SELECT_LUOT =
        "SELECT lt.MaLuotTham, lt.SoCMTKhach, k.HoTen, lt.MaSV, lt.ThoiGianDen, lt.ThoiGianDi "
      + "FROM LuotTham lt JOIN Khach k ON k.SoCMT = lt.SoCMTKhach ";

    // ------------------------------------------------------------------ Khách

    public List<Khach> findAllKhach() throws SQLException {
        return queryKhach("SELECT SoCMT, HoTen, NgaySinh FROM Khach ORDER BY HoTen, SoCMT", null);
    }

    public List<Khach> searchKhach(String keyword) throws SQLException {
        String k = "%" + keyword.trim() + "%";
        return queryKhach("SELECT SoCMT, HoTen, NgaySinh FROM Khach WHERE SoCMT LIKE ? OR HoTen LIKE ? "
                        + "ORDER BY HoTen, SoCMT", new String[]{k, k});
    }

    public Optional<Khach> findKhach(String soCMT) throws SQLException {
        List<Khach> ds = queryKhach("SELECT SoCMT, HoTen, NgaySinh FROM Khach WHERE SoCMT = ?", new String[]{soCMT});
        return ds.isEmpty() ? Optional.empty() : Optional.of(ds.get(0));
    }

    public void insertKhach(Khach k) throws SQLException {
        String sql = "INSERT INTO Khach(SoCMT, HoTen, NgaySinh) VALUES(?,?,?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, k.soCMT());
            p.setString(2, k.hoTen());
            p.setDate(3, Date.valueOf(k.ngaySinh()));
            p.executeUpdate();
        }
    }

    /** SoCMT là khóa chính nên không đổi được; chỉ sửa họ tên và ngày sinh. */
    public boolean updateKhach(Khach k) throws SQLException {
        String sql = "UPDATE Khach SET HoTen = ?, NgaySinh = ? WHERE SoCMT = ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, k.hoTen());
            p.setDate(2, Date.valueOf(k.ngaySinh()));
            p.setString(3, k.soCMT());
            return p.executeUpdate() > 0;
        }
    }

    private List<Khach> queryKhach(String sql, String[] args) throws SQLException {
        List<Khach> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            if (args != null) for (int i = 0; i < args.length; i++) p.setString(i + 1, args[i]);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(new Khach(r.getString("SoCMT"), r.getString("HoTen"),
                                                   r.getDate("NgaySinh").toLocalDate()));
            }
        }
        return out;
    }

    // ------------------------------------------------------------ Lượt thăm

    /** Lịch sử thăm của một sinh viên (khớp chính xác MaSV), mới nhất trước. */
    public List<LuotTham> findLuotThamByMaSV(String maSV) throws SQLException {
        return queryLuot(SELECT_LUOT + "WHERE lt.MaSV = ? ORDER BY lt.ThoiGianDen DESC", List.of(maSV));
    }

    /** Lịch sử thăm của một khách (khớp chính xác SoCMT), mới nhất trước. */
    public List<LuotTham> findLuotThamByKhach(String soCMT) throws SQLException {
        return queryLuot(SELECT_LUOT + "WHERE lt.SoCMTKhach = ? ORDER BY lt.ThoiGianDen DESC", List.of(soCMT));
    }

    /** Khách đang ở trong KTX (chưa ghi nhận giờ về). */
    public List<LuotTham> findLuotDangMo() throws SQLException {
        return queryLuot(SELECT_LUOT + "WHERE lt.ThoiGianDi IS NULL ORDER BY lt.ThoiGianDen", List.of());
    }

    /**
     * Tìm lượt thăm theo từ khóa (CMT khách, tên khách hoặc mã sinh viên) và khoảng thời gian đến.
     * Tham số nào null/rỗng thì bỏ qua điều kiện đó.
     */
    public List<LuotTham> searchLuotTham(String keyword, LocalDateTime tu, LocalDateTime den) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_LUOT).append("WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            String k = "%" + keyword.trim() + "%";
            sql.append("AND (lt.SoCMTKhach LIKE ? OR k.HoTen LIKE ? OR lt.MaSV LIKE ?) ");
            args.add(k); args.add(k); args.add(k);
        }
        if (tu != null)  { sql.append("AND lt.ThoiGianDen >= ? "); args.add(Timestamp.valueOf(tu)); }
        if (den != null) { sql.append("AND lt.ThoiGianDen < ? ");  args.add(Timestamp.valueOf(den)); }
        sql.append("ORDER BY lt.ThoiGianDen DESC");
        return queryLuot(sql.toString(), args);
    }

    private List<LuotTham> queryLuot(String sql, List<?> args) throws SQLException {
        List<LuotTham> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < args.size(); i++) p.setObject(i + 1, args.get(i));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(mapLuot(r));
            }
        }
        return out;
    }

    private LuotTham mapLuot(ResultSet r) throws SQLException {
        Timestamp di = r.getTimestamp("ThoiGianDi");
        return new LuotTham(r.getLong("MaLuotTham"), r.getString("SoCMTKhach"), r.getString("HoTen"),
                r.getString("MaSV"), r.getTimestamp("ThoiGianDen").toLocalDateTime(),
                di == null ? null : di.toLocalDateTime());
    }

    // ------------------------------------- Dùng trong transaction (Service điều khiển)

    /** Khóa dòng khách để hai thao tác ghi nhận đồng thời cho cùng một khách không chạy song song. */
    public boolean khoaKhach(Connection c, String soCMT) throws SQLException {
        return exists(c, "SELECT 1 FROM Khach WHERE SoCMT = ? FOR UPDATE", soCMT);
    }

    public boolean sinhVienTonTai(Connection c, String maSV) throws SQLException {
        return exists(c, "SELECT 1 FROM SinhVien WHERE MaSV = ?", maSV);
    }

    /** Sinh viên đang ở KTX = có hợp đồng thuê phòng chưa kết thúc. */
    public boolean sinhVienDangO(Connection c, String maSV) throws SQLException {
        return exists(c, "SELECT 1 FROM ThuePhong WHERE MaSV = ? AND NgayKetThuc IS NULL LIMIT 1", maSV);
    }

    /**
     * Khách đã có lượt thăm nào chồng lấn với [den, di) chưa? di == null nghĩa là lượt mới chưa kết thúc.
     * Hai khoảng chồng lấn khi: đầu này < cuối kia và đầu kia < cuối này.
     */
    public boolean trungThoiGian(Connection c, String soCMT, LocalDateTime den, LocalDateTime di) throws SQLException {
        String sql = "SELECT 1 FROM LuotTham WHERE SoCMTKhach = ? AND ThoiGianDen < ? "
                   + "AND COALESCE(ThoiGianDi, ?) > ? LIMIT 1";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, soCMT);
            p.setTimestamp(2, di == null ? VO_CUC : Timestamp.valueOf(di));
            p.setTimestamp(3, VO_CUC);
            p.setTimestamp(4, Timestamp.valueOf(den));
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    public long insertLuotTham(Connection c, String soCMT, String maSV,
                               LocalDateTime den, LocalDateTime di) throws SQLException {
        String sql = "INSERT INTO LuotTham(SoCMTKhach, MaSV, ThoiGianDen, ThoiGianDi) VALUES(?,?,?,?)";
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, soCMT);
            p.setString(2, maSV);
            p.setTimestamp(3, Timestamp.valueOf(den));
            p.setTimestamp(4, di == null ? null : Timestamp.valueOf(di));
            p.executeUpdate();
            try (ResultSet k = p.getGeneratedKeys()) { return k.next() ? k.getLong(1) : -1; }
        }
    }

    /** Đọc và khóa một lượt thăm để ghi nhận giờ về. */
    public Optional<LuotTham> khoaLuotTham(Connection c, long maLuotTham) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(SELECT_LUOT + "WHERE lt.MaLuotTham = ? FOR UPDATE")) {
            p.setLong(1, maLuotTham);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(mapLuot(r)) : Optional.empty(); }
        }
    }

    public boolean capNhatGioVe(Connection c, long maLuotTham, LocalDateTime di) throws SQLException {
        String sql = "UPDATE LuotTham SET ThoiGianDi = ? WHERE MaLuotTham = ? AND ThoiGianDi IS NULL";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, Timestamp.valueOf(di));
            p.setLong(2, maLuotTham);
            return p.executeUpdate() > 0;
        }
    }

    private boolean exists(Connection c, String sql, String arg) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, arg);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    // ------------------------------------------------------------- Thống kê

    /** Mỗi dòng = (sinh viên, khách đã thăm sinh viên đó) kèm số lần khách đến trong khoảng [tu, den). */
    public List<ThongKeKhachTham> thongKeKhachTheoSinhVien(LocalDateTime tu, LocalDateTime den) throws SQLException {
        String sql =
            "SELECT sv.MaSV, sv.SoCMT AS SvCMT, sv.NgaySinh AS SvNgaySinh, sv.Lop, sv.QueQuan, "
          + "       k.SoCMT AS KhachCMT, k.HoTen, k.NgaySinh AS KhachNgaySinh, COUNT(*) AS SoLan "
          + "FROM LuotTham lt "
          + "JOIN SinhVien sv ON sv.MaSV = lt.MaSV "
          + "JOIN Khach k ON k.SoCMT = lt.SoCMTKhach "
          + "WHERE lt.ThoiGianDen >= ? AND lt.ThoiGianDen < ? "
          + "GROUP BY sv.MaSV, sv.SoCMT, sv.NgaySinh, sv.Lop, sv.QueQuan, k.SoCMT, k.HoTen, k.NgaySinh "
          + "ORDER BY sv.MaSV, SoLan DESC, k.HoTen";
        List<ThongKeKhachTham> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, Timestamp.valueOf(tu));
            p.setTimestamp(2, Timestamp.valueOf(den));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(new ThongKeKhachTham(mapSinhVien(r),
                        new Khach(r.getString("KhachCMT"), r.getString("HoTen"),
                                  r.getDate("KhachNgaySinh").toLocalDate()),
                        r.getInt("SoLan")));
            }
        }
        return out;
    }

    /** Tổng số lần mỗi khách đến KTX trong khoảng, nhiều lần nhất trước. */
    public List<TongKhachTham> thongKeTheoKhach(LocalDateTime tu, LocalDateTime den) throws SQLException {
        String sql =
            "SELECT k.SoCMT, k.HoTen, k.NgaySinh, COUNT(*) AS TongSoLan, COUNT(DISTINCT lt.MaSV) AS SoSV "
          + "FROM LuotTham lt JOIN Khach k ON k.SoCMT = lt.SoCMTKhach "
          + "WHERE lt.ThoiGianDen >= ? AND lt.ThoiGianDen < ? "
          + "GROUP BY k.SoCMT, k.HoTen, k.NgaySinh "
          + "ORDER BY TongSoLan DESC, k.HoTen";
        List<TongKhachTham> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, Timestamp.valueOf(tu));
            p.setTimestamp(2, Timestamp.valueOf(den));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(new TongKhachTham(
                        new Khach(r.getString("SoCMT"), r.getString("HoTen"), r.getDate("NgaySinh").toLocalDate()),
                        r.getInt("TongSoLan"), r.getInt("SoSV")));
            }
        }
        return out;
    }

    /** Số lượt và số khách khác nhau của mọi sinh viên trong khoảng; sinh viên không có khách vẫn hiện với 0. */
    public List<TongSinhVienDuocTham> thongKeTheoSinhVien(LocalDateTime tu, LocalDateTime den) throws SQLException {
        String sql =
            "SELECT sv.MaSV, sv.SoCMT AS SvCMT, sv.NgaySinh AS SvNgaySinh, sv.Lop, sv.QueQuan, "
          + "       COUNT(lt.MaLuotTham) AS SoLuot, COUNT(DISTINCT lt.SoCMTKhach) AS SoKhach "
          + "FROM SinhVien sv "
          + "LEFT JOIN LuotTham lt ON lt.MaSV = sv.MaSV AND lt.ThoiGianDen >= ? AND lt.ThoiGianDen < ? "
          + "GROUP BY sv.MaSV, sv.SoCMT, sv.NgaySinh, sv.Lop, sv.QueQuan "
          + "ORDER BY SoLuot DESC, sv.MaSV";
        List<TongSinhVienDuocTham> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, Timestamp.valueOf(tu));
            p.setTimestamp(2, Timestamp.valueOf(den));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(new TongSinhVienDuocTham(mapSinhVien(r), r.getInt("SoLuot"), r.getInt("SoKhach")));
            }
        }
        return out;
    }

    private SinhVien mapSinhVien(ResultSet r) throws SQLException {
        return new SinhVien(r.getString("MaSV"), r.getString("SvCMT"),
                r.getDate("SvNgaySinh").toLocalDate(), r.getString("Lop"), r.getString("QueQuan"));
    }
}
