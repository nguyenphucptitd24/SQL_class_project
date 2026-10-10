package ktx.model;

import java.math.BigDecimal;

public record Phong(String soPhong, String loaiPhong, BigDecimal donGia,
                    int soNguoiToiDa, int soNguoiHienTai) {}
