package ktx.model;

import java.time.LocalDate;

public record ThuePhong(
    Long maThue,
    String maSV,
    String soPhong,
    LocalDate ngayBatDau,
    LocalDate ngayKetThuc
) {}
