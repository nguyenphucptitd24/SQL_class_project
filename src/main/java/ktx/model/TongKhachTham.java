package ktx.model;

/** Tổng số lần một khách đến KTX trong kỳ, và số sinh viên khác nhau đã được khách thăm. */
public record TongKhachTham(Khach khach, int tongSoLan, int soSinhVienDuocTham) {}
