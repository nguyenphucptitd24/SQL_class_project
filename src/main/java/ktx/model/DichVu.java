package ktx.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Đại diện cho dữ liệu trong bảng DichVu.
 * Sử dụng BigDecimal cho đơn giá để đảm bảo độ chính xác tài chính.
 */
public class DichVu {
    private String maDV;
    private String tenDV;
    private BigDecimal donGia;
    private String moTa;

    public DichVu() {
    }

    public DichVu(String maDV, String tenDV, BigDecimal donGia, String moTa) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.donGia = donGia;
        this.moTa = moTa;
    }

    public String getMaDV() {
        return maDV;
    }

    public void setMaDV(String maDV) {
        this.maDV = maDV;
    }

    public String getTenDV() {
        return tenDV;
    }

    public void setTenDV(String tenDV) {
        this.tenDV = tenDV;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DichVu dichVu = (DichVu) o;
        return Objects.equals(maDV, dichVu.maDV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDV);
    }

    @Override
    public String toString() {
        return tenDV; // Hiển thị tên dịch vụ khi đưa vào JComboBox của Swing
    }
}