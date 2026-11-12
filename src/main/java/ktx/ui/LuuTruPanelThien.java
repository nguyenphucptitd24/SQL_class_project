
package ktx.ui;

import ktx.model.ThuePhong;
import ktx.repository.ThuePhongRepository;
import ktx.service.ThuePhongService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LuuTruPanel extends JPanel {

    private final ThuePhongRepository repository = new ThuePhongRepository();
    private final ThuePhongService service = new ThuePhongService();

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{
            "Ma hop dong",
            "Ma sinh vien",
            "So phong",
            "Ngay bat dau",
            "Ngay ky",
            "Ngay het han",
            "Ngay ket thuc",
            "Tien coc",
            "Trang thai"
        }, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    public LuuTruPanel() {
        setLayout(new BorderLayout(8, 8));

        // Tieu de
        JLabel title = new JLabel("QUAN LY HOP DONG LUU TRU");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        add(title, BorderLayout.NORTH);

        // Bang danh sach hop dong
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Cac nut chuc nang
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshButton = new JButton("Tai lai");
        JButton addButton = new JButton("Them hop dong");
        JButton editButton = new JButton("Sua hop dong");
        JButton endButton = new JButton("Ket thuc hop dong");

        actions.add(refreshButton);
        actions.add(addButton);
        actions.add(editButton);
        actions.add(endButton);

        add(actions, BorderLayout.SOUTH);

        // Xu ly su kien
        refreshButton.addActionListener(e -> refreshData());
        addButton.addActionListener(e -> addContract());
        editButton.addActionListener(e -> editContract());
        endButton.addActionListener(e -> endContract());

        refreshData();
    }

    // =====================================================
    // 1. HIEN THI DANH SACH HOP DONG
    // =====================================================

    private void refreshData() {
        try {
            List<ThuePhong> contracts = repository.findAll();

            tableModel.setRowCount(0);

            for (ThuePhong contract : contracts) {

                String status = contract.trangThai();

                tableModel.addRow(new Object[]{
                    contract.maThue(),
                    contract.maSV(),
                    contract.soPhong(),
                    contract.ngayBatDau(),
                    contract.ngayKyHopDong(),
                    contract.ngayHetHanHopDong(),
                    contract.ngayKetThuc() == null
                        ? ""
                        : contract.ngayKetThuc(),
                    contract.tienCoc(),
                    status
                });
            }

        } catch (Exception e) {
            showError("Khong tai duoc danh sach hop dong: " + e.getMessage());
        }
    }

    // =====================================================
    // 2. THEM HOP DONG
    // =====================================================

    private void addContract() {
        try {
            JTextField maSVField = new JTextField();
            JTextField soPhongField = new JTextField();
            JTextField ngayBatDauField = new JTextField(LocalDate.now().toString());
            JTextField ngayKyField = new JTextField(LocalDate.now().toString());
            JTextField ngayHetHanField = new JTextField(
                LocalDate.now().plusMonths(6).toString()
            );
            JTextField tienCocField = new JTextField("0");

            Object[] fields = {
                "Ma sinh vien:", maSVField,
                "So phong:", soPhongField,
                "Ngay bat dau (yyyy-MM-dd):", ngayBatDauField,
                "Ngay ky hop dong (yyyy-MM-dd):", ngayKyField,
                "Ngay het han (yyyy-MM-dd):", ngayHetHanField,
                "Tien coc:", tienCocField
            };

            int result = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Them hop dong luu tru",
                JOptionPane.OK_CANCEL_OPTION
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String maSV = maSVField.getText().trim();
            String soPhong = soPhongField.getText().trim();

            LocalDate ngayBatDau = LocalDate.parse(
                ngayBatDauField.getText().trim()
            );

            LocalDate ngayKy = LocalDate.parse(
                ngayKyField.getText().trim()
            );

            LocalDate ngayHetHan = LocalDate.parse(
                ngayHetHanField.getText().trim()
            );

            BigDecimal tienCoc = new BigDecimal(
                tienCocField.getText().trim()
            );

            long maThue = service.themHopDong(
                maSV,
                soPhong,
                ngayBatDau,
                ngayKy,
                ngayHetHan,
                tienCoc
            );

            showInfo("Them hop dong thanh cong. Ma hop dong: " + maThue);

            refreshData();

        } catch (Exception e) {
            showError("Khong them duoc hop dong: " + e.getMessage());
        }
    }

    // =====================================================
    // 3. SUA HOP DONG
    // =====================================================

    private void editContract() {
        ThuePhong selected = getSelectedContract();

        if (selected == null) {
            return;
        }

        if (selected.ngayKetThuc() != null) {
            showError("Khong the sua hop dong da ket thuc.");
            return;
        }

        try {
            JTextField maSVField = new JTextField(selected.maSV());
            JTextField soPhongField = new JTextField(selected.soPhong());

            JTextField ngayBatDauField = new JTextField(
                selected.ngayBatDau().toString()
            );

            JTextField ngayKyField = new JTextField(
                selected.ngayKyHopDong().toString()
            );

            JTextField ngayHetHanField = new JTextField(
                selected.ngayHetHanHopDong().toString()
            );

            JTextField tienCocField = new JTextField(
                selected.tienCoc().toString()
            );

            Object[] fields = {
                "Ma sinh vien:", maSVField,
                "So phong:", soPhongField,
                "Ngay bat dau (yyyy-MM-dd):", ngayBatDauField,
                "Ngay ky hop dong (yyyy-MM-dd):", ngayKyField,
                "Ngay het han (yyyy-MM-dd):", ngayHetHanField,
                "Tien coc:", tienCocField
            };

            int result = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Sua hop dong " + selected.maThue(),
                JOptionPane.OK_CANCEL_OPTION
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            service.capNhatHopDong(
                selected.maThue(),
                maSVField.getText().trim(),
                soPhongField.getText().trim(),
                LocalDate.parse(ngayBatDauField.getText().trim()),
                LocalDate.parse(ngayKyField.getText().trim()),
                LocalDate.parse(ngayHetHanField.getText().trim()),
                new BigDecimal(tienCocField.getText().trim())
            );

            showInfo("Cap nhat hop dong thanh cong.");

            refreshData();

        } catch (Exception e) {
            showError("Khong sua duoc hop dong: " + e.getMessage());
        }
    }

    // =====================================================
    // 4. KET THUC HOP DONG
    // =====================================================

    private void endContract() {
        ThuePhong selected = getSelectedContract();

        if (selected == null) {
            return;
        }

        if (selected.ngayKetThuc() != null) {
            showError("Hop dong nay da ket thuc.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Ban co chac chan muon ket thuc hop dong "
                + selected.maThue() + " khong?",
            "Xac nhan ket thuc hop dong",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String input = JOptionPane.showInputDialog(
                this,
                "Nhap ngay ket thuc (yyyy-MM-dd):",
                LocalDate.now().toString()
            );

            if (input == null) {
                return;
            }

            LocalDate ngayKetThuc = LocalDate.parse(input.trim());

            service.ketThucHopDong(
                selected.maThue(),
                ngayKetThuc
            );

            showInfo("Da ket thuc hop dong.");

            refreshData();

        } catch (Exception e) {
            showError("Khong the ket thuc hop dong: " + e.getMessage());
        }
    }

    // =====================================================
    // 5. LAY HOP DONG DANG DUOC CHON
    // =====================================================

    private ThuePhong getSelectedContract() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            showError("Hay chon mot hop dong trong danh sach.");
            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        Long maThue = Long.valueOf(
            tableModel.getValueAt(modelRow, 0).toString()
        );

        try {
            return repository.findById(maThue);
        } catch (Exception e) {
            showError("Khong tim thay hop dong: " + e.getMessage());
            return null;
        }
    }

    // =====================================================
    // 6. THONG BAO
    // =====================================================

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Thong bao",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Loi",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
