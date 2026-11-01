package ktx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SuKienXe(
    long maSuKien,
    long maLuot,
    String maXe,
    String bienSo,
    String maSV,
    String lop,
    String loaiSuKien,
    LocalDateTime thoiGian,
    boolean mienPhi,
    BigDecimal soTienPhatSinh
) {}
