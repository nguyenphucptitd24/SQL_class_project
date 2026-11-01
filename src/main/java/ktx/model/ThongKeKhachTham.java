package ktx.model;

/** Một dòng thống kê: sinh viên, khách đến thăm sinh viên đó và số lần khách đến trong kỳ. */
public record ThongKeKhachTham(SinhVien sinhVien, Khach khach, int soLan) {}
