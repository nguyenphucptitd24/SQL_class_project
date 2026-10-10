package ktx.model;

import java.time.LocalDate;

public record SinhVien(String maSV, String soCMT, LocalDate ngaySinh,
                       String lop, String queQuan) {}
