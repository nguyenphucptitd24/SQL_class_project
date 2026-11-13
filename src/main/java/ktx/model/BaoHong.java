package ktx.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Một báo hỏng. maSV == null nghĩa là sự cố do nhân viên phát hiện.
 * ngayXuLy là lúc kết thúc xử lý (hoàn thành hoặc từ chối), null khi chưa kết thúc.
 */
public record BaoHong(long maBaoHong, String soPhong, String maSV, String loaiSuCo, String moTa, String mucDo,
                      String trangThai, LocalDateTime ngayBao, LocalDateTime ngayXuLy,
                      String nhanVienXuLy, String ketQuaXuLy) {
    public boolean daKetThuc() { return BaoHongDanhMuc.daKetThuc(trangThai); }

    /** Sự cố mức cao/khẩn cấp mà chưa xử lý xong: cần chú ý. */
    public boolean canChuY() { return !daKetThuc() && BaoHongDanhMuc.laCanhBao(mucDo); }

    /** Thời gian từ lúc báo đến lúc kết thúc xử lý, null nếu chưa kết thúc. */
    public Duration thoiGianXuLy() { return ngayXuLy == null ? null : Duration.between(ngayBao, ngayXuLy); }
}
