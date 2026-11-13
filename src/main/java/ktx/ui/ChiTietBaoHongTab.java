package ktx.ui;

import ktx.model.BaoHong;
import ktx.model.LichSuBaoHong;
import ktx.service.BaoHongService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Tab "Chi tiết & lịch sử": thông tin một báo hỏng và các bước xử lý (lịch sử bảo trì). */
class ChiTietBaoHongTab extends JPanel {
    private final BaoHongService service = new BaoHongService();
    private final JTextField ma = new JTextField(8);
    private final JLabel canhBao = new JLabel(" ");
    private final JTextArea chiTiet = new JTextArea(8, 60);
    private final DefaultTableModel model = KhachThamUi.taoBang("Thời gian", "Từ trạng thái", "Sang trạng thái", "Nhân viên", "Ghi chú / kết quả");

    ChiTietBaoHongTab() {
        setLayout(new BorderLayout(8, 8));
        JPanel bac = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton xem = new JButton("Xem");
        bac.add(new JLabel("Mã báo hỏng:")); bac.add(ma); bac.add(xem);
        JPanel dau = new JPanel(new BorderLayout());
        dau.add(bac, BorderLayout.NORTH);
        canhBao.setForeground(BaoHongUi.CHU_CANH_BAO); canhBao.setFont(canhBao.getFont().deriveFont(Font.BOLD));
        canhBao.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 0));
        dau.add(canhBao, BorderLayout.SOUTH);
        add(dau, BorderLayout.NORTH);

        chiTiet.setEditable(false); chiTiet.setLineWrap(true); chiTiet.setWrapStyleWord(true);
        chiTiet.setBackground(new Color(0xF8FAFC));
        JScrollPane l = new JScrollPane(new JTable(model));
        l.setBorder(BorderFactory.createTitledBorder("Lịch sử xử lý / bảo trì"));
        JSplitPane chia = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(chiTiet), l);
        chia.setResizeWeight(0.4);
        add(chia, BorderLayout.CENTER);

        xem.addActionListener(e -> xemTheoOTrong());
        ma.addActionListener(e -> xemTheoOTrong());
        chiTiet.setText("Nhập mã báo hỏng (hoặc nhấp đúp một dòng ở tab Danh sách) rồi bấm \"Xem\".");
    }

    /** Gọi từ danh sách: điền mã rồi hiển thị. */
    void hienThi(long maBaoHong) { ma.setText(String.valueOf(maBaoHong)); xemTheoOTrong(); }

    private void xemTheoOTrong() {
        KhachThamUi.chay(this, () -> {
            long id;
            try { id = Long.parseLong(ma.getText().trim()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Mã báo hỏng phải là một số."); }
            BaoHong b = service.layBaoHong(id);
            List<LichSuBaoHong> ls = service.lichSuXuLy(id);
            chiTiet.setText("Báo hỏng số " + b.maBaoHong()
                + "\nPhòng: " + b.soPhong() + "   Người báo: " + BaoHongUi.nguoiBao(b)
                + "\nLoại sự cố: " + b.loaiSuCo() + "   Mức độ: " + b.mucDo() + "   Trạng thái: " + b.trangThai()
                + "\nNgày báo: " + KhachThamUi.dinhDang(b.ngayBao())
                + "\nNhân viên xử lý: " + (b.nhanVienXuLy() == null ? "(chưa phân công)" : b.nhanVienXuLy())
                + "\nKết thúc lúc: " + (b.ngayXuLy() == null ? "(chưa kết thúc)" : KhachThamUi.dinhDang(b.ngayXuLy()) + "   Thời gian xử lý: " + BaoHongUi.thoiGianXuLy(b))
                + "\nKết quả xử lý: " + (b.ketQuaXuLy() == null ? "" : b.ketQuaXuLy())
                + "\n\nMô tả sự cố: " + b.moTa());
            chiTiet.setCaretPosition(0);
            canhBao.setText(b.canChuY() ? "CẢNH BÁO: sự cố mức " + b.mucDo().toUpperCase() + " chưa hoàn thành, cần xử lý sớm." : " ");
            model.setRowCount(0);
            for (LichSuBaoHong h : ls)
                model.addRow(new Object[]{KhachThamUi.dinhDang(h.thoiGian()), h.trangThaiCu() == null ? "(tạo mới)" : h.trangThaiCu(),
                        h.trangThaiMoi(), h.nhanVien() == null ? "" : h.nhanVien(), h.ghiChu() == null ? "" : h.ghiChu()});
        });
    }
}
