package ktx.model;

import java.time.LocalDateTime;

/**
 * Điều kiện lọc danh sách báo hỏng; thành phần nào null/false thì bỏ qua.
 * tu/den là khoảng ngày báo, nửa mở [tu, den). chuaKetThuc = Mới báo + Đang xử lý.
 * uuTienMucDo = xếp mức độ nặng lên trước (Khẩn cấp, Cao, Trung bình, Thấp), rồi theo ngày báo.
 */
public record BoLocBaoHong(String soPhong, String maSV, String trangThai, boolean chuaKetThuc,
                           String loaiSuCo, String mucDo, LocalDateTime tu, LocalDateTime den,
                           boolean uuTienMucDo) {
    public static BoLocBaoHong tatCa() { return new BoLocBaoHong(null, null, null, false, null, null, null, null, false); }
}
