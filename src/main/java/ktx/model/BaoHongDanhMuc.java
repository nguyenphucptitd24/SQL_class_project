package ktx.model;

import java.util.List;

/** Danh mục giá trị thống nhất của module báo hỏng (phải khớp các CHECK trong 05_bao_hong_phan_Nam.sql). */
public final class BaoHongDanhMuc {
    private BaoHongDanhMuc() {}

    public static final List<String> LOAI = List.of("Điện", "Nước", "Thiết bị", "Vệ sinh", "Khác");
    public static final List<String> MUC_DO = List.of("Thấp", "Trung bình", "Cao", "Khẩn cấp");

    public static final String MOI_BAO = "Mới báo";
    public static final String DANG_XU_LY = "Đang xử lý";
    public static final String HOAN_THANH = "Đã hoàn thành";
    public static final String TU_CHOI = "Từ chối";
    public static final List<String> TRANG_THAI = List.of(MOI_BAO, DANG_XU_LY, HOAN_THANH, TU_CHOI);

    /** Các trạng thái được phép chuyển sang từ trạng thái hiện tại (rỗng nếu đã kết thúc). */
    public static List<String> trangThaiTiepTheo(String hienTai) {
        if (MOI_BAO.equals(hienTai))    return List.of(DANG_XU_LY, TU_CHOI);
        if (DANG_XU_LY.equals(hienTai)) return List.of(HOAN_THANH, TU_CHOI);
        return List.of();
    }

    /** Giá trị có thuộc danh mục không; null được coi là không thuộc (List.of(...).contains(null) sẽ ném NullPointerException). */
    public static boolean thuoc(List<String> danhMuc, String giaTri) { return giaTri != null && danhMuc.contains(giaTri); }

    public static boolean chuyenHopLe(String cu, String moi) { return thuoc(trangThaiTiepTheo(cu), moi); }

    /** Đã hoàn thành hoặc từ chối: không đổi trạng thái được nữa. */
    public static boolean daKetThuc(String trangThai) { return HOAN_THANH.equals(trangThai) || TU_CHOI.equals(trangThai); }

    /** Mức độ cần cảnh báo ở giao diện. */
    public static boolean laCanhBao(String mucDo) { return "Cao".equals(mucDo) || "Khẩn cấp".equals(mucDo); }
}
