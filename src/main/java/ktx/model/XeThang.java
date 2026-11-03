package ktx.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record XeThang(
    String maXe,
    String maSV,
    String lop,
    String bienSo,
    String loaiXe,
    LocalDate ngayDangKy,
    LocalDate ngayHetHan,
    BigDecimal donGiaThang,
    boolean dangHoatDong
) {}
