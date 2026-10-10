package ktx.service;

import ktx.model.SinhVien;
import ktx.repository.SinhVienRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SinhVienService {
    private final SinhVienRepository repo = new SinhVienRepository();
    public List<SinhVien> getAll() throws SQLException { return repo.findAll(); }
    public List<SinhVien> search(String key) throws SQLException {
        if(key==null || key.isBlank()) return getAll();
        return repo.search(key.trim());
    }
    public void add(SinhVien s) throws SQLException { validate(s); repo.insert(s); }
    public void update(SinhVien s) throws SQLException { validate(s); if(!repo.update(s)) throw new IllegalArgumentException("Khong tim thay sinh vien."); }
    public void delete(String id) throws SQLException {
        if(id==null||id.isBlank()) throw new IllegalArgumentException("Hay chon ma sinh vien.");
        if(!repo.delete(id.trim())) throw new IllegalArgumentException("Khong tim thay sinh vien.");
    }
    private void validate(SinhVien s) {
        if(s==null || blank(s.maSV()) || blank(s.soCMT()) || blank(s.lop()) || blank(s.queQuan()) || s.ngaySinh()==null)
            throw new IllegalArgumentException("Vui long nhap day du thong tin.");
        if(!s.ngaySinh().isBefore(LocalDate.now())) throw new IllegalArgumentException("Ngay sinh phai truoc ngay hien tai.");
    }
    private boolean blank(String s){ return s==null||s.isBlank(); }
}
