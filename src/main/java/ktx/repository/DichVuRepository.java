package ktx.repository;

import ktx.database.DatabaseConnection;
import ktx.model.DichVu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVuRepository {

    // Lấy toàn bộ danh sách dịch vụ
    public List<DichVu> findAll() throws SQLException {
        List<DichVu> list = new ArrayList<>();
        String sql = "SELECT MaDV, TenDV, DonGia, MoTa FROM DichVu";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                DichVu dv = new DichVu(
                    rs.getString("MaDV"),
                    rs.getString("TenDV"),
                    rs.getBigDecimal("DonGia"),
                    rs.getString("MoTa")
                );
                list.add(dv);
            }
        }
        return list;
    }

    // Thêm dịch vụ mới
    public boolean insert(DichVu dv) throws SQLException {
        String sql = "INSERT INTO DichVu (MaDV, TenDV, DonGia, MoTa) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dv.getMaDV());
            pstmt.setString(2, dv.getTenDV());
            pstmt.setBigDecimal(3, dv.getDonGia());
            pstmt.setString(4, dv.getMoTa());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    // Cập nhật dịch vụ đã tồn tại
    public boolean update(DichVu dv) throws SQLException {
        String sql = "UPDATE DichVu SET TenDV = ?, DonGia = ?, MoTa = ? WHERE MaDV = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dv.getTenDV());
            pstmt.setBigDecimal(2, dv.getDonGia());
            pstmt.setString(3, dv.getMoTa());
            pstmt.setString(4, dv.getMaDV()); // Khóa chính để ở cuối cùng cho mệnh đề WHERE
            
            return pstmt.executeUpdate() > 0;
        }
    }

    // Xóa dịch vụ theo mã
    public boolean delete(String maDV) throws SQLException {
        String sql = "DELETE FROM DichVu WHERE MaDV = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, maDV);
            
            return pstmt.executeUpdate() > 0;
        }
    }
}