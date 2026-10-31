package ktx.service;

import ktx.model.DichVu;
import ktx.repository.DichVuRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DichVuService {
    private final DichVuRepository repository;

    public DichVuService() {
        this.repository = new DichVuRepository();
    }

    public List<DichVu> getAllDichVu() throws SQLException {
        return repository.findAll();
    }

    public void addDichVu(DichVu dv) throws Exception {
        validateDichVu(dv);

        try {
            boolean success = repository.insert(dv);
            if (!success) {
                throw new Exception("Không thể thêm dịch vụ. Vui lòng thử lại.");
            }
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new Exception("Thêm thất bại: Mã dịch vụ '" + dv.getMaDV() + "' đã tồn tại!");
            }
            throw e;
        }
    }

    public void updateDichVu(DichVu dv) throws Exception {
        validateDichVu(dv);

        boolean success = repository.update(dv);
        if (!success) {
            throw new Exception("Cập nhật thất bại: Không tìm thấy mã dịch vụ '" + dv.getMaDV() + "'.");
        }
    }

    public void deleteDichVu(String maDV) throws Exception {
        if (maDV == null || maDV.trim().isEmpty()) {
            throw new Exception("Mã dịch vụ cần xóa không được để trống.");
        }

        try {
            boolean success = repository.delete(maDV);
            if (!success) {
                throw new Exception("Xóa thất bại: Không tìm thấy mã dịch vụ '" + maDV + "'.");
            }
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new Exception("Không thể xóa: Dịch vụ này đang được sử dụng trong lịch sử của sinh viên.");
            }
            throw e;
        }
    }

    public void ghiNhanSuDung(String maSV, String maDV, int soLuong, LocalDate ngaySuDung) throws Exception {
        validateGhiNhanSuDung(maSV, maDV, soLuong, ngaySuDung);

        if (!repository.ghiNhanSuDung(maSV, maDV, soLuong, ngaySuDung)) {
            throw new Exception("Ghi nhận sử dụng dịch vụ thất bại.");
        }
    }

    public BigDecimal tinhTienMotLan(String maDV, int soLuong) throws SQLException {
        DichVu dv = getDichVuByMa(maDV);
        if (dv == null) {
            throw new IllegalArgumentException("Không tìm thấy dịch vụ với mã: " + maDV);
        }
        return dv.getThanhTien(soLuong);
    }

    public BigDecimal getTongTienTheoThang(int year, int month) throws SQLException {
        return repository.getTongTienTheoThang(year, month);
    }

    public BigDecimal getTongTienTheoSinhVienVaThang(String maSV, int year, int month) throws SQLException {
        if (maSV == null || maSV.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên không được để trống.");
        }
        return repository.getTongTienTheoSinhVienVaThang(maSV, year, month);
    }

    public List<String> getDanhSachDoanhThuTheoThang(int year, int month) throws SQLException {
        return repository.getDanhSachDichVuTheoThang(year, month);
    }

    public DichVu getDichVuByMa(String maDV) throws SQLException {
        List<DichVu> list = repository.findAll();
        for (DichVu dv : list) {
            if (dv.getMaDV().equalsIgnoreCase(maDV)) {
                return dv;
            }
        }
        return null;
    }

    private void validateDichVu(DichVu dv) throws Exception {
        if (dv == null) {
            throw new Exception("Dữ liệu dịch vụ không hợp lệ.");
        }
        if (dv.getMaDV() == null || dv.getMaDV().trim().isEmpty()) {
            throw new Exception("Mã dịch vụ không được để trống.");
        }
        if (dv.getMaDV().length() > 20) {
            throw new Exception("Mã dịch vụ không được vượt quá 20 ký tự.");
        }
        if (dv.getTenDV() == null || dv.getTenDV().trim().isEmpty()) {
            throw new Exception("Tên dịch vụ không được để trống.");
        }
        if (dv.getDonGia() == null) {
            throw new Exception("Đơn giá không hợp lệ.");
        }
        if (dv.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Đơn giá không được là số âm.");
        }
    }

    private void validateGhiNhanSuDung(String maSV, String maDV, int soLuong, LocalDate ngaySuDung) throws Exception {
        if (maSV == null || maSV.isBlank()) {
            throw new Exception("Mã sinh viên không được để trống.");
        }
        if (maDV == null || maDV.isBlank()) {
            throw new Exception("Mã dịch vụ không được để trống.");
        }
        if (soLuong <= 0) {
            throw new Exception("Số lượng phải lớn hơn 0.");
        }
        if (ngaySuDung == null) {
            throw new Exception("Ngày sử dụng không hợp lệ.");
        }

        if (getDichVuByMa(maDV) == null) {
            throw new Exception("Dịch vụ không tồn tại trong hệ thống.");
        }
    }
}
