package ktx.ui;

import ktx.model.DichVu;
import ktx.service.DichVuService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DichVuPanel extends JPanel {
    private final DichVuService dichVuService = new DichVuService();

    private final DefaultTableModel serviceTableModel = new DefaultTableModel(
            new String[]{"Mã DV", "Tên Dịch Vụ", "Đơn Giá", "Mô Tả"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel billTableModel = new DefaultTableModel(
            new String[]{"Dịch vụ", "Tổng tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable serviceTable = new JTable(serviceTableModel);
    private final JTable billTable = new JTable(billTableModel);

    private final JTextField txtMaDV = new JTextField();
    private final JTextField txtTenDV = new JTextField();
    private final JTextField txtDonGia = new JTextField();
    private final JTextField txtMoTa = new JTextField();
    private final JTextField txtMaSV = new JTextField();
    private final JComboBox<DichVu> cbDichVu = new JComboBox<>();
    private final JTextField txtSoLuong = new JTextField("1");
    private final JTextField txtNgaySuDung = new JTextField(LocalDate.now().toString());
    private final JTextField txtNam = new JTextField(String.valueOf(LocalDate.now().getYear()));
    private final JTextField txtThang = new JTextField(String.valueOf(LocalDate.now().getMonthValue()));

    public DichVuPanel() {
        initComponents();
        loadDichVu();
        loadDoanhThuThang();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JPanel serviceForm = new JPanel(new GridLayout(4, 2, 10, 10));
        serviceForm.setBorder(BorderFactory.createTitledBorder("Thông tin dịch vụ"));

        serviceForm.add(new JLabel("Mã dịch vụ:"));
        serviceForm.add(txtMaDV);
        serviceForm.add(new JLabel("Tên dịch vụ:"));
        serviceForm.add(txtTenDV);
        serviceForm.add(new JLabel("Đơn giá (VNĐ):"));
        serviceForm.add(txtDonGia);
        serviceForm.add(new JLabel("Mô tả:"));
        serviceForm.add(txtMoTa);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnLamMoi = new JButton("Làm mới");

        buttons.add(btnThem);
        buttons.add(btnSua);
        buttons.add(btnXoa);
        buttons.add(btnLamMoi);

        topPanel.add(serviceForm, BorderLayout.CENTER);
        topPanel.add(buttons, BorderLayout.SOUTH);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel usagePanel = new JPanel(new GridLayout(4, 2, 10, 10));
        usagePanel.setBorder(BorderFactory.createTitledBorder("Ghi nhận sử dụng dịch vụ"));

        usagePanel.add(new JLabel("Mã sinh viên:"));
        usagePanel.add(txtMaSV);
        usagePanel.add(new JLabel("Dịch vụ:"));
        usagePanel.add(cbDichVu);
        usagePanel.add(new JLabel("Số lượng:"));
        usagePanel.add(txtSoLuong);
        usagePanel.add(new JLabel("Ngày sử dụng:"));
        usagePanel.add(txtNgaySuDung);

        JButton btnGhiNhan = new JButton("Ghi nhận");
        JPanel usageButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        usageButton.add(btnGhiNhan);

        infoPanel.add(usagePanel);
        infoPanel.add(usageButton);

        JPanel monthPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        monthPanel.setBorder(BorderFactory.createTitledBorder("Xem hóa đơn / doanh thu theo tháng"));
        monthPanel.add(new JLabel("Năm:"));
        monthPanel.add(txtNam);
        monthPanel.add(new JLabel("Tháng:"));
        monthPanel.add(txtThang);

        JButton btnXem = new JButton("Xem doanh thu");
        JButton btnHoaDon = new JButton("Xem hóa đơn SV");
        JPanel viewButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        viewButtons.add(btnXem);
        viewButtons.add(btnHoaDon);

        JPanel listPanel = new JPanel(new BorderLayout(8, 8));
        listPanel.add(monthPanel, BorderLayout.NORTH);
        listPanel.add(new JScrollPane(billTable), BorderLayout.CENTER);
        listPanel.add(viewButtons, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel serviceTablePanel = new JPanel(new BorderLayout(8, 8));
        serviceTablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách dịch vụ"));
        serviceTablePanel.add(new JScrollPane(serviceTable), BorderLayout.CENTER);

        splitPane.setTopComponent(serviceTablePanel);
        splitPane.setBottomComponent(listPanel);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        serviceTable.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
        btnLamMoi.addActionListener(e -> clearForm());
        btnThem.addActionListener(e -> addDichVu());
        btnSua.addActionListener(e -> updateDichVu());
        btnXoa.addActionListener(e -> deleteDichVu());
        btnGhiNhan.addActionListener(e -> ghiNhanSuDung());
        btnXem.addActionListener(e -> loadDoanhThuThang());
        btnHoaDon.addActionListener(e -> loadHoaDonSinhVien());
    }

    private void loadDichVu() {
        try {
            serviceTableModel.setRowCount(0);
            cbDichVu.removeAllItems();
            List<DichVu> list = dichVuService.getAllDichVu();
            for (DichVu dv : list) {
                serviceTableModel.addRow(new Object[]{dv.getMaDV(), dv.getTenDV(), dv.getDonGia(), dv.getMoTa()});
                cbDichVu.addItem(dv);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách dịch vụ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFormFromTable() {
        int row = serviceTable.getSelectedRow();
        if (row >= 0) {
            txtMaDV.setText(serviceTableModel.getValueAt(row, 0).toString());
            txtTenDV.setText(serviceTableModel.getValueAt(row, 1).toString());
            txtDonGia.setText(serviceTableModel.getValueAt(row, 2).toString());
            Object moTa = serviceTableModel.getValueAt(row, 3);
            txtMoTa.setText(moTa == null ? "" : moTa.toString());
        }
    }

    private void clearForm() {
        txtMaDV.setText("");
        txtTenDV.setText("");
        txtDonGia.setText("");
        txtMoTa.setText("");
        txtMaSV.setText("");
        txtSoLuong.setText("1");
        txtNgaySuDung.setText(LocalDate.now().toString());
        serviceTable.clearSelection();
    }

    private DichVu getDichVuFromForm() throws Exception {
        String maDV = txtMaDV.getText().trim();
        String tenDV = txtTenDV.getText().trim();
        String donGiaText = txtDonGia.getText().trim();
        if (maDV.isEmpty() || tenDV.isEmpty() || donGiaText.isEmpty()) {
            throw new Exception("Vui lòng nhập đầy đủ thông tin dịch vụ.");
        }
        BigDecimal donGia;
        try {
            donGia = new BigDecimal(donGiaText);
        } catch (NumberFormatException ex) {
            throw new Exception("Đơn giá phải là số hợp lệ.");
        }
        return new DichVu(maDV, tenDV, donGia, txtMoTa.getText().trim());
    }

    private void addDichVu() {
        try {
            DichVu dv = getDichVuFromForm();
            dichVuService.addDichVu(dv);
            JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công!");
            clearForm();
            loadDichVu();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDichVu() {
        try {
            DichVu dv = getDichVuFromForm();
            dichVuService.updateDichVu(dv);
            JOptionPane.showMessageDialog(this, "Cập nhật dịch vụ thành công!");
            clearForm();
            loadDichVu();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDichVu() {
        String maDV = txtMaDV.getText().trim();
        if (maDV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ cần xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa dịch vụ " + maDV + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            try {
                dichVuService.deleteDichVu(maDV);
                JOptionPane.showMessageDialog(this, "Xóa dịch vụ thành công!");
                clearForm();
                loadDichVu();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ghiNhanSuDung() {
        try {
            String maSV = txtMaSV.getText().trim();
            DichVu dv = (DichVu) cbDichVu.getSelectedItem();
            int soLuong = Integer.parseInt(txtSoLuong.getText().trim());
            LocalDate ngaySuDung = LocalDate.parse(txtNgaySuDung.getText().trim());

            if (dv == null) {
                throw new Exception("Vui lòng chọn dịch vụ.");
            }
            dichVuService.ghiNhanSuDung(maSV, dv.getMaDV(), soLuong, ngaySuDung);
            JOptionPane.showMessageDialog(this, "Ghi nhận sử dụng dịch vụ thành công!");
            loadDoanhThuThang();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDoanhThuThang() {
        try {
            int year = Integer.parseInt(txtNam.getText().trim());
            int month = Integer.parseInt(txtThang.getText().trim());
            billTableModel.setRowCount(0);
            List<String> list = dichVuService.getDanhSachDoanhThuTheoThang(year, month);
            for (String item : list) {
                billTableModel.addRow(new Object[]{item, ""});
            }
            BigDecimal tong = dichVuService.getTongTienTheoThang(year, month);
            billTableModel.addRow(new Object[]{"Tổng doanh thu", tong});
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải doanh thu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHoaDonSinhVien() {
        try {
            String maSV = txtMaSV.getText().trim();
            int year = Integer.parseInt(txtNam.getText().trim());
            int month = Integer.parseInt(txtThang.getText().trim());
            if (maSV.isEmpty()) {
                throw new Exception("Vui lòng nhập mã sinh viên để xem hóa đơn.");
            }
            BigDecimal tong = dichVuService.getTongTienTheoSinhVienVaThang(maSV, year, month);
            billTableModel.setRowCount(0);
            billTableModel.addRow(new Object[]{"Mã SV", maSV});
            billTableModel.addRow(new Object[]{"Tháng", year + "-" + month});
            billTableModel.addRow(new Object[]{"Tổng tiền dịch vụ", tong});
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
