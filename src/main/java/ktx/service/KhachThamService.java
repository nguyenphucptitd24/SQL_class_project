package ktx.service;

import ktx.database.DatabaseConnection;
import ktx.model.Khach;
import ktx.model.KhoangThoiGian;
import ktx.model.LuotTham;
import ktx.model.ThongKeKhachTham;
import ktx.model.TongKhachTham;
import ktx.model.TongSinhVienDuocTham;
import ktx.repository.KhachThamRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Nghiệp vụ module khách thăm. Giao diện chỉ gọi lớp này.
 * Vi phạm quy tắc nghiệp vụ / dữ liệu sai -> IllegalArgumentException (thông báo tiếng Việt, hiển thị thẳng cho người dùng).
 * Lỗi cơ sở dữ liệu -> SQLException.
 */
public class KhachThamService {
    private final KhachThamRepository repo = new KhachThamRepository();

    private interface TxAction<T> { T run(Connection c) throws SQLException; }

    // ------------------------------------------------------------------ Khách

    public void themKhach(Khach k) throws SQLException {
        Khach da = chuanHoaKhach(k);
        try {
            repo.insertKhach(da);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Số CMT " + da.soCMT() + " đã có trong danh sách khách.");
        }
    }

    public void suaKhach(Khach k) throws SQLException {
        Khach da = chuanHoaKhach(k);
        if (!repo.updateKhach(da)) throw new IllegalArgumentException("Không tìm thấy khách có số CMT " + da.soCMT() + ".");
    }

    public List<Khach> tatCaKhach() throws SQLException { return repo.findAllKhach(); }

    /** Tra cứu chính xác một khách theo số CMT (rỗng nếu chưa có). */
    public Optional<Khach> layKhach(String soCMT) throws SQLException {
        return blank(soCMT) ? Optional.empty() : repo.findKhach(soCMT.trim());
    }

    /** Tìm theo số CMT hoặc họ tên (khớp một phần). Từ khóa rỗng thì trả về tất cả. */
    public List<Khach> timKhach(String tuKhoa) throws SQLException {
        return blank(tuKhoa) ? repo.findAllKhach() : repo.searchKhach(tuKhoa);
    }

    private Khach chuanHoaKhach(Khach k) {
        if (k == null || blank(k.soCMT()) || blank(k.hoTen()) || k.ngaySinh() == null)
            throw new IllegalArgumentException("Hãy nhập đủ số CMT, họ tên và ngày sinh của khách.");
        String cmt = k.soCMT().trim();
        String ten = k.hoTen().trim();
        if (!cmt.matches("[A-Za-z0-9]{6,20}"))
            throw new IllegalArgumentException("Số CMT chỉ gồm chữ và số, dài từ 6 đến 20 ký tự.");
        if (ten.length() > 100) throw new IllegalArgumentException("Họ tên không được quá 100 ký tự.");
        if (!k.ngaySinh().isBefore(LocalDate.now()) || k.ngaySinh().getYear() < 1900)
            throw new IllegalArgumentException("Ngày sinh không hợp lệ.");
        return new Khach(cmt, ten, k.ngaySinh());
    }

    // ------------------------------------------------------------ Lượt thăm

    /** Khách vừa đến: ghi nhận lượt thăm, chưa có giờ về. Trả về mã lượt thăm mới. */
    public long ghiNhanDen(String soCMT, String maSV, LocalDateTime thoiGianDen) throws SQLException {
        return ghiNhanLuotTham(soCMT, maSV, thoiGianDen, null);
    }

    /**
     * Ghi nhận một lượt thăm. thoiGianDi có thể null (khách chưa về).
     * Có giờ về thì dùng để nhập bù lượt thăm đã kết thúc.
     */
    public long ghiNhanLuotTham(String soCMT, String maSV, LocalDateTime den, LocalDateTime di) throws SQLException {
        if (blank(soCMT) || blank(maSV) || den == null)
            throw new IllegalArgumentException("Hãy chọn khách, sinh viên được thăm và nhập giờ đến.");
        String cmt = soCMT.trim();
        String sv = maSV.trim();
        LocalDateTime bayGio = LocalDateTime.now();
        if (den.isAfter(bayGio)) throw new IllegalArgumentException("Giờ đến không được ở tương lai.");
        if (di != null) {
            if (di.isBefore(den)) throw new IllegalArgumentException("Giờ về không được trước giờ đến.");
            if (di.isAfter(bayGio)) throw new IllegalArgumentException("Giờ về không được ở tương lai.");
        }
        return inTransaction(c -> {
            if (!repo.khoaKhach(c, cmt))
                throw new IllegalArgumentException("Khách chưa có trong hệ thống; hãy thêm khách trước.");
            if (!repo.sinhVienTonTai(c, sv))
                throw new IllegalArgumentException("Sinh viên " + sv + " không tồn tại.");
            if (!repo.sinhVienDangO(c, sv))
                throw new IllegalArgumentException("Sinh viên " + sv + " hiện không ở trong KTX nên không thể ghi nhận khách đến thăm.");
            if (repo.trungThoiGian(c, cmt, den, di))
                throw new IllegalArgumentException("Khách này đang có lượt thăm chưa kết thúc hoặc trùng khoảng thời gian; "
                        + "hãy ghi nhận giờ về cho lượt trước.");
            return repo.insertLuotTham(c, cmt, sv, den, di);
        });
    }

    /** Khách ra về: ghi nhận giờ về cho một lượt thăm đang mở. */
    public void ghiNhanVe(long maLuotTham, LocalDateTime thoiGianDi) throws SQLException {
        if (thoiGianDi == null) throw new IllegalArgumentException("Hãy nhập giờ về.");
        if (thoiGianDi.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Giờ về không được ở tương lai.");
        inTransaction(c -> {
            LuotTham lt = repo.khoaLuotTham(c, maLuotTham)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lượt thăm số " + maLuotTham + "."));
            if (!lt.dangO()) throw new IllegalArgumentException("Lượt thăm này đã được ghi nhận giờ về.");
            if (thoiGianDi.isBefore(lt.thoiGianDen()))
                throw new IllegalArgumentException("Giờ về không được trước giờ đến (" + lt.thoiGianDen() + ").");
            if (!repo.capNhatGioVe(c, maLuotTham, thoiGianDi))
                throw new IllegalArgumentException("Không cập nhật được giờ về cho lượt thăm này.");
            return null;
        });
    }

    // ------------------------------------------------------------ Tra cứu

    public List<LuotTham> lichSuTheoSinhVien(String maSV) throws SQLException {
        if (blank(maSV)) throw new IllegalArgumentException("Hãy nhập mã sinh viên.");
        return repo.findLuotThamByMaSV(maSV.trim());
    }

    public List<LuotTham> lichSuTheoKhach(String soCMT) throws SQLException {
        if (blank(soCMT)) throw new IllegalArgumentException("Hãy nhập số CMT của khách.");
        return repo.findLuotThamByKhach(soCMT.trim());
    }

    /** Khách hiện đang ở trong KTX. */
    public List<LuotTham> khachDangO() throws SQLException { return repo.findLuotDangMo(); }

    /** Tìm theo từ khóa (CMT khách, tên khách, mã sinh viên) và khoảng thời gian; cả hai đều có thể bỏ trống. */
    public List<LuotTham> timLuotTham(String tuKhoa, KhoangThoiGian khoang) throws SQLException {
        return repo.searchLuotTham(tuKhoa, khoang == null ? null : khoang.tu(), khoang == null ? null : khoang.den());
    }

    // ------------------------------------------------------------ Thống kê

    /** Sinh viên cùng từng khách đến thăm và số lần khách đến (theo tuần, tháng hoặc khoảng tùy chọn). */
    public List<ThongKeKhachTham> thongKeKhachTheoSinhVien(KhoangThoiGian k) throws SQLException {
        return repo.thongKeKhachTheoSinhVien(k.tu(), k.den());
    }

    public List<TongKhachTham> thongKeTheoKhach(KhoangThoiGian k) throws SQLException {
        return repo.thongKeTheoKhach(k.tu(), k.den());
    }

    public List<TongSinhVienDuocTham> thongKeTheoSinhVien(KhoangThoiGian k) throws SQLException {
        return repo.thongKeTheoSinhVien(k.tu(), k.den());
    }

    // ----------------------------------------------------------------- Nội bộ

    private <T> T inTransaction(TxAction<T> action) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                T kq = action.run(c);
                c.commit();
                return kq;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private boolean blank(String s) { return s == null || s.isBlank(); }
}
