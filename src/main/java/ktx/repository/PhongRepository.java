package ktx.repository;

import ktx.database.DatabaseConnection;
import ktx.model.Phong;
import java.sql.*;
import java.util.*;

public class PhongRepository {
    public List<Phong> findAll() throws SQLException {
        String sql = "SELECT p.SoPhong,p.LoaiPhong,p.DonGia,p.SoNguoiToiDa, " +
            "(SELECT COUNT(*) FROM ThuePhong t WHERE t.SoPhong=p.SoPhong AND t.NgayKetThuc IS NULL) AS DangO " +
            "FROM Phong p ORDER BY p.SoPhong";
        List<Phong> out=new ArrayList<>();
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while(r.next()) out.add(new Phong(r.getString("SoPhong"),r.getString("LoaiPhong"),r.getBigDecimal("DonGia"),r.getInt("SoNguoiToiDa"),r.getInt("DangO")));
        }
        return out;
    }
    public void insert(Phong p0) throws SQLException {
        String sql="INSERT INTO Phong(SoPhong,LoaiPhong,DonGia,SoNguoiToiDa) VALUES(?,?,?,?)";
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,p0.soPhong()); p.setString(2,p0.loaiPhong()); p.setBigDecimal(3,p0.donGia()); p.setInt(4,p0.soNguoiToiDa()); p.executeUpdate();
        }
    }
    public boolean update(Phong p0) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement("UPDATE Phong SET LoaiPhong=?,DonGia=?,SoNguoiToiDa=? WHERE SoPhong=?")) {
            p.setString(1,p0.loaiPhong()); p.setBigDecimal(2,p0.donGia()); p.setInt(3,p0.soNguoiToiDa()); p.setString(4,p0.soPhong()); return p.executeUpdate()>0;
        }
    }
}
