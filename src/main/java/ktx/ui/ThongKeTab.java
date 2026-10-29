package ktx.ui;

import ktx.model.KhoangThoiGian;
import ktx.model.ThongKeKhachTham;
import ktx.model.TongKhachTham;
import ktx.model.TongSinhVienDuocTham;
import ktx.service.KhachThamService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Tab "Thống kê": chọn kỳ (tuần Thứ Hai - Chủ nhật, tháng, hoặc khoảng tùy chọn) rồi xem 3 bảng thống kê.
 * Một lượt thăm thuộc kỳ nếu giờ đến của nó nằm trong kỳ.
 */
class ThongKeTab extends JPanel {
    private final KhachThamService service = new KhachThamService();
    private final JTextField tuNgay = new JTextField(9);
    private final JTextField denNgay = new JTextField(9);
    private final JLabel tomTat = new JLabel(" ");

    private final DefaultTableModel mKhachTheoSV = KhachThamUi.taoBang(
        "Mã SV", "Số CMT SV", "Ngày sinh SV", "Lớp", "Quê quán",
        "Số CMT khách", "Họ tên khách", "Ngày sinh khách", "Số lần đến");
    private final DefaultTableModel mTheoKhach = KhachThamUi.taoBang(
        "Số CMT khách", "Họ tên khách", "Ngày sinh khách", "Tổng số lần đến", "Số sinh viên được thăm");
    private final DefaultTableModel mTheoSV = KhachThamUi.taoBang(
        "Mã SV", "Lớp", "Quê quán", "Số lượt được thăm", "Số khách khác nhau");

    ThongKeTab() {
        setLayout(new BorderLayout(8, 8));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton tuanNay = new JButton("Tuần này"), tuanTruoc = new JButton("Tuần trước");
        JButton thangNay = new JButton("Tháng này"), thangTruoc = new JButton("Tháng trước");
        JButton xem = new JButton("Thống kê");
        bar.add(tuanNay); bar.add(tuanTruoc); bar.add(thangNay); bar.add(thangTruoc);
        bar.add(new JLabel("  Từ ngày:")); bar.add(tuNgay);
        bar.add(new JLabel("Đến ngày:")); bar.add(denNgay);
        bar.add(xem);

        JPanel bac = new JPanel(new BorderLayout());
        bac.add(bar, BorderLayout.CENTER);
        bac.add(tomTat, BorderLayout.SOUTH);
        tomTat.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 0));
        add(bac, BorderLayout.NORTH);

        JTabbedPane kq = new JTabbedPane();
        kq.addTab("Khách đến thăm từng sinh viên", new JScrollPane(new JTable(mKhachTheoSV)));
        kq.addTab("Tổng số lần mỗi khách đến", new JScrollPane(new JTable(mTheoKhach)));
        kq.addTab("Số lượt thăm của từng sinh viên", new JScrollPane(new JTable(mTheoSV)));
        add(kq, BorderLayout.CENTER);

        // Các nút chọn nhanh chỉ điền ngày rồi thống kê luôn; tuần luôn tính từ Thứ Hai đến Chủ nhật.
        tuanNay.addActionListener(e -> datKy(KhoangThoiGian.tuan(LocalDate.now())));
        tuanTruoc.addActionListener(e -> datKy(KhoangThoiGian.tuan(LocalDate.now().minusWeeks(1))));
        thangNay.addActionListener(e -> datKy(KhoangThoiGian.thang(YearMonth.now())));
        thangTruoc.addActionListener(e -> datKy(KhoangThoiGian.thang(YearMonth.now().minusMonths(1))));
        xem.addActionListener(e -> thongKe());

        datKy(KhoangThoiGian.tuan(LocalDate.now()));   // mặc định: tuần hiện tại
    }

    private void datKy(KhoangThoiGian k) {
        tuNgay.setText(KhachThamUi.NGAY.format(k.ngayDau()));
        denNgay.setText(KhachThamUi.NGAY.format(k.ngayCuoi()));
        thongKe();
    }

    private void thongKe() {
        KhachThamUi.chay(this, () -> {
            KhoangThoiGian k = KhoangThoiGian.khoang(
                KhachThamUi.parseNgay(tuNgay.getText(), "Từ ngày"),
                KhachThamUi.parseNgay(denNgay.getText(), "Đến ngày"));

            List<ThongKeKhachTham> a = service.thongKeKhachTheoSinhVien(k);
            mKhachTheoSV.setRowCount(0);
            int tongLuot = 0;
            for (ThongKeKhachTham t : a) {
                tongLuot += t.soLan();
                mKhachTheoSV.addRow(new Object[]{
                    t.sinhVien().maSV(), t.sinhVien().soCMT(), KhachThamUi.NGAY.format(t.sinhVien().ngaySinh()),
                    t.sinhVien().lop(), t.sinhVien().queQuan(),
                    t.khach().soCMT(), t.khach().hoTen(), KhachThamUi.NGAY.format(t.khach().ngaySinh()), t.soLan()});
            }

            List<TongKhachTham> b = service.thongKeTheoKhach(k);
            mTheoKhach.setRowCount(0);
            for (TongKhachTham t : b)
                mTheoKhach.addRow(new Object[]{t.khach().soCMT(), t.khach().hoTen(),
                    KhachThamUi.NGAY.format(t.khach().ngaySinh()), t.tongSoLan(), t.soSinhVienDuocTham()});

            List<TongSinhVienDuocTham> c = service.thongKeTheoSinhVien(k);
            mTheoSV.setRowCount(0);
            for (TongSinhVienDuocTham t : c)
                mTheoSV.addRow(new Object[]{t.sinhVien().maSV(), t.sinhVien().lop(), t.sinhVien().queQuan(),
                    t.soLuot(), t.soKhach()});

            tomTat.setText("Kỳ " + k.moTa() + ": " + tongLuot + " lượt thăm của " + b.size() + " khách.");
        });
    }
}
