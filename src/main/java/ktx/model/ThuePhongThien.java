
package ktx.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ThuePhong(
    Long maThue,
    String maSV,
    String soPhong,
    LocalDate ngayBatDau,
    LocalDate ngayKetThuc,
    LocalDate ngayKyHopDong,
    LocalDate ngayHetHanHopDong,
    BigDecimal tienCoc
) {

    // Xac dinh trang thai hop dong
    public String trangThai() {
        if (ngayKetThuc != null) {
            return "Da ket thuc";
        }

        if (ngayHetHanHopDong != null
                && ngayHetHanHopDong.isBefore(LocalDate.now())) {
            return "Het han";
        }

        return "Dang hieu luc";
    }
}
