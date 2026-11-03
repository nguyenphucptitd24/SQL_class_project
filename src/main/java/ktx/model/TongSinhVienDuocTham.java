package ktx.model;

/** Số lượt thăm và số khách khác nhau của một sinh viên trong kỳ (sinh viên không có khách vẫn xuất hiện, bằng 0). */
public record TongSinhVienDuocTham(SinhVien sinhVien, int soLuot, int soKhach) {}
