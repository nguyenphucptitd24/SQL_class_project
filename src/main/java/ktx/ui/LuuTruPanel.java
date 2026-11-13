package ktx.ui;

import ktx.model.ThuePhong;
import ktx.repository.ThuePhongRepository;
import ktx.service.ThuePhongService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LuuTruPanel extends JPanel {
    private final ThuePhongRepository repository = new ThuePhongRepository();
    private final ThuePhongService service = new ThuePhongService();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Ma thue", "Ma SV", "So phong", "Bat dau", "Ket thuc",
                    "Ngay ky", "Het han", "Tien coc", "Trang thai"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public LuuTruPanel() {
        setLayout(new BorderLayout(8, 8));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reload = new JButton("Tai lai");
        JButton add = new JButton("Tao hop dong");
        JButton edit = new JButton("Cap nhat hop dong");
        JButton end = new JButton("Ket thuc hop dong");
        actions.add(reload);
        actions.add(add);
        actions.add(edit);
        actions.add(end);
        add(actions, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        reload.addActionListener(event -> refreshData());
        add.addActionListener(event -> addContract());
        edit.addActionListener(event -> editContract());
        end.addActionListener(event -> endContract());
        refreshData();
    }

    private void refreshData() {
        try {
            List<ThuePhong> contracts = repository.findAll();
            model.setRowCount(0);
            for (ThuePhong contract : contracts) {
                model.addRow(new Object[]{
                        contract.maThue(), contract.maSV(), contract.soPhong(),
                        contract.ngayBatDau(), contract.ngayKetThuc(), contract.ngayKyHopDong(),
                        contract.ngayHetHanHopDong(), contract.tienCoc(), contract.trangThai()
                });
            }
        } catch (SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void addContract() {
        try {
            String maSV = ask("Ma sinh vien:");
            if (maSV == null) return;
            String soPhong = ask("So phong:");
            if (soPhong == null) return;
            LocalDate ngayBatDau = askDate("Ngay bat dau (yyyy-MM-dd):");
            if (ngayBatDau == null) return;
            LocalDate ngayKy = askDate("Ngay ky hop dong (yyyy-MM-dd):");
            if (ngayKy == null) return;
            String ngayHetHanInput = ask("Ngay het han (yyyy-MM-dd, bo trong neu khong co):");
            if (ngayHetHanInput == null) return;
            LocalDate ngayHetHan = parseOptionalDate(ngayHetHanInput);
            BigDecimal tienCoc = askAmount("Tien coc:");
            if (tienCoc == null) return;

            long maThue = service.themHopDong(maSV, soPhong, ngayBatDau, ngayKy, ngayHetHan, tienCoc);
            showInfo("Da tao hop dong " + maThue + ".");
            refreshData();
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void editContract() {
        ThuePhong contract = getSelectedContract();
        if (contract == null) return;
        try {
            LocalDate ngayKy = askDate("Ngay ky (yyyy-MM-dd):");
            if (ngayKy == null) return;
            String ngayHetHanInput = ask("Ngay het han (yyyy-MM-dd, bo trong neu khong co):");
            if (ngayHetHanInput == null) return;
            LocalDate ngayHetHan = parseOptionalDate(ngayHetHanInput);
            BigDecimal tienCoc = askAmount("Tien coc:");
            if (tienCoc == null) return;

            service.capNhatHopDong(contract.maThue(), ngayKy, ngayHetHan, tienCoc);
            refreshData();
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void endContract() {
        ThuePhong contract = getSelectedContract();
        if (contract == null) return;
        try {
            LocalDate ngayKetThuc = askDate("Ngay ket thuc (yyyy-MM-dd):");
            if (ngayKetThuc == null) return;
            service.ketThucHopDong(contract.maThue(), ngayKetThuc);
            refreshData();
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private ThuePhong getSelectedContract() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            showError("Hay chon hop dong.");
            return null;
        }
        try {
            return repository.findById(((Number) model.getValueAt(selected, 0)).longValue());
        } catch (SQLException exception) {
            showError(exception.getMessage());
            return null;
        }
    }

    private String ask(String prompt) {
        return JOptionPane.showInputDialog(this, prompt);
    }

    private LocalDate askDate(String prompt) {
        String value = ask(prompt);
        return value == null ? null : LocalDate.parse(value.trim());
    }

    private LocalDate parseOptionalDate(String value) {
        return value.isBlank() ? null : LocalDate.parse(value.trim());
    }

    private BigDecimal askAmount(String prompt) {
        String value = ask(prompt);
        return value == null ? null : new BigDecimal(value.trim());
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Loi", JOptionPane.ERROR_MESSAGE);
    }
}
