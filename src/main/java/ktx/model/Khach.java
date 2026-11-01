package ktx.model;

import java.time.LocalDate;

/** Khách đến thăm sinh viên trong KTX. Nhận diện duy nhất bằng số CMT/CCCD. */
public record Khach(String soCMT, String hoTen, LocalDate ngaySinh) {}
