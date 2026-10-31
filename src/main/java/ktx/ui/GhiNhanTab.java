package ktx.ui;

import ktx.model.LuotTham;
import ktx.service.KhachThamService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Tab "Ghi nhận lượt thăm": ghi nhận khách đến, và ghi nhận giờ về cho khách đang ở trong KTX. */
class GhiNhanTab extends JPanel implements KhachThamUi.TaiLai {
    private final KhachThamService service = new KhachThamService();
    private final JTextField cmt = new JTextField(18);
    private final JLabel tenKhach = new JLabel(" ");
    private final JTextField maSV = new JTextField(18);
    private final JTextField gioDen = new JTextField(KhachThamUi.bayGio(), 18);
    private final JTextField gioVe = new JTextField(18);
    private final DefaultTableModel model =
        KhachThamUi.taoBang("Mã lượt", "Số CMT khách", "Họ tên khách", "Mã SV được thăm", "Giờ đến", "Đã ở");
    private final JTable table = new JTable(model);
    private final List<LuotTham> dangO = new ArrayList<>();

    GhiNhanTab() {
        setLayout(new BorderLayout(8, 8));
        add(taoForm(), BorderLayout.NORTH);

        JPanel duoi = new JPanel(new BorderLayout(4, 4));
        duoi.setBorder(BorderFactory.createTitledBorder("Khách đang ở trong KTX (chưa ghi nhận giờ về)"));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton ve = new JButton("Ghi nhận khách ra về"), tai = new JButton("Tải lại");
        bar.add(ve); bar.add(tai);
        duoi.add(bar, BorderLayout.NORTH);
        duoi.add(new JScrollPane(table), BorderLayout.CENTER);
        add(duoi, BorderLayout.CENTER);

        ve.addActionListener(e -> khachRaVe());
        tai.addActionListener(e -> taiLai());
        cmt.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { hienTenKhach(); }
        });
        taiLai();
    }

    private JPanel taoForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Ghi nhận lượt thăm mới"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 6, 3, 6);
        g.anchor = GridBagConstraints.WEST;

        dong(p, g, 0, "Số CMT khách:", cmt);
        g.gridx = 2; g.gridy = 0; p.add(tenKhach, g);
        dong(p, g, 1, "Mã sinh viên được thăm:", maSV);
        dong(p, g, 2, "Giờ đến (dd/MM/yyyy HH:mm):", gioDen);
        JButton bayGio = new JButton("Bây giờ");
        g.gridx = 2; g.gridy = 2; p.add(bayGio, g);
        dong(p, g, 3, "Giờ về (bỏ trống nếu khách còn ở):", gioVe);

        JButton ghi = new JButton("Ghi nhận lượt thăm");
        g.gridx = 1; g.gridy = 4; p.add(ghi, g);

        bayGio.addActionListener(e -> gioDen.setText(KhachThamUi.bayGio()));
        ghi.addActionListener(e -> ghiNhan());
        return p;
    }

    private void dong(JPanel p, GridBagConstraints g, int hang, String nhan, JComponent o) {
        g.gridx = 0; g.gridy = hang; p.add(new JLabel(nhan), g);
        g.gridx = 1; p.add(o, g);
    }

    @Override public void taiLai() {
        KhachThamUi.chay(this, () -> {
            dangO.clear(); dangO.addAll(service.khachDangO());
            model.setRowCount(0);
            LocalDateTime now = LocalDateTime.now();
            for (LuotTham lt : dangO)
                model.addRow(new Object[]{lt.maLuotTham(), lt.soCMTKhach(), lt.hoTenKhach(), lt.maSV(),
                        KhachThamUi.dinhDang(lt.thoiGianDen()), moTaDaO(lt.thoiGianDen(), now)});
        });
    }

    private String moTaDaO(LocalDateTime den, LocalDateTime now) {
        long phut = java.time.Duration.between(den, now).toMinutes();
        if (phut < 0) phut = 0;
        return phut >= 60 ? (phut / 60) + " giờ " + (phut % 60) + " phút" : phut + " phút";
    }

    private void hienTenKhach() {
        if (cmt.getText().isBlank()) { tenKhach.setText(" "); return; }
        try {
            tenKhach.setText(service.layKhach(cmt.getText())
                .map(k -> "Khách: " + k.hoTen())
                .orElse("Chưa có trong hệ thống - hãy thêm ở tab Khách"));
        } catch (Exception e) { tenKhach.setText(" "); }
    }

    private void ghiNhan() {
        KhachThamUi.chay(this, () -> {
            LocalDateTime den = KhachThamUi.parseNgayGio(gioDen.getText(), "Giờ đến");
            LocalDateTime di = gioVe.getText().isBlank() ? null : KhachThamUi.parseNgayGio(gioVe.getText(), "Giờ về");
            long ma = service.ghiNhanLuotTham(cmt.getText(), maSV.getText(), den, di);
            KhachThamUi.thanhCong(this, "Đã ghi nhận lượt thăm số " + ma
                + (di == null ? " (khách đang ở trong KTX)." : "."));
            cmt.setText(""); maSV.setText(""); gioVe.setText(""); tenKhach.setText(" ");
            gioDen.setText(KhachThamUi.bayGio());
            taiLai();
        });
    }

    private void khachRaVe() {
        int r = table.getSelectedRow();
        if (r < 0) { KhachThamUi.hienLoi(this, new IllegalArgumentException("Hãy chọn một khách trong bảng.")); return; }
        LuotTham lt = dangO.get(r);
        String nhap = JOptionPane.showInputDialog(this,
            "Giờ về của " + lt.hoTenKhach() + " (dd/MM/yyyy HH:mm):", KhachThamUi.bayGio());
        if (nhap == null) return;
        KhachThamUi.chay(this, () -> {
            service.ghiNhanVe(lt.maLuotTham(), KhachThamUi.parseNgayGio(nhap, "Giờ về"));
            taiLai();
        });
    }
}
