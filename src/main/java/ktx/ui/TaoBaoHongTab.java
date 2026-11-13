package ktx.ui;

import ktx.model.BaoHongDanhMuc;
import ktx.service.BaoHongService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.LongConsumer;

/** Tab "Tạo báo hỏng": sinh viên báo sự cố phòng đang ở, hoặc nhân viên ghi nhận sự cố tự phát hiện. */
class TaoBaoHongTab extends JPanel implements KhachThamUi.TaiLai {
    private final BaoHongService service = new BaoHongService();
    private final LongConsumer saoKhiTao;

    private final JRadioButton rbSinhVien = new JRadioButton("Sinh viên báo", true);
    private final JRadioButton rbNhanVien = new JRadioButton("Nhân viên phát hiện");
    private final JTextField maSV = new JTextField(18);
    private final JTextField nhanVien = new JTextField(18);
    private final JLabel goiY = new JLabel(" ");
    private final JComboBox<String> phong = new JComboBox<>();
    private final JComboBox<String> loai = new JComboBox<>(BaoHongDanhMuc.LOAI.toArray(new String[0]));
    private final JComboBox<String> mucDo = new JComboBox<>(BaoHongDanhMuc.MUC_DO.toArray(new String[0]));
    private final JTextArea moTa = new JTextArea(6, 40);

    TaoBaoHongTab(LongConsumer saoKhiTao) {
        this.saoKhiTao = saoKhiTao;
        setLayout(new BorderLayout(8, 8));
        mucDo.setSelectedItem("Trung bình");
        moTa.setLineWrap(true); moTa.setWrapStyleWord(true);

        ButtonGroup nhom = new ButtonGroup(); nhom.add(rbSinhVien); nhom.add(rbNhanVien);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Thông tin sự cố"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8); g.anchor = GridBagConstraints.WEST;

        JPanel nguoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        nguoi.add(rbSinhVien); nguoi.add(rbNhanVien);
        dong(p, g, 0, "Người báo:", nguoi);
        dong(p, g, 1, "Mã sinh viên:", maSV);
        g.gridx = 2; g.gridy = 1; p.add(goiY, g);
        dong(p, g, 2, "Tên nhân viên ghi nhận:", nhanVien);
        dong(p, g, 3, "Số phòng:", phong);
        dong(p, g, 4, "Loại sự cố:", loai);
        dong(p, g, 5, "Mức độ:", mucDo);
        g.anchor = GridBagConstraints.NORTHWEST;
        dong(p, g, 6, "Mô tả sự cố:", new JScrollPane(moTa));
        JButton gui = new JButton("Gửi báo hỏng");
        g.gridx = 1; g.gridy = 7; g.anchor = GridBagConstraints.WEST; p.add(gui, g);
        add(p, BorderLayout.NORTH);

        JLabel ghiChu = new JLabel("<html>Sinh viên chỉ báo hỏng được cho phòng mình đang ở. "
            + "Sự cố mức <b>Cao</b> hoặc <b>Khẩn cấp</b> sẽ được cảnh báo ngay và tô màu trong danh sách.</html>");
        ghiChu.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        add(ghiChu, BorderLayout.CENTER);

        rbSinhVien.addActionListener(e -> capNhatCheDo());
        rbNhanVien.addActionListener(e -> capNhatCheDo());
        gui.addActionListener(e -> guiBaoHong());
        maSV.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { goiYPhong(); }
        });
        capNhatCheDo();
        taiLai();
    }

    private void dong(JPanel p, GridBagConstraints g, int hang, String nhan, JComponent o) {
        g.gridx = 0; g.gridy = hang; p.add(new JLabel(nhan), g);
        g.gridx = 1; p.add(o, g);
    }

    private void capNhatCheDo() {
        boolean sv = rbSinhVien.isSelected();
        maSV.setEnabled(sv); nhanVien.setEnabled(!sv);
        if (!sv) goiY.setText(" ");
    }

    @Override public void taiLai() {
        KhachThamUi.chay(this, () -> {
            Object dang = phong.getSelectedItem();
            phong.removeAllItems();
            for (String s : service.danhSachPhong()) phong.addItem(s);
            if (dang != null) phong.setSelectedItem(dang);
        });
    }

    /** Nhập mã sinh viên xong thì gợi ý (và tự chọn) phòng sinh viên đang ở. */
    private void goiYPhong() {
        if (!rbSinhVien.isSelected() || maSV.getText().isBlank()) { goiY.setText(" "); return; }
        try {
            var ph = service.phongDangO(maSV.getText());
            if (ph.isPresent()) { phong.setSelectedItem(ph.get()); goiY.setText("Đang ở phòng " + ph.get()); }
            else goiY.setText("Chưa có hợp đồng thuê đang hiệu lực");
        } catch (Exception e) { goiY.setText(" "); }
    }

    private void guiBaoHong() {
        KhachThamUi.chay(this, () -> {
            boolean sv = rbSinhVien.isSelected();
            String muc = (String) mucDo.getSelectedItem();
            long ma = service.taoBaoHong((String) phong.getSelectedItem(), sv ? maSV.getText() : "",
                    sv ? "" : nhanVien.getText(), (String) loai.getSelectedItem(), moTa.getText(), muc);
            if (BaoHongDanhMuc.laCanhBao(muc)) {
                JOptionPane.showMessageDialog(this,
                    "Đã ghi nhận báo hỏng số " + ma + ".\n\nCẢNH BÁO: sự cố mức \"" + muc.toUpperCase()
                    + "\" cần được xử lý sớm, hãy thông báo ngay cho ban quản lý.",
                    "Cảnh báo sự cố " + muc, JOptionPane.WARNING_MESSAGE);
            } else {
                KhachThamUi.thanhCong(this, "Đã ghi nhận báo hỏng số " + ma + ".");
            }
            maSV.setText(""); nhanVien.setText(""); moTa.setText(""); goiY.setText(" ");
            mucDo.setSelectedItem("Trung bình");
            saoKhiTao.accept(ma);
        });
    }
}
