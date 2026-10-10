package ktx.repository;

import ktx.database.DatabaseConnection;
import ktx.model.SinhVien;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class SinhVienRepository {
    public List<SinhVien> findAll() throws SQLException {
        String sql = "SELECT MaSV,SoCMT,NgaySinh,Lop,QueQuan FROM SinhVien ORDER BY MaSV";
        List<SinhVien> out = new ArrayList<>();
        try (Connection c=DatabaseConnection.getConnection();
             PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while(r.next()) out.add(new SinhVien(r.getString("MaSV"),r.getString("SoCMT"),
                r.getDate("NgaySinh").toLocalDate(),r.getString("Lop"),r.getString("QueQuan")));
        }
        return out;
    }
    public List<SinhVien> search(String keyword) throws SQLException {
        String sql = "SELECT MaSV,SoCMT,NgaySinh,Lop,QueQuan FROM SinhVien WHERE MaSV LIKE ? OR Lop LIKE ? OR QueQuan LIKE ? ORDER BY MaSV";
        List<SinhVien> out = new ArrayList<>();
        try (Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            String k="%"+keyword.trim()+"%"; p.setString(1,k); p.setString(2,k); p.setString(3,k);
            try(ResultSet r=p.executeQuery()) { while(r.next()) out.add(new SinhVien(r.getString(1),r.getString(2),r.getDate(3).toLocalDate(),r.getString(4),r.getString(5))); }
        }
        return out;
    }
    public void insert(SinhVien s) throws SQLException {
        String sql="INSERT INTO SinhVien(MaSV,SoCMT,NgaySinh,Lop,QueQuan) VALUES(?,?,?,?,?)";
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            bind(p,s); p.executeUpdate();
        }
    }
    public boolean update(SinhVien s) throws SQLException {
        String sql="UPDATE SinhVien SET SoCMT=?,NgaySinh=?,Lop=?,QueQuan=? WHERE MaSV=?";
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,s.soCMT()); p.setDate(2,Date.valueOf(s.ngaySinh())); p.setString(3,s.lop()); p.setString(4,s.queQuan()); p.setString(5,s.maSV());
            return p.executeUpdate()>0;
        }
    }
    public boolean delete(String maSV) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement("DELETE FROM SinhVien WHERE MaSV=?")) {
            p.setString(1,maSV); return p.executeUpdate()>0;
        }
    }
    private void bind(PreparedStatement p,SinhVien s) throws SQLException {
        p.setString(1,s.maSV()); p.setString(2,s.soCMT()); p.setDate(3,Date.valueOf(s.ngaySinh())); p.setString(4,s.lop()); p.setString(5,s.queQuan());
    }
}
