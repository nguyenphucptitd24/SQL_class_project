package ktx.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/**
 * Khoảng thời gian thống kê, dạng nửa mở [tu, den): gồm tu, không gồm den.
 * Lượt thăm được tính vào khoảng nếu giờ ĐẾN nằm trong khoảng.
 */
public record KhoangThoiGian(LocalDateTime tu, LocalDateTime den) {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public KhoangThoiGian {
        if (tu == null || den == null || !tu.isBefore(den))
            throw new IllegalArgumentException("Khoảng thời gian không hợp lệ.");
    }

    /** Tuần chứa ngày đã cho, tính từ Thứ Hai đến Chủ nhật. */
    public static KhoangThoiGian tuan(LocalDate ngayBatKy) {
        if (ngayBatKy == null) throw new IllegalArgumentException("Hãy chọn một ngày trong tuần.");
        LocalDate thuHai = ngayBatKy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new KhoangThoiGian(thuHai.atStartOfDay(), thuHai.plusDays(7).atStartOfDay());
    }

    public static KhoangThoiGian thang(YearMonth thang) {
        if (thang == null) throw new IllegalArgumentException("Hãy chọn tháng.");
        return new KhoangThoiGian(thang.atDay(1).atStartOfDay(), thang.plusMonths(1).atDay(1).atStartOfDay());
    }

    /** Từ ngày đầu đến ngày cuối, gồm cả hai ngày. */
    public static KhoangThoiGian khoang(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null) throw new IllegalArgumentException("Hãy nhập đủ từ ngày và đến ngày.");
        if (denNgay.isBefore(tuNgay)) throw new IllegalArgumentException("Đến ngày không được trước từ ngày.");
        return new KhoangThoiGian(tuNgay.atStartOfDay(), denNgay.plusDays(1).atStartOfDay());
    }

    public LocalDate ngayDau() { return tu.toLocalDate(); }
    public LocalDate ngayCuoi() { return den.minusNanos(1).toLocalDate(); }

    public String moTa() { return NGAY.format(ngayDau()) + " - " + NGAY.format(ngayCuoi()); }
}
