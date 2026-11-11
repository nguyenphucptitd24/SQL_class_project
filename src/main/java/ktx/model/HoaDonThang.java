package ktx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HoaDonThang(
        long maHoaDon,
        String maSV,
        String hoTen,
        int thang,
        int nam,
        BigDecimal tienPhong,
        BigDecimal tienDichVu,
        BigDecimal tongTien,
        String trangThai,
        LocalDateTime ngayLap) {
}
