package ktx.service;

import ktx.database.DatabaseConnection;
import ktx.model.BaoHong;
import ktx.model.BoLocBaoHong;
import ktx.model.KhoangThoiGian;
import ktx.model.LichSuBaoHong;
import ktx.model.ThoiGianXuLyTB;
import ktx.model.ThongKeNhomBaoHong;
import ktx.repository.BaoHongRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Nghiệp vụ báo hỏng và bảo trì phòng. Giao diện chỉ gọi lớp này.
 * Vi phạm quy tắc -> IllegalArgumentException (thông báo tiếng Việt); lỗi cơ sở dữ liệu -> SQLException.
 */
public class BaoHongService {
    private final BaoHongRepository repo = new BaoHongRepository();

    private interface TxAction<T> { T run(Connection c) throws SQLException; }

    // ------------------------------------------------------------ Tạo báo hỏng

    /**
     * Tạo báo hỏng. maSV rỗng nghĩa là nhân viên phát hiện (khi đó nguoiGhiNhan là bắt buộc).
     * Sinh viên chỉ được báo hỏng cho phòng mình đang ở. Trả về mã báo hỏng mới.
     * Báo hỏng và dòng lịch sử đầu tiên được ghi trong cùng một transaction.
     */
    public long taoBaoHong(String soPhong, String maSV, String nguoiGhiNhan, String loaiSuCo,
                           String moTa, String mucDo) throws SQLException {
        BaoHongRules.TaoMoi d = BaoHongRules.kiemTraTaoMoi(soPhong, maSV, nguoiGhiNhan, loaiSuCo, moTa, mucDo);
        // Cắt về phút để thao tác xử lý ngay sau đó (nhập theo phút) không bị coi là "trước ngày báo".
        LocalDateTime bayGio = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        return inTransaction(c -> {
            if (!repo.phongTonTai(c, d.soPhong()))
                throw new IllegalArgumentException("Phòng " + d.soPhong() + " không tồn tại.");
            if (d.maSV() != null) {
                if (!repo.sinhVienTonTai(c, d.maSV()))
                    throw new IllegalArgumentException("Sinh viên " + d.maSV() + " không tồn tại.");
                if (!repo.sinhVienOPhongVaoNgay(c, d.maSV(), d.soPhong(), bayGio.toLocalDate()))
                    throw new IllegalArgumentException("Sinh viên " + d.maSV() + " không ở phòng " + d.soPhong()
                            + " tại thời điểm báo nên không thể báo hỏng cho phòng này.");
            }
            long ma = repo.insert(c, d.soPhong(), d.maSV(), d.loaiSuCo(), d.moTa(), d.mucDo(), bayGio);
            repo.insertLichSu(c, ma, null, "Mới báo", d.nguoiGhiNhan(),
                    d.maSV() != null ? "Sinh viên gửi báo hỏng" : "Nhân viên phát hiện và ghi nhận sự cố", bayGio);
            return ma;
        });
    }

    // ------------------------------------------------------- Theo dõi xử lý

    /**
     * Đổi trạng thái báo hỏng theo đúng thứ tự cho phép, ghi nhân viên, thời điểm, kết quả và dòng lịch sử.
     *
     * @param nhanVien       nhân viên xử lý; bỏ trống thì giữ nhân viên đang phụ trách
     * @param ghiChuKetQua   kết quả xử lý hoặc lý do từ chối (bắt buộc khi kết thúc); với Đang xử lý là ghi chú tùy chọn
     * @param thoiDiem       thời điểm xảy ra; null nghĩa là bây giờ. Không được trước ngày báo
     * @return báo hỏng sau khi cập nhật
     */
    public BaoHong capNhatTrangThai(long maBaoHong, String trangThaiMoi, String nhanVien,
                                    String ghiChuKetQua, LocalDateTime thoiDiem) throws SQLException {
        return inTransaction(c -> {
            BaoHong ht = repo.khoa(c, maBaoHong)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo hỏng số " + maBaoHong + "."));
            LocalDateTime buocTruoc = repo.thoiGianLichSuCuoi(c, maBaoHong).orElse(ht.ngayBao());
            BaoHongRules.CapNhat cn = BaoHongRules.kiemTraCapNhat(ht, trangThaiMoi, nhanVien, ghiChuKetQua,
                    thoiDiem, buocTruoc, LocalDateTime.now());
            if (!repo.capNhat(c, maBaoHong, ht.trangThai(), cn.trangThaiMoi(), cn.nhanVienXuLy(), cn.ngayXuLy(), cn.ketQuaXuLy()))
                throw new IllegalArgumentException("Báo hỏng vừa được người khác cập nhật; hãy tải lại rồi thử lại.");
            repo.insertLichSu(c, maBaoHong, ht.trangThai(), cn.trangThaiMoi(), cn.nhanVienXuLy(), cn.ghiChu(), cn.thoiGian());
            return repo.khoa(c, maBaoHong).orElseThrow();
        });
    }

    /** Chỉ xóa được báo hỏng còn "Mới báo" (ví dụ gửi nhầm). Báo hỏng đã xử lý phải giữ để lưu lịch sử. */
    public void xoaBaoHong(long maBaoHong) throws SQLException {
        inTransaction(c -> {
            BaoHong ht = repo.khoa(c, maBaoHong)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo hỏng số " + maBaoHong + "."));
            if (!"Mới báo".equals(ht.trangThai()))
                throw new IllegalArgumentException("Chỉ được xóa báo hỏng ở trạng thái \"Mới báo\". Báo hỏng \""
                        + ht.trangThai() + "\" phải giữ lại để lưu lịch sử; nếu báo nhầm hãy chuyển sang \"Từ chối\".");
            if (!repo.xoaNeuMoiBao(c, maBaoHong))
                throw new IllegalArgumentException("Báo hỏng vừa được người khác cập nhật; hãy tải lại.");
            return null;
        });
    }

    // ------------------------------------------------------------ Tra cứu

    public List<String> danhSachPhong() throws SQLException { return repo.tatCaSoPhong(); }

    /** Phòng sinh viên đang ở, để giao diện tự điền khi nhập mã sinh viên. */
    public Optional<String> phongDangO(String maSV) throws SQLException {
        return maSV == null || maSV.isBlank() ? Optional.empty() : repo.phongDangO(maSV.trim());
    }

    public BaoHong layBaoHong(long maBaoHong) throws SQLException {
        return repo.findById(maBaoHong)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo hỏng số " + maBaoHong + "."));
    }

    public List<LichSuBaoHong> lichSuXuLy(long maBaoHong) throws SQLException { return repo.findLichSu(maBaoHong); }

    public List<BaoHong> timBaoHong(BoLocBaoHong boLoc) throws SQLException { return repo.search(boLoc); }

    /** Báo hỏng mới tiếp nhận (chưa ai xử lý), mức độ nặng lên trước. */
    public List<BaoHong> moiTiepNhan() throws SQLException {
        return repo.search(new BoLocBaoHong(null, null, "Mới báo", false, null, null, null, null, true));
    }

    /** Sự cố chưa hoàn thành (Mới báo + Đang xử lý), mức độ nặng lên trước. */
    public List<BaoHong> chuaHoanThanh() throws SQLException {
        return repo.search(new BoLocBaoHong(null, null, null, true, null, null, null, null, true));
    }

    public List<BaoHong> lichSuTheoPhong(String soPhong) throws SQLException {
        if (soPhong == null || soPhong.isBlank()) throw new IllegalArgumentException("Hãy chọn số phòng.");
        return repo.search(new BoLocBaoHong(soPhong.trim(), null, null, false, null, null, null, null, false));
    }

    public List<BaoHong> lichSuTheoSinhVien(String maSV) throws SQLException {
        if (maSV == null || maSV.isBlank()) throw new IllegalArgumentException("Hãy nhập mã sinh viên.");
        return repo.search(new BoLocBaoHong(null, maSV.trim(), null, false, null, null, null, null, false));
    }

    // ------------------------------------------------------------ Thống kê (kỳ theo ngày báo)

    public List<ThongKeNhomBaoHong> thongKeTheoLoai(KhoangThoiGian k) throws SQLException { return repo.thongKeTheoLoai(k.tu(), k.den()); }

    public List<ThongKeNhomBaoHong> thongKeTheoMucDo(KhoangThoiGian k) throws SQLException { return repo.thongKeTheoMucDo(k.tu(), k.den()); }

    public List<ThongKeNhomBaoHong> thongKeTheoThang(KhoangThoiGian k) throws SQLException { return repo.thongKeTheoThang(k.tu(), k.den()); }

    /** Trung bình chung (giờ) của báo hỏng đã hoàn thành trong kỳ; rỗng nếu chưa có báo hỏng nào hoàn thành. */
    public Optional<ThoiGianXuLyTB> thoiGianXuLyTrungBinh(KhoangThoiGian k) throws SQLException {
        return repo.thoiGianXuLyTrungBinh(k.tu(), k.den(), false).stream().findFirst();
    }

    public List<ThoiGianXuLyTB> thoiGianXuLyTheoMucDo(KhoangThoiGian k) throws SQLException {
        return repo.thoiGianXuLyTrungBinh(k.tu(), k.den(), true);
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
}
