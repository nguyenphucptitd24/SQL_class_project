package ktx.ui;

import ktx.model.Khach;
import ktx.service.KhachThamService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Tab "Khách": xem, tìm, thêm, sửa thông tin khách đến thăm. */
class KhachTab extends JPanel implements KhachThamUi.TaiLai {
    private final KhachThamService service = new KhachThamService();
    private final DefaultTableModel model = KhachThamUi.taoBang("Số CMT", "Họ tên", "Ngày sinh");
    private final JTable table = new JTable(model);
    private final JTextField search = new JTextField(20);
    private final List<Khach> hienTai = new ArrayList<>();

    KhachTab() {
        setLayout(new BorderLayout(8, 8));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton find = new JButton("Tìm kiếm"), refresh = new JButton("Tải lại");
        JButton add = new JButton("Thêm khách"), edit = new JButton("Sửa");
        bar.add(new JLabel("Từ khóa (số CMT hoặc họ tên):"));
        bar.add(search); bar.add(find); bar.add(refresh); bar.add(add); bar.add(edit);
        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        find.addActionListener(e -> timKiem());
        search.addActionListener(e -> timKiem());
        refresh.addActionListener(e -> { search.setText(""); taiLai(); });
        add.addActionListener(e -> moForm(null));
        edit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { KhachThamUi.hienLoi(this, new IllegalArgumentException("Hãy chọn một khách trong bảng.")); return; }
            moForm(hienTai.get(r));
        });
        taiLai();
    }

    @Override public void taiLai() { timKiem(); }

    private void timKiem() {
        KhachThamUi.chay(this, () -> hienThi(service.timKhach(search.getText())));
    }

    private void hienThi(List<Khach> ds) {
        hienTai.clear(); hienTai.addAll(ds);
        model.setRowCount(0);
        for (Khach k : ds) model.addRow(new Object[]{k.soCMT(), k.hoTen(), KhachThamUi.NGAY.format(k.ngaySinh())});
    }

    /** Hộp thoại thêm (cu == null) hoặc sửa khách; nhập sai thì báo lỗi và cho nhập lại, không mất dữ liệu đã gõ. */
    private void moForm(Khach cu) {
        JTextField cmt = new JTextField(cu == null ? "" : cu.soCMT(), 20);
        JTextField ten = new JTextField(cu == null ? "" : cu.hoTen(), 20);
        JTextField ngay = new JTextField(cu == null ? "" : KhachThamUi.NGAY.format(cu.ngaySinh()), 20);
        cmt.setEnabled(cu == null);   // số CMT là khóa chính nên không sửa được
        Object[] noiDung = {"Số CMT (chữ và số, 6-20 ký tự):", cmt, "Họ tên:", ten, "Ngày sinh (dd/MM/yyyy):", ngay};
        while (true) {
            int kq = JOptionPane.showConfirmDialog(this, noiDung, cu == null ? "Thêm khách" : "Sửa thông tin khách",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (kq != JOptionPane.OK_OPTION) return;
            try {
                Khach k = new Khach(cmt.getText(), ten.getText(), KhachThamUi.parseNgay(ngay.getText(), "Ngày sinh"));
                if (cu == null) service.themKhach(k); else service.suaKhach(k);
                taiLai();
                return;
            } catch (Exception ex) {
                KhachThamUi.hienLoi(this, ex);
            }
        }
    }
}
