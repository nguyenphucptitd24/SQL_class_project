package ktx.service;

import ktx.model.DichVu;
import ktx.repository.DichVuRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
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
            // SQLState bắt đầu bằng "23" thường là vi phạm ràng buộc toàn vẹn (Integrity Constraint Violation)
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

    // Hàm dùng chung để kiểm tra logic nghiệp vụ đầu vào
    private void validateDichVu(DichVu dv) throws Exception {
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
}