package ktx.ui;

import ktx.model.KhoangThoiGian;
import ktx.model.LuotTham;
import ktx.service.KhachThamService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Tab "Lịch sử": tra cứu các lượt thăm theo từ khóa (CMT khách, tên khách, mã SV) và khoảng ngày. */
class LichSuTab extends JPanel implements KhachThamUi.TaiLai {
    private final KhachThamService service = new KhachThamService();
    private final DefaultTableModel model = KhachThamUi.taoBang(
        "Mã lượt", "Số CMT khách", "Họ tên khách", "Mã SV được thăm", "Giờ đến", "Giờ về", "Thời lượng");
    private final JTable table = new JTable(model);
    private final JTextField tuKhoa = new JTextField(14);
    private final JTextField tuNgay = new JTextField(9);
    private final JTextField denNgay = new JTextField(9);
    private final JLabel ketQua = new JLabel(" ");

    LichSuTab() {
        setLayout(new BorderLayout(8, 8));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton tim = new JButton("Tìm kiếm"), xoaLoc = new JButton("Xóa bộ lọc");
        bar.add(new JLabel("Từ khóa (CMT khách / tên khách / mã SV):")); bar.add(tuKhoa);
        bar.add(new JLabel("Từ ngày:")); bar.add(tuNgay);
        bar.add(new JLabel("Đến ngày:")); bar.add(denNgay);
        bar.add(tim); bar.add(xoaLoc);
        tuNgay.setToolTipText("dd/MM/yyyy, bỏ trống nếu không lọc theo ngày");
        denNgay.setToolTipText("dd/MM/yyyy, bỏ trống nếu không lọc theo ngày");

        JPanel bac = new JPanel(new BorderLayout());
        bac.add(bar, BorderLayout.CENTER);
        bac.add(ketQua, BorderLayout.SOUTH);
        ketQua.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 0));
        add(bac, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        tim.addActionListener(e -> timKiem());
        tuKhoa.addActionListener(e -> timKiem());
        xoaLoc.addActionListener(e -> { tuKhoa.setText(""); tuNgay.setText(""); denNgay.setText(""); timKiem(); });
        timKiem();
    }

    @Override public void taiLai() { timKiem(); }

    private void timKiem() {
        KhachThamUi.chay(this, () -> {
            boolean coTu = !tuNgay.getText().isBlank(), coDen = !denNgay.getText().isBlank();
            if (coTu != coDen)
                throw new IllegalArgumentException("Hãy nhập đủ từ ngày và đến ngày, hoặc để trống cả hai.");
            KhoangThoiGian k = coTu
                ? KhoangThoiGian.khoang(KhachThamUi.parseNgay(tuNgay.getText(), "Từ ngày"),
                                        KhachThamUi.parseNgay(denNgay.getText(), "Đến ngày"))
                : null;
            hienThi(service.timLuotTham(tuKhoa.getText(), k));
        });
    }

    private void hienThi(List<LuotTham> ds) {
        model.setRowCount(0);
        for (LuotTham lt : ds)
            model.addRow(new Object[]{lt.maLuotTham(), lt.soCMTKhach(), lt.hoTenKhach(), lt.maSV(),
                    KhachThamUi.dinhDang(lt.thoiGianDen()), KhachThamUi.dinhDang(lt.thoiGianDi()),
                    KhachThamUi.thoiLuong(lt.thoiGianDen(), lt.thoiGianDi())});
        ketQua.setText("Tìm thấy " + ds.size() + " lượt thăm.");
    }
}
