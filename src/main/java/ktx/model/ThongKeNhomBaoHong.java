package ktx.model;

/** Một dòng thống kê báo hỏng theo nhóm (loại, mức độ hoặc tháng). */
public record ThongKeNhomBaoHong(String nhom, int tong, int chuaHoanThanh, int daHoanThanh, int tuChoi) {}
