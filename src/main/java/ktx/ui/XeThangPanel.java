package ktx.ui;

import ktx.model.SuKienXe;
import ktx.model.ThongKePhiXe;
import ktx.model.XeThang;
import ktx.repository.XeThangRepository;
import ktx.service.XeThangService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class XeThangPanel extends JPanel {
    private final XeThangRepository repository = new XeThangRepository();
    private final XeThangService service = new XeThangService();

    private final DefaultTableModel xeModel = model(
        "Ma xe", "Ma SV", "Lop", "Bien so", "Loai xe", "Ngay dang ky", "Ngay het han", "Gia/thang", "Trang thai"
    );
    private final DefaultTableModel eventModel = model(
        "Ma su kien", "Ma luot", "Ma xe", "Bien so", "Ma SV", "Lop", "Thao tac", "Thoi gian", "Mien phi", "Phi"
    );
    private final DefaultTableModel feeModel = model(
        "Ma SV", "Lop", "So xe tinh phi", "Tien ve thang", "Tong luot",
        "Luot mien phi", "Luot tinh phi", "Phi vuot luot", "Tong phai tra"
    );

    private final JTable xeTable = new JTable(xeModel);
    private final JTable eventTable = new JTable(eventModel);
    private final JTable feeTable = new JTable(feeModel);
    private final JTextField eventMonth = new JTextField(7);
    private final JTextField feeMonth = new JTextField(7);

    public XeThangPanel() {
        setLayout(new BorderLayout());
        YearMonth currentMonth = YearMonth.now();
        eventMonth.setText(currentMonth.toString());
        feeMonth.setText(currentMonth.toString());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Xe dang ky", buildVehicleTab());
        tabs.addTab("Lich su lay/gui", buildEventTab());
        tabs.addTab("Phi theo thang", buildFeeTab());
        add(tabs, BorderLayout.CENTER);
        refreshVehicles();
    }

    private JPanel buildVehicleTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Tai lai");
        JButton register = new JButton("Dang ky xe");
        JButton edit = new JButton("Sua xe");
        JButton end = new JButton("Ket thuc ve thang");
        JButton pickup = new JButton("Ghi nhan lay xe");
        JButton returnVehicle = new JButton("Ghi nhan gui xe");
        for (JButton button : new JButton[]{refresh, register, edit, end, pickup, returnVehicle}) {
            actions.add(button);
        }
        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(xeTable), BorderLayout.CENTER);
        refresh.addActionListener(e -> refreshVehicles());
        register.addActionListener(e -> registerVehicle());
        edit.addActionListener(e -> editVehicle());
        end.addActionListener(e -> endRegistration());
        pickup.addActionListener(e -> recordEvent(true));
        returnVehicle.addActionListener(e -> recordEvent(false));
        return panel;
    }

    private JPanel buildEventTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Xem lich su");
        filter.add(new JLabel("Thang (YYYY-MM):"));
        filter.add(eventMonth);
        filter.add(refresh);
        panel.add(filter, BorderLayout.NORTH);
        panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);
        refresh.addActionListener(e -> refreshEvents());
        return panel;
    }

    private JPanel buildFeeTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Thong ke");
        filter.add(new JLabel("Thang (YYYY-MM):"));
        filter.add(feeMonth);
        filter.add(refresh);
        panel.add(filter, BorderLayout.NORTH);
        panel.add(new JScrollPane(feeTable), BorderLayout.CENTER);
        refresh.addActionListener(e -> refreshFees());
        return panel;
    }

    private void refreshVehicles() {
        run(() -> {
            List<XeThang> vehicles = repository.findAll();
            xeModel.setRowCount(0);
            for (XeThang xe : vehicles) {
                xeModel.addRow(new Object[]{
                    xe.maXe(), xe.maSV(), xe.lop(), xe.bienSo(), xe.loaiXe(), xe.ngayDangKy(),
                    xe.ngayHetHan() == null ? "" : xe.ngayHetHan(), xe.donGiaThang(),
                    xe.dangHoatDong() ? "Dang hoat dong" : "Het han"
                });
            }
        });
    }

    private void refreshEvents() {
        run(() -> {
            List<SuKienXe> events = repository.findEvents(parseMonth(eventMonth));
            eventModel.setRowCount(0);
            for (SuKienXe event : events) {
                eventModel.addRow(new Object[]{
                    event.maSuKien(), event.maLuot(), event.maXe(), event.bienSo(), event.maSV(),
                    event.lop(), event.loaiSuKien(), event.thoiGian(),
                    event.mienPhi() ? "Co" : "Khong", event.soTienPhatSinh()
                });
            }
        });
    }

    private void refreshFees() {
        run(() -> {
            List<ThongKePhiXe> fees = repository.findMonthlyFees(parseMonth(feeMonth));
            feeModel.setRowCount(0);
            for (ThongKePhiXe fee : fees) {
                feeModel.addRow(new Object[]{
                    fee.maSV(), fee.lop(), fee.soXeThang(), fee.tienVeThang(), fee.soLuot(),
                    fee.soLuotMienPhi(), fee.soLuotTinhPhi(), fee.phiPhatSinh(), fee.tongPhaiTra()
                });
            }
        });
    }

    private void registerVehicle() {
        try {
            String maXe = ask("Ma xe:");
            if (maXe == null) return;
            String maSV = ask("Ma sinh vien dang ky:");
            if (maSV == null) return;
            String bienSo = ask("Bien so:");
            if (bienSo == null) return;
            String loaiXe = ask("Loai xe:");
            if (loaiXe == null) return;
            String expiry = ask("Ngay het han (yyyy-MM-dd, de trong neu chua xac dinh):");
            if (expiry == null) return;
            LocalDate ngayHetHan = expiry.isBlank() ? null : LocalDate.parse(expiry.trim());
            service.dangKyXe(maXe, maSV, bienSo, loaiXe, ngayHetHan);
            info("Dang ky xe thanh cong. Don gia ve thang: " + XeThangService.DON_GIA_THANG + " VND.");
            refreshVehicles();
        } catch (Exception e) {
            error("Khong dang ky duoc xe: " + e.getMessage());
        }
    }

    private void editVehicle() {
        XeThang selected = selectedVehicle();
        if (selected == null) return;
        try {
            String bienSo = JOptionPane.showInputDialog(this, "Bien so:", selected.bienSo());
            if (bienSo == null) return;
            String loaiXe = JOptionPane.showInputDialog(this, "Loai xe:", selected.loaiXe());
            if (loaiXe == null) return;
            String expiry = JOptionPane.showInputDialog(
                this, "Ngay het han (yyyy-MM-dd, de trong neu khong het han):",
                selected.ngayHetHan() == null ? "" : selected.ngayHetHan().toString()
            );
            if (expiry == null) return;
            LocalDate ngayHetHan = expiry.isBlank() ? null : LocalDate.parse(expiry.trim());
            service.suaXe(selected.maXe(), bienSo, loaiXe, ngayHetHan);
            refreshVehicles();
        } catch (Exception e) {
            error("Khong sua duoc xe: " + e.getMessage());
        }
    }

    private void endRegistration() {
        XeThang selected = selectedVehicle();
        if (selected == null) return;
        if (JOptionPane.showConfirmDialog(
                this, "Ket thuc dang ky ve thang cho xe " + selected.maXe() + "?",
                "Xac nhan", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            service.ketThucDangKy(selected.maXe());
            info("Da ket thuc dang ky ve thang.");
            refreshVehicles();
        } catch (Exception e) {
            error("Khong the ket thuc dang ky: " + e.getMessage());
        }
    }

    private void recordEvent(boolean pickup) {
        XeThang selected = selectedVehicle();
        if (selected == null) return;
        try {
            BigDecimal fee = pickup ? service.layXe(selected.maXe()) : service.guiXe(selected.maXe());
            String action = pickup ? "lay xe" : "gui xe";
            info("Da ghi nhan " + action + ". Phi phat sinh: " + fee + " VND.");
            refreshVehicles();
            refreshEvents();
            refreshFees();
        } catch (Exception e) {
            error("Khong ghi nhan duoc: " + e.getMessage());
        }
    }

    private XeThang selectedVehicle() {
        int row = xeTable.getSelectedRow();
        if (row < 0) {
            error("Hay chon xe trong danh sach.");
            return null;
        }
        String maXe = (String) xeModel.getValueAt(xeTable.convertRowIndexToModel(row), 0);
        try {
            for (XeThang xe : repository.findAll()) {
                if (xe.maXe().equals(maXe)) return xe;
            }
            error("Khong tim thay xe da chon.");
        } catch (Exception e) {
            error(e.getMessage());
        }
        return null;
    }

    private YearMonth parseMonth(JTextField field) {
        try {
            return YearMonth.parse(field.getText().trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Thang khong hop le. Hay nhap theo dang YYYY-MM.");
        }
    }

    private String ask(String label) {
        return JOptionPane.showInputDialog(this, label);
    }

    private void run(SqlAction action) {
        try {
            action.run();
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "Thong bao", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Loi", JOptionPane.ERROR_MESSAGE);
    }

    private DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws Exception;
    }
}
