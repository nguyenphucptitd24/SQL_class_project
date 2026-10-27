package ktx.ui;

import ktx.model.DichVu;
import ktx.service.DichVuService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class DichVuPanel extends JPanel {

    private final DichVuService dichVuService;
    
    // Các thành phần giao diện
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMaDV, txtTenDV, txtDonGia, txtMoTa;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;

    public DichVuPanel() {
        this.dichVuService = new DichVuService();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. Khu vực nhập liệu (Form) ---
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Dịch vụ"));

        formPanel.add(new JLabel("Mã Dịch Vụ:"));
        txtMaDV = new JTextField();
        formPanel.add(txtMaDV);

        formPanel.add(new JLabel("Tên Dịch Vụ:"));
        txtTenDV = new JTextField();
        formPanel.add(txtTenDV);

        formPanel.add(new JLabel("Đơn Giá (VNĐ):"));
        txtDonGia = new JTextField();
        formPanel.add(txtDonGia);

        formPanel.add(new JLabel("Mô Tả:"));
        txtMoTa = new JTextField();
        formPanel.add(txtMoTa);

        add(formPanel, BorderLayout.NORTH);

        // --- 2. Khu vực hiển thị bảng (Table) ---
        tableModel = new DefaultTableModel(new String[]{"Mã DV", "Tên Dịch Vụ", "Đơn Giá", "Mô Tả"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Khóa không cho sửa trực tiếp trên bảng
            }
        };
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Khu vực nút chức năng (Buttons) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm Mới");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnLamMoi);

        add(buttonPanel, BorderLayout.SOUTH);

        // --- 4. Gắn sự kiện (Event Listeners) ---
        btnLamMoi.addActionListener(e -> clearForm());
        btnThem.addActionListener(e -> addDichVu());
        btnSua.addActionListener(e -> updateDichVu());
        btnXoa.addActionListener(e -> deleteDichVu());
    }

    // --- CÁC HÀM XỬ LÝ LOGIC ---

    private void loadData() {
        try {
            tableModel.setRowCount(0); // Xóa dữ liệu cũ trên bảng
            List<DichVu> list = dichVuService.getAllDichVu();
            for (DichVu dv : list) {
                tableModel.addRow(new Object[]{
                    dv.getMaDV(), 
                    dv.getTenDV(), 
                    dv.getDonGia(), 
                    dv.getMoTa()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            txtMaDV.setText(tableModel.getValueAt(row, 0).toString());
            txtMaDV.setEditable(false); // Không cho sửa Mã DV khi đang chọn để update
            txtTenDV.setText(tableModel.getValueAt(row, 1).toString());
            txtDonGia.setText(tableModel.getValueAt(row, 2).toString());
            txtMoTa.setText(tableModel.getValueAt(row, 3) != null ? tableModel.getValueAt(row, 3).toString() : "");
        }
    }

    private void clearForm() {
        txtMaDV.setText("");
        txtMaDV.setEditable(true); // Mở lại ô Mã DV
        txtTenDV.setText("");
        txtDonGia.setText("");
        txtMoTa.setText("");
        table.clearSelection();
    }

    private DichVu getDichVuFromForm() throws Exception {
        String maDV = txtMaDV.getText().trim();
        String tenDV = txtTenDV.getText().trim();
        String donGiaStr = txtDonGia.getText().trim();
        String moTa = txtMoTa.getText().trim();

        if (donGiaStr.isEmpty()) throw new Exception("Vui lòng nhập đơn giá!");
        
        BigDecimal donGia;
        try {
            donGia = new BigDecimal(donGiaStr);
        } catch (NumberFormatException ex) {
            throw new Exception("Đơn giá phải là số hợp lệ!");
        }

        return new DichVu(maDV, tenDV, donGia, moTa);
    }

    private void addDichVu() {
        try {
            DichVu dv = getDichVuFromForm();
            dichVuService.addDichVu(dv);
            JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công!");
            clearForm();
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateDichVu() {
        try {
            DichVu dv = getDichVuFromForm();
            dichVuService.updateDichVu(dv);
            JOptionPane.showMessageDialog(this, "Cập nhật dịch vụ thành công!");
            clearForm();
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteDichVu() {
        String maDV = txtMaDV.getText().trim();
        if (maDV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dịch vụ để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa dịch vụ mã '" + maDV + "'?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dichVuService.deleteDichVu(maDV);
                JOptionPane.showMessageDialog(this, "Xóa dịch vụ thành công!");
                clearForm();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}