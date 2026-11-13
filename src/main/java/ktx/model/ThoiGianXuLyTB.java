package ktx.model;

/** Thời gian xử lý trung bình (giờ) của các báo hỏng đã hoàn thành trong một nhóm. */
public record ThoiGianXuLyTB(String nhom, int soBaoHong, double gioTrungBinh) {}
