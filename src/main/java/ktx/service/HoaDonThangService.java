package ktx.service;

import ktx.model.HoaDonThang;
import ktx.repository.HoaDonThangRepository;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;

public class HoaDonThangService {
    private final HoaDonThangRepository repository = new HoaDonThangRepository();

    public List<HoaDonThang> getByPeriod(int year, int month) throws SQLException {
        return repository.findByPeriod(validatePeriod(year, month));
    }

    public int lapHoaDon(int year, int month) throws SQLException {
        return repository.generateForPeriod(validatePeriod(year, month));
    }

    public void danhDauDaThanhToan(long maHoaDon) throws SQLException {
        if (maHoaDon <= 0) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }
        repository.markAsPaid(maHoaDon);
    }

    private YearMonth validatePeriod(int year, int month) {
        if (year <= 2000) {
            throw new IllegalArgumentException("Năm phải lớn hơn 2000.");
        }
        try {
            return YearMonth.of(year, month);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Tháng phải nằm trong khoảng từ 1 đến 12.", exception);
        }
    }
}
