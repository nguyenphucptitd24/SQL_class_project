package ktx.ui;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Tiện ích dùng chung cho các tab của module khách thăm: định dạng ngày giờ, thông báo, bảng. */
final class KhachThamUi {
    static final DateTimeFormatter NGAY =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    static final DateTimeFormatter NGAY_GIO =
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private KhachThamUi() {}

    /** Một tab có thể được làm mới khi người dùng chuyển sang. */
    interface TaiLai { void taiLai(); }

    @FunctionalInterface
    interface HanhDong { void chay() throws Exception; }

    /** Chạy một hành động, báo lỗi bằng hộp thoại nếu thất bại. */
    static void chay(Component cha, HanhDong h) {
        try { h.chay(); } catch (Exception e) { hienLoi(cha, e); }
    }

    static void hienLoi(Component cha, Exception e) {
        String nd = e.getMessage() == null ? e.toString() : e.getMessage();
        if (e instanceof SQLException) nd = "Lỗi cơ sở dữ liệu: " + nd;
        JOptionPane.showMessageDialog(cha, nd, "Thông báo", JOptionPane.ERROR_MESSAGE);
    }

    static void thanhCong(Component cha, String nd) {
        JOptionPane.showMessageDialog(cha, nd, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Bảng chỉ đọc (không cho sửa trực tiếp trên ô). */
    static DefaultTableModel taoBang(String... cot) {
        return new DefaultTableModel(cot, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static LocalDate parseNgay(String s, String ten) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException("Hãy nhập " + ten.toLowerCase() + ".");
        try { return LocalDate.parse(s.trim(), NGAY); }
        catch (DateTimeParseException e) {
            throw new IllegalArgumentException(ten + " không đúng định dạng dd/MM/yyyy (ví dụ 29/10/2026).");
        }
    }

    static LocalDateTime parseNgayGio(String s, String ten) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException("Hãy nhập " + ten.toLowerCase() + ".");
        try { return LocalDateTime.parse(s.trim(), NGAY_GIO); }
        catch (DateTimeParseException e) {
            throw new IllegalArgumentException(ten + " không đúng định dạng dd/MM/yyyy HH:mm (ví dụ 29/10/2026 09:30).");
        }
    }

    static String dinhDang(LocalDateTime t) { return t == null ? "" : NGAY_GIO.format(t); }

    static String bayGio() { return NGAY_GIO.format(LocalDateTime.now()); }

    /** "1 giờ 30 phút", "45 phút"; chưa có giờ về thì ghi "Đang ở trong KTX". */
    static String thoiLuong(LocalDateTime den, LocalDateTime di) {
        if (di == null) return "Đang ở trong KTX";
        long phut = Duration.between(den, di).toMinutes();
        return phut >= 60 ? (phut / 60) + " giờ " + (phut % 60) + " phút" : phut + " phút";
    }
}
