package ktx.ui;

import ktx.model.KhoangThoiGian;
import ktx.model.ThoiGianXuLyTB;
import ktx.model.ThongKeNhomBaoHong;
import ktx.service.BaoHongService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Tab "Thống kê": sự cố theo loại, mức độ, tháng (theo ngày báo) và thời gian xử lý trung bình. */
class ThongKeBaoHongTab extends JPanel implements KhachThamUi.TaiLai {
    private final BaoHongService service = new BaoHongService();
    private final JTextField tuNgay = new JTextField(9), denNgay = new JTextField(9);
    private final JLabel tomTat = new JLabel(" ");
    private final DefaultTableModel mLoai = bang("Loại sự cố"), mMucDo = bang("Mức độ"), mThang = bang("Tháng");
    private final DefaultTableModel mTb = KhachThamUi.taoBang("Mức độ", "Số báo hỏng đã hoàn thành", "Thời gian xử lý trung bình (giờ)");

    private static DefaultTableModel bang(String nhom) {
        return KhachThamUi.taoBang(nhom, "Tổng số", "Chưa hoàn thành", "Đã hoàn thành", "Từ chối");
    }

    ThongKeBaoHongTab() {
        setLayout(new BorderLayout(8, 8));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton thangNay = new JButton("Tháng này"), thangTruoc = new JButton("Tháng trước"), ba = new JButton("3 tháng gần nhất");
        JButton xem = new JButton("Thống kê");
        bar.add(thangNay); bar.add(thangTruoc); bar.add(ba);
        bar.add(new JLabel("  Từ ngày:")); bar.add(tuNgay); bar.add(new JLabel("Đến ngày:")); bar.add(denNgay); bar.add(xem);
        JPanel bac = new JPanel(new BorderLayout());
        bac.add(bar, BorderLayout.CENTER);
        tomTat.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 0));
        bac.add(tomTat, BorderLayout.SOUTH);
        add(bac, BorderLayout.NORTH);

        JTabbedPane kq = new JTabbedPane();
        kq.addTab("Theo loại sự cố", new JScrollPane(new JTable(mLoai)));
        kq.addTab("Theo mức độ", new JScrollPane(new JTable(mMucDo)));
        kq.addTab("Theo tháng", new JScrollPane(new JTable(mThang)));
        kq.addTab("Thời gian xử lý trung bình", new JScrollPane(new JTable(mTb)));
        add(kq, BorderLayout.CENTER);

        thangNay.addActionListener(e -> datKy(YearMonth.now().atDay(1), YearMonth.now().atEndOfMonth()));
        thangTruoc.addActionListener(e -> { YearMonth t = YearMonth.now().minusMonths(1); datKy(t.atDay(1), t.atEndOfMonth()); });
        ba.addActionListener(e -> datKy(YearMonth.now().minusMonths(2).atDay(1), YearMonth.now().atEndOfMonth()));
        xem.addActionListener(e -> thongKe());
        datKy(YearMonth.now().atDay(1), YearMonth.now().atEndOfMonth());
    }

    @Override public void taiLai() { thongKe(); }

    private void datKy(LocalDate tu, LocalDate den) {
        tuNgay.setText(KhachThamUi.NGAY.format(tu)); denNgay.setText(KhachThamUi.NGAY.format(den)); thongKe();
    }

    private void dien(DefaultTableModel m, List<ThongKeNhomBaoHong> ds) {
        m.setRowCount(0);
        for (ThongKeNhomBaoHong t : ds) m.addRow(new Object[]{t.nhom(), t.tong(), t.chuaHoanThanh(), t.daHoanThanh(), t.tuChoi()});
    }

    private void thongKe() {
        KhachThamUi.chay(this, () -> {
            KhoangThoiGian k = KhoangThoiGian.khoang(KhachThamUi.parseNgay(tuNgay.getText(), "Từ ngày"),
                                                      KhachThamUi.parseNgay(denNgay.getText(), "Đến ngày"));
            List<ThongKeNhomBaoHong> loai = service.thongKeTheoLoai(k);
            dien(mLoai, loai); dien(mMucDo, service.thongKeTheoMucDo(k)); dien(mThang, service.thongKeTheoThang(k));
            mTb.setRowCount(0);
            for (ThoiGianXuLyTB t : service.thoiGianXuLyTheoMucDo(k)) mTb.addRow(new Object[]{t.nhom(), t.soBaoHong(), t.gioTrungBinh()});
            int tong = loai.stream().mapToInt(ThongKeNhomBaoHong::tong).sum();
            var tb = service.thoiGianXuLyTrungBinh(k);
            tomTat.setText("Kỳ " + k.moTa() + ": " + tong + " báo hỏng"
                + tb.map(t -> "; thời gian xử lý trung bình " + t.gioTrungBinh() + " giờ (" + t.soBaoHong() + " báo hỏng đã hoàn thành).")
                    .orElse("; chưa có báo hỏng nào hoàn thành để tính thời gian xử lý trung bình."));
        });
    }
}
