package ktx.ui;

import ktx.model.BaoHong;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.time.Duration;
import java.util.List;

/** Tiện ích riêng của các tab báo hỏng: định dạng và tô màu cảnh báo. */
final class BaoHongUi {
    private BaoHongUi() {}

    static final Color NEN_KHAN_CAP = new Color(0xFEE2E2);   // đỏ nhạt: Khẩn cấp chưa xong
    static final Color NEN_CAO = new Color(0xFFEDD5);        // cam nhạt: Cao chưa xong
    static final Color CHU_CANH_BAO = new Color(0xB91C1C);

    static String nguoiBao(BaoHong b) { return b.maSV() != null ? b.maSV() : "Nhân viên phát hiện"; }

    /** "35 phút", "6 giờ 30 phút", "2 ngày 12 giờ"; chưa kết thúc thì để trống. */
    static String thoiGianXuLy(BaoHong b) {
        Duration d = b.thoiGianXuLy();
        if (d == null) return "";
        long phut = Math.max(0, d.toMinutes());
        if (phut < 60) return phut + " phút";
        long gio = phut / 60;
        if (gio < 24) return gio + " giờ " + (phut % 60) + " phút";
        return (gio / 24) + " ngày " + (gio % 24) + " giờ";
    }

    /** Tô nền các dòng sự cố cao/khẩn cấp chưa xử lý xong; hang(i) cho biết báo hỏng ở dòng i của bảng. */
    static DefaultTableCellRenderer boMauCanhBao(List<BaoHong> hang) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    comp.setBackground(t.getBackground());
                    comp.setForeground(t.getForeground());
                    if (r >= 0 && r < hang.size() && hang.get(r).canChuY()) {
                        comp.setBackground("Khẩn cấp".equals(hang.get(r).mucDo()) ? NEN_KHAN_CAP : NEN_CAO);
                    }
                }
                return comp;
            }
        };
    }
}
