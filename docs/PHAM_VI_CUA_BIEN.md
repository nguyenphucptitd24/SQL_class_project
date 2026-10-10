# Phạm vi công việc cá nhân — Biên

## 1. Phạm vi phụ trách
- Thiết kế CSDL tổng thể và thống nhất khóa chính/khóa ngoại dùng chung.
- Module Sinh viên: xem danh sách, tìm kiếm, thêm, sửa, xóa có kiểm soát.
- Module Phòng: xem danh sách, thêm/sửa phòng, theo dõi số người đang ở và số chỗ trống.
- Module Thuê phòng: xếp phòng, trả phòng, lưu lịch sử thuê.
- Kiểm tra sức chứa phòng, tránh xếp một sinh viên vào nhiều phòng cùng lúc; khóa bản ghi phòng trong transaction khi xếp.
- Chuẩn bị sơ đồ EER và truy vấn hỗ trợ phần cá nhân.

## 2. Các bảng cốt lõi
- `SinhVien(MaSV PK, SoCMT UNIQUE, NgaySinh, Lop, QueQuan)`
- `Phong(SoPhong PK, LoaiPhong, DonGia, SoNguoiToiDa)`
- `ThuePhong(MaThue PK, MaSV FK, SoPhong FK, NgayBatDau, NgayKetThuc)`

`NgayKetThuc IS NULL` được quy ước là hợp đồng đang có hiệu lực. Một sinh viên có nhiều bản ghi thuê theo thời gian, nhưng ứng dụng chỉ cho phép một bản ghi đang hiệu lực. Mỗi phòng có thể có nhiều lượt thuê theo thời gian.

## 3. Các bảng mở rộng để tích hợp nhóm
Schema tổng thể có thêm `DichVu`, `SuDungDichVu`, `XeThang`, `LuotXe`, `Khach`, `LuotTham`. Đây là cấu trúc tích hợp để các thành viên phụ trách module riêng kết nối theo `MaSV`; không có nghĩa Biên phụ trách triển khai giao diện các module đó.

## 4. Quy tắc nghiệp vụ chính
1. Mã sinh viên và số CMT là duy nhất.
2. Đơn giá phòng không âm; sức chứa phải lớn hơn 0.
3. Ngày kết thúc thuê không được trước ngày bắt đầu.
4. Không xếp phòng nếu sinh viên không tồn tại, đã có hợp đồng đang ở, hoặc phòng đã đủ sức chứa.
5. Không giảm sức chứa phòng xuống thấp hơn số người đang ở.
6. Xóa sinh viên/phòng đang được lịch sử thuê tham chiếu sẽ bị khóa ngoại ngăn lại, nhằm bảo toàn lịch sử.
7. Tiền phòng tính tròn tháng: chỉ cần thời gian thuê giao với tháng lập hóa đơn thì tính đủ đơn giá tháng đó. Cần thống nhất thêm quy tắc khi chuyển phòng trong tháng nếu giảng viên yêu cầu chi tiết hơn.

## 5. Chạy chương trình
- Cần JDK 17+, MySQL 8+ và NetBeans có hỗ trợ Maven.
- Chạy `database/01_ktx_schema.sql` trong MySQL Workbench.
- Cấu hình `KTX_DB_URL`, `KTX_DB_USER`, `KTX_DB_PASSWORD` nếu không dùng tài khoản mặc định.
- Mở project Maven `KTXManagement` trong NetBeans và chạy `ktx.ui.MainFrame`.
- Nếu tài khoản MySQL không có quyền `CREATE DATABASE`, chọn schema được cấp bởi giảng viên, bỏ qua `CREATE DATABASE`/`USE KTX` và chạy các lệnh tạo bảng bên dưới.

## 6. Kiểm thử đề xuất
- Thêm sinh viên có mã trùng hoặc số CMT trùng: phải báo lỗi.
- Tìm kiếm theo mã sinh viên, lớp, quê quán.
- Thêm phòng với sức chứa 0 hoặc đơn giá âm: phải bị từ chối.
- Xếp một sinh viên vào phòng còn chỗ: thành công.
- Xếp sinh viên đã có hợp đồng đang ở vào phòng khác: bị từ chối.
- Xếp sinh viên vào phòng đầy: bị từ chối.
- Trả phòng với ngày trước ngày bắt đầu: bị từ chối.
- Giảm sức chứa phòng thấp hơn số người đang ở: bị từ chối ở giao diện.
