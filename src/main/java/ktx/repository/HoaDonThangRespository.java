    package ktx.repository;

    import ktx.database.DatabaseConnection;
    import ktx.model.HoaDonThang;

    import java.math.BigDecimal;
    import java.sql.Connection;
    import java.sql.Date;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.sql.Timestamp;
    import java.time.LocalDateTime;
    import java.time.YearMonth;
    import java.util.ArrayList;
    import java.util.LinkedHashSet;
    import java.util.List;
    import java.util.Set;

    public class HoaDonThangRepository {

        public List<HoaDonThang> findByPeriod(YearMonth period) throws SQLException {
            String sql = """
                    SELECT hd.MaHoaDon, hd.MaSV, sv.HoTen, hd.Thang, hd.Nam,
                        hd.TienPhong,
                        COALESCE(SUM(sd.SoLuong * sd.DonGiaApDung), 0) AS TienDichVu,
                        hd.TongTien, hd.TrangThai, hd.NgayLap
                    FROM HoaDon hd
                    JOIN SinhVien sv ON sv.MaSV = hd.MaSV
                    LEFT JOIN SuDungDichVu sd ON sd.MaHoaDon = hd.MaHoaDon
                    WHERE hd.Thang = ? AND hd.Nam = ?
                    GROUP BY hd.MaHoaDon, hd.MaSV, sv.HoTen, hd.Thang, hd.Nam,
                            hd.TienPhong, hd.TongTien, hd.TrangThai, hd.NgayLap
                    ORDER BY hd.MaSV
                    """;
            List<HoaDonThang> invoices = new ArrayList<>();

            try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, period.getMonthValue());
                statement.setInt(2, period.getYear());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        Timestamp createdAt = result.getTimestamp("NgayLap");
                        invoices.add(new HoaDonThang(
                                result.getLong("MaHoaDon"),
                                result.getString("MaSV"),
                                result.getString("HoTen"),
                                result.getInt("Thang"),
                                result.getInt("Nam"),
                                result.getBigDecimal("TienPhong"),
                                result.getBigDecimal("TienDichVu"),
                                result.getBigDecimal("TongTien"),
                                result.getString("TrangThai"),
                                createdAt == null ? null : createdAt.toLocalDateTime()));
                    }
                }
            }
            return invoices;
        }

        public int generateForPeriod(YearMonth period) throws SQLException {
            LocalDateTime start = period.atDay(1).atStartOfDay();
            LocalDateTime end = period.plusMonths(1).atDay(1).atStartOfDay();
            Set<String> studentIds = findEligibleStudents(start, end);
            int createdCount = 0;

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    for (String studentId : studentIds) {
                        InvoiceStatus existing = findInvoice(connection, studentId, period);
                        boolean hasUnbilledServices = hasUnbilledServices(connection, studentId, start, end);

                        if (existing != null && "DA_THANH_TOAN".equals(existing.status())) {
                            if (hasUnbilledServices) {
                                throw new SQLException("Sinh viên " + studentId
                                        + " đã thanh toán hóa đơn tháng này nhưng còn dịch vụ chưa được tính.");
                            }
                            continue;
                        }

                        long invoiceId;
                        if (existing == null) {
                            BigDecimal roomCharge = getRoomCharge(connection, studentId, start, end);
                            invoiceId = insertInvoice(connection, studentId, period, roomCharge);
                            createdCount++;
                        } else {
                            invoiceId = existing.id();
                        }

                        attachUnbilledServices(connection, invoiceId, studentId, start, end);
                        updateTotal(connection, invoiceId);
                    }
                    connection.commit();
                } catch (SQLException | RuntimeException exception) {
                    connection.rollback();
                    throw exception;
                } finally {
                    connection.setAutoCommit(true);
                }
            }
            return createdCount;
        }

        public void markAsPaid(long invoiceId) throws SQLException {
            String sql = "UPDATE HoaDon SET TrangThai = 'DA_THANH_TOAN' "
                    + "WHERE MaHoaDon = ? AND TrangThai = 'CHUA_THANH_TOAN'";
            try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, invoiceId);
                if (statement.executeUpdate() == 0) {
                    throw new SQLException("Hóa đơn không tồn tại hoặc đã được thanh toán.");
                }
            }
        }

        private Set<String> findEligibleStudents(LocalDateTime start, LocalDateTime end) throws SQLException {
            String sql = """
                    SELECT MaSV
                    FROM ThuePhong
                    WHERE NgayBatDau < ? AND COALESCE(NgayKetThuc, '9999-12-31') >= ?
                    UNION
                    SELECT MaSV
                    FROM SuDungDichVu
                    WHERE ThoiGianSuDung >= ? AND ThoiGianSuDung < ? AND MaHoaDon IS NULL
                    ORDER BY MaSV
                    """;
            Set<String> studentIds = new LinkedHashSet<>();
            try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDate(1, Date.valueOf(end.toLocalDate()));
                statement.setDate(2, Date.valueOf(start.toLocalDate()));
                statement.setTimestamp(3, Timestamp.valueOf(start));
                statement.setTimestamp(4, Timestamp.valueOf(end));
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        studentIds.add(result.getString("MaSV"));
                    }
                }
            }
            return studentIds;
        }

        private InvoiceStatus findInvoice(Connection connection, String studentId, YearMonth period)
                throws SQLException {
            String sql = "SELECT MaHoaDon, TrangThai FROM HoaDon "
                    + "WHERE MaSV = ? AND Thang = ? AND Nam = ? FOR UPDATE";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, studentId);
                statement.setInt(2, period.getMonthValue());
                statement.setInt(3, period.getYear());
                try (ResultSet result = statement.executeQuery()) {
                    return result.next()
                            ? new InvoiceStatus(result.getLong("MaHoaDon"), result.getString("TrangThai"))
                            : null;
                }
            }
        }

        private boolean hasUnbilledServices(Connection connection, String studentId,
                                            LocalDateTime start, LocalDateTime end) throws SQLException {
            String sql = "SELECT 1 FROM SuDungDichVu WHERE MaSV = ? AND MaHoaDon IS NULL "
                    + "AND ThoiGianSuDung >= ? AND ThoiGianSuDung < ? LIMIT 1";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, studentId);
                statement.setTimestamp(2, Timestamp.valueOf(start));
                statement.setTimestamp(3, Timestamp.valueOf(end));
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        }

        private BigDecimal getRoomCharge(Connection connection, String studentId,
                                        LocalDateTime start, LocalDateTime end) throws SQLException {
            String sql = """
                    SELECT COALESCE(SUM(p.DonGia), 0)
                    FROM ThuePhong tp
                    JOIN Phong p ON p.SoPhong = tp.SoPhong
                    WHERE tp.MaSV = ? AND tp.NgayBatDau < ?
                    AND COALESCE(tp.NgayKetThuc, '9999-12-31') >= ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, studentId);
                statement.setDate(2, Date.valueOf(end.toLocalDate()));
                statement.setDate(3, Date.valueOf(start.toLocalDate()));
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    return result.getBigDecimal(1);
                }
            }
        }

        private long insertInvoice(Connection connection, String studentId, YearMonth period,
                                BigDecimal roomCharge) throws SQLException {
            String sql = "INSERT INTO HoaDon (MaSV, Thang, Nam, TienPhong, TongTien, TrangThai) "
                    + "VALUES (?, ?, ?, ?, ?, 'CHUA_THANH_TOAN')";
            try (PreparedStatement statement = connection.prepareStatement(sql,
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, studentId);
                statement.setInt(2, period.getMonthValue());
                statement.setInt(3, period.getYear());
                statement.setBigDecimal(4, roomCharge);
                statement.setBigDecimal(5, roomCharge);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Không lấy được mã hóa đơn vừa tạo.");
                    }
                    return keys.getLong(1);
                }
            }
        }

        private void attachUnbilledServices(Connection connection, long invoiceId, String studentId,
                                            LocalDateTime start, LocalDateTime end) throws SQLException {
            String sql = "UPDATE SuDungDichVu SET MaHoaDon = ? WHERE MaSV = ? AND MaHoaDon IS NULL "
                    + "AND ThoiGianSuDung >= ? AND ThoiGianSuDung < ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, invoiceId);
                statement.setString(2, studentId);
                statement.setTimestamp(3, Timestamp.valueOf(start));
                statement.setTimestamp(4, Timestamp.valueOf(end));
                statement.executeUpdate();
            }
        }

        private void updateTotal(Connection connection, long invoiceId) throws SQLException {
            String sql = """
                    UPDATE HoaDon
                    SET TongTien = TienPhong + (
                        SELECT COALESCE(SUM(sd.SoLuong * sd.DonGiaApDung), 0)
                        FROM SuDungDichVu sd
                        WHERE sd.MaHoaDon = HoaDon.MaHoaDon
                    )
                    WHERE MaHoaDon = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, invoiceId);
                statement.executeUpdate();
            }
        }

        private record InvoiceStatus(long id, String status) {
        }
    }
