package ktx.model;

import java.time.LocalDateTime;

/** Một bước trong lịch sử xử lý. trangThaiCu == null là dòng ghi nhận lúc tạo báo hỏng. */
public record LichSuBaoHong(long maLichSu, long maBaoHong, String trangThaiCu, String trangThaiMoi,
                            String nhanVien, String ghiChu, LocalDateTime thoiGian) {}
