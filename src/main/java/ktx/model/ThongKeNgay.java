package ktx.model;

import java.time.LocalDate;

/** Số lượt khách thăm và số khách khác nhau trong một ngày. */
public record ThongKeNgay(LocalDate ngay, int soLuot, int soKhach) {}
