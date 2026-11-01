package ktx.model;

import java.math.BigDecimal;

public record ThongKePhiXe(
    String maSV,
    String lop,
    long soXeThang,
    BigDecimal tienVeThang,
    long soLuot,
    long soLuotMienPhi,
    long soLuotTinhPhi,
    BigDecimal phiPhatSinh,
    BigDecimal tongPhaiTra
) {}
