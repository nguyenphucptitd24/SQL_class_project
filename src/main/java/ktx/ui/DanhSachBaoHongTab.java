package ktx.ui;

import ktx.model.BaoHong;
import ktx.model.BaoHongDanhMuc;
import ktx.model.BoLocBaoHong;
import ktx.model.KhoangThoiGian;
import ktx.service.BaoHongService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

/** Tab "Danh sách": lọc báo hỏng theo phòng, sinh viên, trạng thái, loại, mức độ, ngày báo. */
class DanhSachBaoHongTab extends JPanel implements KhachThamUi.TaiLai {
    private static final String TAT_CA = "Tất cả";
    private static final String CHUA_XONG = "Chưa hoàn thành";

    private final BaoHongService service = new BaoHongService();
    private final LongConsumer xemChiTiet, capNhatXuLy;
    private final DefaultTableModel model = KhachThamUi.taoBang("Mã", "Phòng", "Người báo", "Loại", "Mức độ",
            "Trạng thái", "Ngày báo", "Ngày xử lý", "Nhân viên", "Thời gian xử lý");
    private final JTable table = new JTable(model);
    private final List<BaoHong> hienTai = new ArrayList<>();

    private final JComboBox<String> phong = new JComboBox<>();
    private final JTextField maSV = new JTextField(8);
    private final JComboBox<String> trangThai = new JComboBox<>(cacLuaChon(CHUA_XONG, BaoHongDanhMuc.TRANG_THAI));
    private final JComboBox<String> loai = new JComboBox<>(cacLuaChon(null, BaoHongDanhMuc.LOAI));
    private final JComboBox<String> mucDo = new JComboBox<>(cacLuaChon(null, BaoHongDanhMuc.MUC_DO));
    private final JTextField tuNgay = new JTextField(9);
    private final JTextField denNgay = new JTextField(9);
    private final JLabel canhBao = new JLabel(" ");
    private boolean uuTienMucDo = false;

    DanhSachBaoHongTab(LongConsumer xemChiTiet, LongConsumer capNhatXuLy) {
        this.xemChiTiet = xemChiTiet; this.capNhatXuLy = capNhatXuLy;
        setLayout(new BorderLayout(8, 8));

        JPanel loc = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        loc.add(new JLabel("Phòng:")); loc.add(phong);
        loc.add(new JLabel("Mã SV:")); loc.add(maSV);
        loc.add(new JLabel("Trạng thái:")); loc.add(trangThai);
        loc.add(new JLabel("Loại:")); loc.add(loai);
        loc.add(new JLabel("Mức độ:")); loc.add(mucDo);
        loc.add(new JLabel("Báo từ ngày:")); loc.add(tuNgay);
        loc.add(new JLabel("đến:")); loc.add(denNgay);
        tuNgay.setToolTipText("dd/MM/yyyy, bỏ trống nếu không lọc theo ngày");
        denNgay.setToolTipText("dd/MM/yyyy, bỏ trống nếu không lọc theo ngày");

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton bLoc = new JButton("Lọc"), bXoa = new JButton("Xóa bộ lọc");
        JButton bMoi = new JButton("Mới tiếp nhận"), bChua = new JButton("Chưa hoàn thành (ưu tiên nặng)");
        JButton bChiTiet = new JButton("Xem chi tiết & lịch sử"), bXuLy = new JButton("Cập nhật xử lý");
        nut.add(bLoc); nut.add(bXoa); nut.add(bMoi); nut.add(bChua); nut.add(bChiTiet); nut.add(bXuLy);

        JPanel bac = new JPanel(new BorderLayout());
        JPanel hai = new JPanel(new GridLayout(2, 1)); hai.add(loc); hai.add(nut);
        bac.add(hai, BorderLayout.NORTH);
        canhBao.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 0));
        bac.add(canhBao, BorderLayout.SOUTH);
        add(bac, BorderLayout.NORTH);

        table.setDefaultRenderer(Object.class, BaoHongUi.boMauCanhBao(hienTai));
        add(new JScrollPane(table), BorderLayout.CENTER);

        bLoc.addActionListener(e -> { uuTienMucDo = false; tai(); });
        maSV.addActionListener(e -> { uuTienMucDo = false; tai(); });
        bXoa.addActionListener(e -> xoaLoc());
        bMoi.addActionListener(e -> { datLoc("Mới báo", true); });
        bChua.addActionListener(e -> { datLoc(CHUA_XONG, true); });
        bChiTiet.addActionListener(e -> chonDong(xemChiTiet));
        bXuLy.addActionListener(e -> chonDong(capNhatXuLy));
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) chonDong(xemChiTiet); }
        });
        napPhong();
        taiLai();
    }

    private static String[] cacLuaChon(String them, List<String> goc) {
        List<String> ds = new ArrayList<>(); ds.add(TAT_CA);
        if (them != null) ds.add(them);
        ds.addAll(goc);
        return ds.toArray(new String[0]);
    }

    private void napPhong() {
        try {
            phong.removeAllItems(); phong.addItem(TAT_CA);
            for (String s : service.danhSachPhong()) phong.addItem(s);
        } catch (Exception e) { KhachThamUi.hienLoi(this, e); }
    }

    @Override public void taiLai() { tai(); }

    private void xoaLoc() {
        phong.setSelectedIndex(0); maSV.setText(""); trangThai.setSelectedIndex(0);
        loai.setSelectedIndex(0); mucDo.setSelectedIndex(0); tuNgay.setText(""); denNgay.setText("");
        uuTienMucDo = false; tai();
    }

    /** Nút chọn nhanh: giữ các bộ lọc khác, đặt trạng thái và (tùy chọn) xếp mức độ nặng lên trước. */
    private void datLoc(String tt, boolean uuTien) {
        trangThai.setSelectedItem(tt); uuTienMucDo = uuTien; tai();
    }

    private void tai() {
        KhachThamUi.chay(this, () -> {
            boolean coTu = !tuNgay.getText().isBlank(), coDen = !denNgay.getText().isBlank();
            if (coTu != coDen) throw new IllegalArgumentException("Hãy nhập đủ ngày bắt đầu và ngày kết thúc, hoặc để trống cả hai.");
            KhoangThoiGian k = coTu ? KhoangThoiGian.khoang(KhachThamUi.parseNgay(tuNgay.getText(), "Ngày bắt đầu"),
                                                             KhachThamUi.parseNgay(denNgay.getText(), "Ngày kết thúc")) : null;
            String tt = (String) trangThai.getSelectedItem();
            BoLocBaoHong f = new BoLocBaoHong(chon(phong), maSV.getText(),
                    TAT_CA.equals(tt) || CHUA_XONG.equals(tt) ? null : tt, CHUA_XONG.equals(tt),
                    chon(loai), chon(mucDo), k == null ? null : k.tu(), k == null ? null : k.den(), uuTienMucDo);
            hienThi(service.timBaoHong(f));
        });
    }

    private String chon(JComboBox<String> cb) {
        String s = (String) cb.getSelectedItem();
        return s == null || TAT_CA.equals(s) ? null : s;
    }

    private void hienThi(List<BaoHong> ds) {
        hienTai.clear(); hienTai.addAll(ds);
        model.setRowCount(0);
        int chuY = 0;
        for (BaoHong b : ds) {
            if (b.canChuY()) chuY++;
            model.addRow(new Object[]{b.maBaoHong(), b.soPhong(), BaoHongUi.nguoiBao(b), b.loaiSuCo(), b.mucDo(), b.trangThai(),
                    KhachThamUi.dinhDang(b.ngayBao()), KhachThamUi.dinhDang(b.ngayXuLy()),
                    b.nhanVienXuLy() == null ? "" : b.nhanVienXuLy(), BaoHongUi.thoiGianXuLy(b)});
        }
        canhBao.setText(ds.size() + " báo hỏng." + (chuY > 0 ? "  CẢNH BÁO: " + chuY
                + " sự cố mức cao/khẩn cấp chưa hoàn thành (dòng tô màu)." : ""));
        canhBao.setForeground(chuY > 0 ? BaoHongUi.CHU_CANH_BAO : Color.DARK_GRAY);
    }

    private void chonDong(LongConsumer hanhDong) {
        int r = table.getSelectedRow();
        if (r < 0) { KhachThamUi.hienLoi(this, new IllegalArgumentException("Hãy chọn một báo hỏng trong bảng.")); return; }
        hanhDong.accept(hienTai.get(r).maBaoHong());
    }
}
