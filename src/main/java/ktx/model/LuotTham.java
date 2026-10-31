package ktx.model;

import java.time.LocalDateTime;

/**
 * Một lượt khách đến thăm một sinh viên.
 * thoiGianDi == null nghĩa là khách vẫn đang ở trong KTX.
 */
public record LuotTham(long maLuotTham, String soCMTKhach, String hoTenKhach, String maSV,
                       LocalDateTime thoiGianDen, LocalDateTime thoiGianDi) {
    public boolean dangO() { return thoiGianDi == null; }
}
