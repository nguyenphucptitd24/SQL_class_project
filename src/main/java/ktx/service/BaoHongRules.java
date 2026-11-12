package ktx.service;

import ktx.model.BaoHong;
import ktx.model.BaoHongDanhMuc;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Quy tắc nghiệp vụ báo hỏng, không đụng tới cơ sở dữ liệu nên kiểm thử độc lập được.
 * Vi phạm -> IllegalArgumentException với thông báo tiếng Việt, hiển thị thẳng cho người dùng.
 */
public final class BaoHongRules {
    private BaoHongRules() {}

    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    static final int MO_TA_TOI_DA = 2000, KET_QUA_TOI_DA = 500;

    /** Dữ liệu đã chuẩn hóa (cắt khoảng trắng) để tạo báo hỏng. */
    public record TaoMoi(String soPhong, String maSV, String nguoiGhiNhan, String loaiSuCo, String moTa, String mucDo) {}

    /** Giá trị cần ghi khi đổi trạng thái; ngayXuLy/ketQuaXuLy chỉ có khi kết thúc xử lý. */
    public record CapNhat(String trangThaiMoi, String nhanVienXuLy, LocalDateTime ngayXuLy,
                          String ketQuaXuLy, String ghiChu, LocalDateTime thoiGian) {}

    /**
     * Kiểm tra dữ liệu tạo báo hỏng. maSV rỗng nghĩa là nhân viên phát hiện: khi đó bắt buộc ghi tên nhân viên.
     */
    public static TaoMoi kiemTraTaoMoi(String soPhong, String maSV, String nguoiGhiNhan,
                                       String loaiSuCo, String moTa, String mucDo) {
        String phong = cat(soPhong), sv = cat(maSV), nv = cat(nguoiGhiNhan), mt = cat(moTa);
        if (phong.isEmpty()) throw new IllegalArgumentException("Hãy chọn số phòng.");
        if (phong.length() > 20) throw new IllegalArgumentException("Số phòng không hợp lệ.");
        if (!BaoHongDanhMuc.thuoc(BaoHongDanhMuc.LOAI, loaiSuCo))
            throw new IllegalArgumentException("Loại sự cố phải là một trong: " + String.join(", ", BaoHongDanhMuc.LOAI) + ".");
        if (!BaoHongDanhMuc.thuoc(BaoHongDanhMuc.MUC_DO, mucDo))
            throw new IllegalArgumentException("Mức độ phải là một trong: " + String.join(", ", BaoHongDanhMuc.MUC_DO) + ".");
        if (mt.isEmpty()) throw new IllegalArgumentException("Hãy mô tả sự cố.");
        if (mt.length() > MO_TA_TOI_DA) throw new IllegalArgumentException("Mô tả không được quá " + MO_TA_TOI_DA + " ký tự.");
        if (sv.isEmpty()) {
            if (nv.isEmpty()) throw new IllegalArgumentException("Sự cố do nhân viên phát hiện: hãy nhập tên nhân viên ghi nhận.");
            if (nv.length() > 100) throw new IllegalArgumentException("Tên nhân viên không được quá 100 ký tự.");
        } else {
            if (sv.length() > 20) throw new IllegalArgumentException("Mã sinh viên không hợp lệ.");
            nv = "";
        }
        return new TaoMoi(phong, sv.isEmpty() ? null : sv, nv.isEmpty() ? null : nv, loaiSuCo, mt, mucDo);
    }

    /**
     * Kiểm tra việc đổi trạng thái.
     *
     * @param hienTai   báo hỏng hiện tại (đã đọc từ CSDL)
     * @param moi       trạng thái muốn chuyển tới
     * @param nhanVien  nhân viên xử lý; bỏ trống thì dùng nhân viên đang phụ trách (nếu có)
     * @param ghiChuKetQua kết quả xử lý hoặc lý do từ chối (bắt buộc khi kết thúc); với Đang xử lý là ghi chú tùy chọn
     * @param thoiDiem  thời điểm xảy ra; null nghĩa là bây giờ
     * @param buocTruoc thời gian của bước lịch sử gần nhất (không được lùi về trước)
     * @param bayGio    giờ hiện tại, truyền vào để dễ kiểm thử
     */
    public static CapNhat kiemTraCapNhat(BaoHong hienTai, String moi, String nhanVien, String ghiChuKetQua,
                                         LocalDateTime thoiDiem, LocalDateTime buocTruoc, LocalDateTime bayGio) {
        if (!BaoHongDanhMuc.thuoc(BaoHongDanhMuc.TRANG_THAI, moi))
            throw new IllegalArgumentException("Trạng thái phải là một trong: " + String.join(", ", BaoHongDanhMuc.TRANG_THAI) + ".");
        if (!BaoHongDanhMuc.chuyenHopLe(hienTai.trangThai(), moi)) {
            var tiep = BaoHongDanhMuc.trangThaiTiepTheo(hienTai.trangThai());
            if (tiep.isEmpty())
                throw new IllegalArgumentException("Báo hỏng đã ở trạng thái \"" + hienTai.trangThai() + "\" nên không thể đổi trạng thái nữa.");
            throw new IllegalArgumentException("Không thể chuyển từ \"" + hienTai.trangThai() + "\" sang \"" + moi
                    + "\". Chỉ được chuyển sang: " + String.join(", ", tiep) + ".");
        }
        LocalDateTime luc = thoiDiem == null ? bayGio : thoiDiem;
        if (luc.isBefore(hienTai.ngayBao()))
            throw new IllegalArgumentException("Ngày xử lý không được trước ngày báo (" + GIO.format(hienTai.ngayBao()) + ").");
        if (buocTruoc != null && luc.isBefore(buocTruoc))
            throw new IllegalArgumentException("Thời điểm không được trước bước xử lý trước đó (" + GIO.format(buocTruoc) + ").");
        if (luc.isAfter(bayGio)) throw new IllegalArgumentException("Thời điểm xử lý không được ở tương lai.");

        String nv = cat(nhanVien);
        if (nv.isEmpty()) nv = cat(hienTai.nhanVienXuLy());
        if (nv.isEmpty()) throw new IllegalArgumentException("Hãy nhập nhân viên xử lý.");
        if (nv.length() > 100) throw new IllegalArgumentException("Tên nhân viên không được quá 100 ký tự.");

        String gc = cat(ghiChuKetQua);
        boolean ketThuc = BaoHongDanhMuc.daKetThuc(moi);
        if (ketThuc && gc.isEmpty())
            throw new IllegalArgumentException(BaoHongDanhMuc.HOAN_THANH.equals(moi)
                    ? "Hãy nhập kết quả xử lý." : "Hãy nhập lý do từ chối.");
        if (gc.length() > KET_QUA_TOI_DA) throw new IllegalArgumentException("Nội dung không được quá " + KET_QUA_TOI_DA + " ký tự.");

        return new CapNhat(moi, nv, ketThuc ? luc : null, ketThuc ? gc : null, gc.isEmpty() ? null : gc, luc);
    }

    private static String cat(String s) { return s == null ? "" : s.trim(); }
}
