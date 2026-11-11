package ktx.ui;

import ktx.model.HoaDonThang;
import ktx.service.HoaDonThangService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class HoaDonThangPanel extends JPanel {
    private final HoaDonThangService service = new HoaDonThangService();
    private final JTextField txtNam = new JTextField(String.valueOf(LocalDate.now().getYear()), 6);
    private final JTextField txtThang = new JTextField(String.valueOf(LocalDate.now().getMonthValue()), 4);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Mã hóa đơn", "Mã SV", "Họ tên", "Tháng", "Tiền phòng",
                    "Tiền dịch vụ", "Tổng tiền", "Trạng thái", "Ngày lập"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public HoaDonThangPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBorder(BorderFactory.createTitledBorder("Kỳ hóa đơn"));
        controls.add(new JLabel("Tháng:"));
        controls.add(txtThang);
        controls.add(new JLabel("Năm:"));
        controls.add(txtNam);

        JButton generateButton = new JButton("Lập hóa đơn tháng");
        JButton refreshButton = new JButton("Tải lại");
        JButton paidButton = new JButton("Đánh dấu đã thanh toán");
        controls.add(generateButton);
        controls.add(refreshButton);
        controls.add(paidButton);

        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        generateButton.addActionListener(event -> generateInvoices());
        refreshButton.addActionListener(event -> loadInvoices());
        paidButton.addActionListener(event -> markSelectedAsPaid());
        loadInvoices();
    }

    private void generateInvoices() {
        try {
            int year = Integer.parseInt(txtNam.getText().trim());
            int month = Integer.parseInt(txtThang.getText().trim());
            int confirmed = JOptionPane.showConfirmDialog(this,
                    "Lập hoặc cập nhật các hóa đơn chưa thanh toán cho tháng " + month + "/" + year + "?",
                    "Xác nhận lập hóa đơn", JOptionPane.YES_NO_OPTION);
            if (confirmed != JOptionPane.YES_OPTION) {
                return;
            }
            int created = service.lapHoaDon(year, month);
            JOptionPane.showMessageDialog(this, "Đã xử lý kỳ hóa đơn. Số hóa đơn mới: " + created + ".");
            loadInvoices();
        } catch (Exception exception) {
            showError("Không thể lập hóa đơn: " + exception.getMessage());
        }
    }

    private void loadInvoices() {
        try {
            int year = Integer.parseInt(txtNam.getText().trim());
            int month = Integer.parseInt(txtThang.getText().trim());
            List<HoaDonThang> invoices = service.getByPeriod(year, month);
            tableModel.setRowCount(0);
            for (HoaDonThang invoice : invoices) {
                tableModel.addRow(new Object[]{
                        invoice.maHoaDon(),
                        invoice.maSV(),
                        invoice.hoTen(),
                        String.format("%02d/%d", invoice.thang(), invoice.nam()),
                        invoice.tienPhong(),
                        invoice.tienDichVu(),
                        invoice.tongTien(),
                        invoice.trangThai().equals("DA_THANH_TOAN") ? "Đã thanh toán" : "Chưa thanh toán",
                        invoice.ngayLap()
                });
            }
        } catch (Exception exception) {
            showError("Không thể tải hóa đơn: " + exception.getMessage());
        }
    }

    private void markSelectedAsPaid() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showError("Vui lòng chọn hóa đơn cần cập nhật.");
            return;
        }
        long invoiceId = ((Number) tableModel.getValueAt(selectedRow, 0)).longValue();
        if (JOptionPane.showConfirmDialog(this,
                "Xác nhận hóa đơn " + invoiceId + " đã được thanh toán?",
                "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.danhDauDaThanhToan(invoiceId);
            loadInvoices();
        } catch (Exception exception) {
            showError("Không thể cập nhật trạng thái hóa đơn: " + exception.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
