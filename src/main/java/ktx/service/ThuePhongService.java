package ktx.service;

import ktx.database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;

public class ThuePhongService {
    public void xepPhong(String maSV, String soPhong, LocalDate ngay) throws SQLException {
        if(blank(maSV)||blank(soPhong)||ngay==null) throw new IllegalArgumentException("Nhap du MaSV, SoPhong va ngay bat dau.");
        if(ngay.isAfter(LocalDate.now())) throw new IllegalArgumentException("Ngay bat dau khong duoc o tuong lai.");
        String lock="SELECT SoNguoiToiDa FROM Phong WHERE SoPhong=? FOR UPDATE";
        try(Connection c=DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int max;
                try(PreparedStatement p=c.prepareStatement(lock)) {
                    p.setString(1,soPhong.trim());
                    try(ResultSet r=p.executeQuery()){ if(!r.next()) throw new SQLException("Phong khong ton tai."); max=r.getInt(1); }
                }
                try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM SinhVien WHERE MaSV=?")) {
                    p.setString(1,maSV.trim()); try(ResultSet r=p.executeQuery()){r.next(); if(r.getInt(1)==0) throw new SQLException("Sinh vien khong ton tai.");}
                }
                try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM ThuePhong WHERE MaSV=? AND NgayKetThuc IS NULL")) {
                    p.setString(1,maSV.trim()); try(ResultSet r=p.executeQuery()){r.next(); if(r.getInt(1)>0) throw new SQLException("Sinh vien dang o mot phong; hay tra phong truoc.");}
                }
                try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM ThuePhong WHERE SoPhong=? AND NgayKetThuc IS NULL")) {
                    p.setString(1,soPhong.trim()); try(ResultSet r=p.executeQuery()){r.next(); if(r.getInt(1)>=max) throw new SQLException("Phong da du suc chua.");}
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO ThuePhong(MaSV,SoPhong,NgayBatDau) VALUES(?,?,?)")) {
                    p.setString(1,maSV.trim()); p.setString(2,soPhong.trim()); p.setDate(3,Date.valueOf(ngay)); p.executeUpdate();
                }
                c.commit();
            } catch(Exception e) {
                c.rollback();
                if(e instanceof SQLException se) throw se;
                if(e instanceof RuntimeException re) throw re;
                throw new SQLException(e);
            } finally { c.setAutoCommit(true); }
        }
    }
    public void traPhong(String maSV, LocalDate ngayTra) throws SQLException {
        if(blank(maSV)||ngayTra==null) throw new IllegalArgumentException("Nhap MaSV va ngay tra.");
        String sql="UPDATE ThuePhong SET NgayKetThuc=? WHERE MaSV=? AND NgayKetThuc IS NULL AND NgayBatDau<=?";
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setDate(1,Date.valueOf(ngayTra)); p.setString(2,maSV.trim()); p.setDate(3,Date.valueOf(ngayTra));
            if(p.executeUpdate()==0) throw new SQLException("Khong co hop dong dang o hop le; ngay tra phai >= ngay bat dau.");
        }
    }
    private boolean blank(String s){return s==null||s.isBlank();}
}
