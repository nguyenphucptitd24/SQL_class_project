package ktx.ui;

import ktx.model.BaoHong;
import ktx.model.BaoHongDanhMuc;
import ktx.service.BaoHongService;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Tab "Cập nhật xử lý": phân công nhân viên, chuyển trạng thái, ghi kết quả; xóa báo hỏng gửi nhầm (chỉ khi Mới báo). */
class CapNhatBaoHongTab extends JPanel {
    private final BaoHongService service = new BaoHongService();
    private final JTextField ma = new JTextField(8);
    private final JTextArea tomTat = new JTextArea(6, 50);
    private final JComboBox<String> trangThaiMoi = new JComboBox<>();
    private final JTextField nhanVien = new JTextField(22);
    private final JTextField thoiDiem = new JTextField(KhachThamUi.bayGio(), 16);
    private final JLabel nhanGhiChu = new JLabel("Ghi chú (không bắt buộc):");
    private final JTextArea ghiChu = new JTextArea(4, 50);
    private final JButton capNhat = new JButton("Cập nhật");
    private final JButton xoa = new JButton("Xóa báo hỏng này");
    private BaoHong dangXem;

    CapNhatBaoHongTab() {
        setLayout(new BorderLayout(8, 8));
        JPanel bac = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton tai = new JButton("Tải báo hỏng");
        bac.add(new JLabel("Mã báo hỏng:")); bac.add(ma); bac.add(tai);
        add(bac, BorderLayout.NORTH);

        tomTat.setEditable(false); tomTat.setLineWrap(true); tomTat.setWrapStyleWord(true);
        tomTat.setBackground(new Color(0xF8FAFC));
        ghiChu.setLineWrap(true); ghiChu.setWrapStyleWord(true);

        JPanel giua = new JPanel(new BorderLayout(8, 8));
        giua.add(new JScrollPane(tomTat), BorderLayout.NORTH);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Cập nhật"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8); g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0; p.add(new JLabel("Chuyển sang trạng thái:"), g); g.gridx = 1; p.add(trangThaiMoi, g);
        g.gridx = 0; g.gridy = 1; p.add(new JLabel("Nhân viên xử lý:"), g); g.gridx = 1; p.add(nhanVien, g);
        g.gridx = 0; g.gridy = 2; p.add(new JLabel("Thời điểm (dd/MM/yyyy HH:mm):"), g); g.gridx = 1; p.add(thoiDiem, g);
        JButton bayGio = new JButton("Bây giờ"); g.gridx = 2; p.add(bayGio, g);
        g.anchor = GridBagConstraints.NORTHWEST;
        g.gridx = 0; g.gridy = 3; p.add(nhanGhiChu, g); g.gridx = 1; g.gridwidth = 2; p.add(new JScrollPane(ghiChu), g);
        g.gridwidth = 1; g.anchor = GridBagConstraints.WEST;
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT)); nut.add(capNhat); nut.add(xoa);
        g.gridx = 1; g.gridy = 4; g.gridwidth = 2; p.add(nut, g);
        giua.add(p, BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);

        tai.addActionListener(e -> taiTheoOTrong());
        ma.addActionListener(e -> taiTheoOTrong());
        bayGio.addActionListener(e -> thoiDiem.setText(KhachThamUi.bayGio()));
        trangThaiMoi.addActionListener(e -> doiNhan());
        capNhat.addActionListener(e -> thucHien());
        xoa.addActionListener(e -> xoaBaoHong());
        hienKhoa();
    }

    /** Gọi từ danh sách: điền mã rồi tải. */
    void tai(long maBaoHong) { ma.setText(String.valueOf(maBaoHong)); taiTheoOTrong(); }

    private void taiTheoOTrong() {
        KhachThamUi.chay(this, () -> {
            long id;
            try { id = Long.parseLong(ma.getText().trim()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Mã báo hỏng phải là một số."); }
            napDuLieu(service.layBaoHong(id));
        });
    }

    private void napDuLieu(BaoHong b) {
        dangXem = b;
        tomTat.setText("Báo hỏng số " + b.maBaoHong() + " · Phòng " + b.soPhong() + " · Người báo: " + BaoHongUi.nguoiBao(b)
            + "\nLoại: " + b.loaiSuCo() + " · Mức độ: " + b.mucDo() + " · Trạng thái: " + b.trangThai()
            + "\nNgày báo: " + KhachThamUi.dinhDang(b.ngayBao())
            + (b.nhanVienXuLy() != null ? "\nNhân viên phụ trách: " + b.nhanVienXuLy() : "")
            + (b.ngayXuLy() != null ? "\nKết thúc lúc: " + KhachThamUi.dinhDang(b.ngayXuLy()) + " · Kết quả: " + b.ketQuaXuLy() : "")
            + "\nMô tả: " + b.moTa()
            + (b.canChuY() ? "\n\nCẢNH BÁO: sự cố mức " + b.mucDo() + " chưa hoàn thành." : ""));
        trangThaiMoi.removeAllItems();
        List<String> tiep = BaoHongDanhMuc.trangThaiTiepTheo(b.trangThai());
        for (String s : tiep) trangThaiMoi.addItem(s);
        nhanVien.setText(b.nhanVienXuLy() == null ? "" : b.nhanVienXuLy());
        thoiDiem.setText(KhachThamUi.bayGio()); ghiChu.setText("");
        boolean choPhep = !tiep.isEmpty();
        trangThaiMoi.setEnabled(choPhep); nhanVien.setEnabled(choPhep); thoiDiem.setEnabled(choPhep);
        ghiChu.setEnabled(choPhep); capNhat.setEnabled(choPhep);
        xoa.setEnabled(BaoHongDanhMuc.MOI_BAO.equals(b.trangThai()));
        if (!choPhep) tomTat.append("\n\nBáo hỏng đã kết thúc nên không thể cập nhật thêm (được giữ lại để lưu lịch sử).");
        doiNhan();
    }

    private void hienKhoa() {
        trangThaiMoi.setEnabled(false); nhanVien.setEnabled(false); thoiDiem.setEnabled(false);
        ghiChu.setEnabled(false); capNhat.setEnabled(false); xoa.setEnabled(false);
        tomTat.setText("Nhập mã báo hỏng (hoặc chọn từ tab Danh sách) rồi bấm \"Tải báo hỏng\".");
    }

    private void doiNhan() {
        String moi = (String) trangThaiMoi.getSelectedItem();
        if (BaoHongDanhMuc.HOAN_THANH.equals(moi)) nhanGhiChu.setText("Kết quả xử lý (bắt buộc):");
        else if (BaoHongDanhMuc.TU_CHOI.equals(moi)) nhanGhiChu.setText("Lý do từ chối (bắt buộc):");
        else nhanGhiChu.setText("Ghi chú (không bắt buộc):");
    }

    private void thucHien() {
        if (dangXem == null) return;
        KhachThamUi.chay(this, () -> {
            var luc = KhachThamUi.parseNgayGio(thoiDiem.getText(), "Thời điểm");
            BaoHong kq = service.capNhatTrangThai(dangXem.maBaoHong(), (String) trangThaiMoi.getSelectedItem(),
                    nhanVien.getText(), ghiChu.getText(), luc);
            KhachThamUi.thanhCong(this, "Đã chuyển báo hỏng số " + kq.maBaoHong() + " sang \"" + kq.trangThai() + "\".");
            napDuLieu(kq);
        });
    }

    private void xoaBaoHong() {
        if (dangXem == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Xóa báo hỏng số " + dangXem.maBaoHong() + "? Chỉ nên xóa báo hỏng gửi nhầm.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        KhachThamUi.chay(this, () -> {
            service.xoaBaoHong(dangXem.maBaoHong());
            KhachThamUi.thanhCong(this, "Đã xóa báo hỏng số " + dangXem.maBaoHong() + ".");
            dangXem = null; ma.setText(""); hienKhoa();
        });
    }
}
