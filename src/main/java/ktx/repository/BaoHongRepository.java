package ktx.repository;

import ktx.database.DatabaseConnection;
import ktx.model.BaoHong;
import ktx.model.BoLocBaoHong;
import ktx.model.LichSuBaoHong;
import ktx.model.ThoiGianXuLyTB;
import ktx.model.ThongKeNhomBaoHong;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Truy cập bảng BaoHong và LichSuBaoHong. Toàn bộ SQL của module báo hỏng nằm ở đây.
 * Các phương thức nhận Connection dùng trong transaction do BaoHongService điều khiển.
 */
public class BaoHongRepository {
    private static final String SELECT =
        "SELECT b.MaBaoHong, b.SoPhong, b.MaSV, b.LoaiSuCo, b.MoTa, b.MucDo, b.TrangThai, "
      + "b.NgayBao, b.NgayXuLy, b.NhanVienXuLy, b.KetQuaXuLy FROM BaoHong b ";
    private static final String THU_TU_MUC_DO =
        "CASE b.MucDo WHEN 'Khẩn cấp' THEN 1 WHEN 'Cao' THEN 2 WHEN 'Trung bình' THEN 3 ELSE 4 END";
    private static final String CHUA_KET_THUC = "b.TrangThai IN ('Mới báo', 'Đang xử lý')";

    // --------------------------------------------------------------- Tra cứu

    public List<String> tatCaSoPhong() throws SQLException {
        List<String> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT SoPhong FROM Phong ORDER BY SoPhong");
             ResultSet r = p.executeQuery()) {
            while (r.next()) out.add(r.getString("SoPhong"));
        }
        return out;
    }

    /** Phòng sinh viên đang ở (hợp đồng chưa kết thúc), nếu có. */
    public Optional<String> phongDangO(String maSV) throws SQLException {
        String sql = "SELECT SoPhong FROM ThuePhong WHERE MaSV = ? AND NgayKetThuc IS NULL ORDER BY NgayBatDau DESC LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSV);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(r.getString("SoPhong")) : Optional.empty(); }
        }
    }

    public Optional<BaoHong> findById(long ma) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(SELECT + "WHERE b.MaBaoHong = ?")) {
            p.setLong(1, ma);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(map(r)) : Optional.empty(); }
        }
    }

    public List<BaoHong> search(BoLocBaoHong f) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        if (notBlank(f.soPhong()))   { sql.append("AND b.SoPhong = ? ");  args.add(f.soPhong().trim()); }
        if (notBlank(f.maSV()))      { sql.append("AND b.MaSV = ? ");     args.add(f.maSV().trim()); }
        if (notBlank(f.trangThai())) { sql.append("AND b.TrangThai = ? "); args.add(f.trangThai()); }
        if (f.chuaKetThuc())         { sql.append("AND ").append(CHUA_KET_THUC).append(' '); }
        if (notBlank(f.loaiSuCo()))  { sql.append("AND b.LoaiSuCo = ? "); args.add(f.loaiSuCo()); }
        if (notBlank(f.mucDo()))     { sql.append("AND b.MucDo = ? ");    args.add(f.mucDo()); }
        if (f.tu() != null)          { sql.append("AND b.NgayBao >= ? "); args.add(Timestamp.valueOf(f.tu())); }
        if (f.den() != null)         { sql.append("AND b.NgayBao < ? ");  args.add(Timestamp.valueOf(f.den())); }
        sql.append(f.uuTienMucDo() ? "ORDER BY " + THU_TU_MUC_DO + ", b.NgayBao" : "ORDER BY b.NgayBao DESC, b.MaBaoHong DESC");
        List<BaoHong> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) p.setObject(i + 1, args.get(i));
            try (ResultSet r = p.executeQuery()) { while (r.next()) out.add(map(r)); }
        }
        return out;
    }

    public List<LichSuBaoHong> findLichSu(long maBaoHong) throws SQLException {
        String sql = "SELECT l.MaLichSu, l.MaBaoHong, l.TrangThaiCu, l.TrangThaiMoi, l.NhanVien, l.GhiChu, l.ThoiGian "
                   + "FROM LichSuBaoHong l WHERE l.MaBaoHong = ? ORDER BY l.ThoiGian, l.MaLichSu";
        List<LichSuBaoHong> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maBaoHong);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(new LichSuBaoHong(r.getLong("MaLichSu"), r.getLong("MaBaoHong"),
                        r.getString("TrangThaiCu"), r.getString("TrangThaiMoi"), r.getString("NhanVien"),
                        r.getString("GhiChu"), r.getTimestamp("ThoiGian").toLocalDateTime()));
            }
        }
        return out;
    }

    // ------------------------------------- Dùng trong transaction (Service điều khiển)

    public boolean phongTonTai(Connection c, String soPhong) throws SQLException {
        return exists(c, "SELECT 1 FROM Phong WHERE SoPhong = ?", soPhong);
    }

    public boolean sinhVienTonTai(Connection c, String maSV) throws SQLException {
        return exists(c, "SELECT 1 FROM SinhVien WHERE MaSV = ?", maSV);
    }

    /** Sinh viên có hợp đồng thuê đúng phòng đó còn hiệu lực vào ngày đã cho. */
    public boolean sinhVienOPhongVaoNgay(Connection c, String maSV, String soPhong, LocalDate ngay) throws SQLException {
        String sql = "SELECT 1 FROM ThuePhong WHERE MaSV = ? AND SoPhong = ? AND NgayBatDau <= ? "
                   + "AND (NgayKetThuc IS NULL OR NgayKetThuc >= ?) LIMIT 1";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maSV); p.setString(2, soPhong);
            p.setDate(3, java.sql.Date.valueOf(ngay)); p.setDate(4, java.sql.Date.valueOf(ngay));
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    public long insert(Connection c, String soPhong, String maSV, String loai, String moTa, String mucDo,
                       LocalDateTime ngayBao) throws SQLException {
        String sql = "INSERT INTO BaoHong(SoPhong, MaSV, LoaiSuCo, MoTa, MucDo, TrangThai, NgayBao) VALUES(?,?,?,?,?,'Mới báo',?)";
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, soPhong); p.setString(2, maSV); p.setString(3, loai);
            p.setString(4, moTa); p.setString(5, mucDo); p.setTimestamp(6, Timestamp.valueOf(ngayBao));
            p.executeUpdate();
            try (ResultSet k = p.getGeneratedKeys()) { return k.next() ? k.getLong(1) : -1; }
        }
    }

    /** Đọc và khóa một báo hỏng để đổi trạng thái hoặc xóa an toàn khi nhiều người cùng thao tác. */
    public Optional<BaoHong> khoa(Connection c, long ma) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(SELECT + "WHERE b.MaBaoHong = ? FOR UPDATE")) {
            p.setLong(1, ma);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(map(r)) : Optional.empty(); }
        }
    }

    /** Chỉ cập nhật nếu trạng thái vẫn là trangThaiCu (phát hiện người khác đã đổi trước). */
    public boolean capNhat(Connection c, long ma, String trangThaiCu, String trangThaiMoi, String nhanVien,
                           LocalDateTime ngayXuLy, String ketQua) throws SQLException {
        String sql = "UPDATE BaoHong SET TrangThai = ?, NhanVienXuLy = ?, NgayXuLy = ?, KetQuaXuLy = ? "
                   + "WHERE MaBaoHong = ? AND TrangThai = ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, trangThaiMoi); p.setString(2, nhanVien);
            p.setTimestamp(3, ngayXuLy == null ? null : Timestamp.valueOf(ngayXuLy));
            p.setString(4, ketQua); p.setLong(5, ma); p.setString(6, trangThaiCu);
            return p.executeUpdate() > 0;
        }
    }

    public boolean xoaNeuMoiBao(Connection c, long ma) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("DELETE FROM BaoHong WHERE MaBaoHong = ? AND TrangThai = 'Mới báo'")) {
            p.setLong(1, ma);
            return p.executeUpdate() > 0;
        }
    }

    public void insertLichSu(Connection c, long ma, String cu, String moi, String nhanVien, String ghiChu,
                             LocalDateTime thoiGian) throws SQLException {
        String sql = "INSERT INTO LichSuBaoHong(MaBaoHong, TrangThaiCu, TrangThaiMoi, NhanVien, GhiChu, ThoiGian) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, ma); p.setString(2, cu); p.setString(3, moi); p.setString(4, nhanVien);
            p.setString(5, ghiChu); p.setTimestamp(6, Timestamp.valueOf(thoiGian));
            p.executeUpdate();
        }
    }

    /** Thời gian của bước lịch sử gần nhất của một báo hỏng. */
    public Optional<LocalDateTime> thoiGianLichSuCuoi(Connection c, long ma) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT MAX(ThoiGian) AS Cuoi FROM LichSuBaoHong WHERE MaBaoHong = ?")) {
            p.setLong(1, ma);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return Optional.empty();
                Timestamp t = r.getTimestamp("Cuoi");
                return t == null ? Optional.empty() : Optional.of(t.toLocalDateTime());
            }
        }
    }

    // ------------------------------------------------------------- Thống kê

    public List<ThongKeNhomBaoHong> thongKeTheoLoai(LocalDateTime tu, LocalDateTime den) throws SQLException {
        return thongKe("b.LoaiSuCo", "b.LoaiSuCo", "SoSuCo DESC, Nhom", tu, den);
    }

    public List<ThongKeNhomBaoHong> thongKeTheoMucDo(LocalDateTime tu, LocalDateTime den) throws SQLException {
        return thongKe("b.MucDo", "b.MucDo", THU_TU_MUC_DO, tu, den);
    }

    public List<ThongKeNhomBaoHong> thongKeTheoThang(LocalDateTime tu, LocalDateTime den) throws SQLException {
        return thongKe("DATE_FORMAT(b.NgayBao, '%Y-%m')", "DATE_FORMAT(b.NgayBao, '%Y-%m')", "Nhom", tu, den);
    }

    private List<ThongKeNhomBaoHong> thongKe(String chon, String nhom, String order,
                                             LocalDateTime tu, LocalDateTime den) throws SQLException {
        String sql = "SELECT " + chon + " AS Nhom, COUNT(*) AS SoSuCo, "
                   + "SUM(" + CHUA_KET_THUC + ") AS ChuaHoanThanh, "
                   + "SUM(b.TrangThai = 'Đã hoàn thành') AS DaHoanThanh, "
                   + "SUM(b.TrangThai = 'Từ chối') AS TuChoi "
                   + "FROM BaoHong b WHERE b.NgayBao >= ? AND b.NgayBao < ? GROUP BY " + nhom + " ORDER BY " + order;
        List<ThongKeNhomBaoHong> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, Timestamp.valueOf(tu)); p.setTimestamp(2, Timestamp.valueOf(den));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(new ThongKeNhomBaoHong(r.getString("Nhom"), r.getInt("SoSuCo"),
                        r.getInt("ChuaHoanThanh"), r.getInt("DaHoanThanh"), r.getInt("TuChoi")));
            }
        }
        return out;
    }

    /** Thời gian xử lý trung bình (giờ) của báo hỏng ĐÃ HOÀN THÀNH, gộp tất cả hoặc tách theo mức độ. */
    public List<ThoiGianXuLyTB> thoiGianXuLyTrungBinh(LocalDateTime tu, LocalDateTime den, boolean theoMucDo) throws SQLException {
        String nhom = theoMucDo ? "b.MucDo" : "'Tất cả'";
        String sql = "SELECT " + nhom + " AS Nhom, COUNT(*) AS SoBaoHong, "
                   + "ROUND(AVG(TIMESTAMPDIFF(MINUTE, b.NgayBao, b.NgayXuLy)) / 60, 1) AS TrungBinhGio "
                   + "FROM BaoHong b WHERE b.TrangThai = 'Đã hoàn thành' AND b.NgayXuLy IS NOT NULL "
                   + "AND b.NgayBao >= ? AND b.NgayBao < ? "
                   + (theoMucDo ? "GROUP BY b.MucDo ORDER BY " + THU_TU_MUC_DO : "");
        List<ThoiGianXuLyTB> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, Timestamp.valueOf(tu)); p.setTimestamp(2, Timestamp.valueOf(den));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    int so = r.getInt("SoBaoHong");
                    if (so > 0) out.add(new ThoiGianXuLyTB(r.getString("Nhom"), so, r.getDouble("TrungBinhGio")));
                }
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ Nội bộ

    private BaoHong map(ResultSet r) throws SQLException {
        Timestamp xl = r.getTimestamp("NgayXuLy");
        return new BaoHong(r.getLong("MaBaoHong"), r.getString("SoPhong"), r.getString("MaSV"), r.getString("LoaiSuCo"),
                r.getString("MoTa"), r.getString("MucDo"), r.getString("TrangThai"),
                r.getTimestamp("NgayBao").toLocalDateTime(), xl == null ? null : xl.toLocalDateTime(),
                r.getString("NhanVienXuLy"), r.getString("KetQuaXuLy"));
    }

    private boolean exists(Connection c, String sql, String arg) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, arg);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    private boolean notBlank(String s) { return s != null && !s.isBlank(); }
}
