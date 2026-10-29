# Task BreakDown ( Phân Chia Nhiệm vụ) : 
## BIÊN : 
###  1. Phạm vi phụ trách
- Thiết kế CSDL tổng thể và thống nhất khóa chính/khóa ngoại dùng chung.
- Module Sinh viên: xem danh sách, tìm kiếm, thêm, sửa, xóa có kiểm soát.
- Module Phòng: xem danh sách, thêm/sửa phòng, theo dõi số người đang ở và số chỗ trống.
- Module Thuê phòng: xếp phòng, trả phòng, lưu lịch sử thuê.
- Kiểm tra sức chứa phòng, tránh xếp một sinh viên vào nhiều phòng cùng lúc; khóa bản ghi phòng trong transaction khi xếp.
- Chuẩn bị sơ đồ EER và truy vấn hỗ trợ phần cá nhân.

### 2. Các bảng cốt lõi
- `SinhVien(MaSV PK, SoCMT UNIQUE, NgaySinh, Lop, QueQuan)`
- `Phong(SoPhong PK, LoaiPhong, DonGia, SoNguoiToiDa)`
- `ThuePhong(MaThue PK, MaSV FK, SoPhong FK, NgayBatDau, NgayKetThuc)`

`NgayKetThuc IS NULL` được quy ước là hợp đồng đang có hiệu lực. Một sinh viên có nhiều bản ghi thuê theo thời gian, nhưng ứng dụng chỉ cho phép một bản ghi đang hiệu lực. Mỗi phòng có thể có nhiều lượt thuê theo thời gian.

### 3. Các bảng mở rộng để tích hợp nhóm
Schema tổng thể có thêm `DichVu`, `SuDungDichVu`, `XeThang`, `LuotXe`, `Khach`, `LuotTham`. Đây là cấu trúc tích hợp để các thành viên phụ trách module riêng kết nối theo `MaSV`; không có nghĩa Biên phụ trách triển khai giao diện các module đó.

### 4. Quy tắc nghiệp vụ chính
1. Mã sinh viên và số CMT là duy nhất.
2. Đơn giá phòng không âm; sức chứa phải lớn hơn 0.
3. Ngày kết thúc thuê không được trước ngày bắt đầu.
4. Không xếp phòng nếu sinh viên không tồn tại, đã có hợp đồng đang ở, hoặc phòng đã đủ sức chứa.
5. Không giảm sức chứa phòng xuống thấp hơn số người đang ở.
6. Xóa sinh viên/phòng đang được lịch sử thuê tham chiếu sẽ bị khóa ngoại ngăn lại, nhằm bảo toàn lịch sử.
7. Tiền phòng tính tròn tháng: chỉ cần thời gian thuê giao với tháng lập hóa đơn thì tính đủ đơn giá tháng đó. Cần thống nhất thêm quy tắc khi chuyển phòng trong tháng nếu giảng viên yêu cầu chi tiết hơn.

### 5. Chạy chương trình
- Cần JDK 17+, MySQL 8+ và NetBeans có hỗ trợ Maven.
- Chạy `database/01_ktx_schema.sql` trong MySQL Workbench.
- Cấu hình `KTX_DB_URL`, `KTX_DB_USER`, `KTX_DB_PASSWORD` nếu không dùng tài khoản mặc định.
- Mở project Maven `KTXManagement` trong NetBeans và chạy `ktx.ui.MainFrame`.
- Nếu tài khoản MySQL không có quyền `CREATE DATABASE`, chọn schema được cấp bởi giảng viên, bỏ qua `CREATE DATABASE`/`USE KTX` và chạy các lệnh tạo bảng bên dưới.

### 6. Kiểm thử đề xuất
- Thêm sinh viên có mã trùng hoặc số CMT trùng: phải báo lỗi.
- Tìm kiếm theo mã sinh viên, lớp, quê quán.
- Thêm phòng với sức chứa 0 hoặc đơn giá âm: phải bị từ chối.
- Xếp một sinh viên vào phòng còn chỗ: thành công.
- Xếp sinh viên đã có hợp đồng đang ở vào phòng khác: bị từ chối.
- Xếp sinh viên vào phòng đầy: bị từ chối.
- Trả phòng với ngày trước ngày bắt đầu: bị từ chối.
- Giảm sức chứa phòng thấp hơn số người đang ở: bị từ chối ở giao diện.
# TASK BREAKDOWN — KTX MANAGEMENT

## 1. Phân CHia 3 modul và 1 tổng hợp 

Phân chia công việc cho 4 thành viên phụ trách các module mở rộng của hệ thống quản lý ký túc xá, sử dụng Java Swing, JDBC và MySQL.

Mỗi thành viên chịu trách nhiệm thiết kế CSDL liên quan, xử lý nghiệp vụ, viết truy vấn SQL, phát triển giao diện và kiểm thử module của mình.

## 2. Phân công công việc

### Thành viên 2 — Mạnh: Quản lý dịch vụ và hóa đơn

**Mục tiêu:** Xây dựng chức năng quản lý dịch vụ, ghi nhận việc sử dụng dịch vụ và tổng hợp tiền dịch vụ theo tháng.

**Task 2.1 — Thiết kế cơ sở dữ liệu**
- Xây dựng bảng `DichVu` lưu thông tin dịch vụ.
- Xây dựng bảng `SuDungDichVu` lưu lịch sử sử dụng dịch vụ của sinh viên.
- Thiết kế khóa chính, khóa ngoại và các ràng buộc dữ liệu phù hợp.
- Lưu lại đơn giá áp dụng tại thời điểm sử dụng dịch vụ để bảo toàn lịch sử tính tiền.

**Task 2.2 — Xử lý nghiệp vụ**
- Thêm, sửa, xóa và tra cứu dịch vụ theo các quy tắc đã thống nhất.
- Ghi nhận mã sinh viên, mã dịch vụ, số lượng, thời gian sử dụng và đơn giá áp dụng.
- Tính thành tiền từng lần sử dụng dịch vụ.
- Tổng hợp tiền dịch vụ theo từng sinh viên trong từng tháng.
- Kiểm tra dữ liệu đầu vào hợp lệ.

**Task 2.3 — Truy vấn SQL**
- Truy vấn lịch sử sử dụng dịch vụ của sinh viên.
- Tính tổng tiền dịch vụ của từng sinh viên theo tháng.
- Thống kê doanh thu từng dịch vụ theo tháng.
- Kiểm tra kết quả truy vấn với dữ liệu mẫu.

**Task 2.4 — Giao diện Java Swing**
- Xây dựng giao diện quản lý danh sách dịch vụ.
- Xây dựng giao diện ghi nhận sinh viên sử dụng dịch vụ.
- Xây dựng giao diện xem chi tiết và tổng tiền dịch vụ theo tháng.
- Hiển thị dữ liệu và thông báo lỗi rõ ràng.

**Sản phẩm bàn giao**
- Script SQL tạo bảng và dữ liệu mẫu.
- Các lớp Model, Repository/DAO, Service và giao diện liên quan.
- Các truy vấn thống kê doanh thu.
- Hướng dẫn sử dụng và kết quả kiểm thử module.

---

### Thành viên 3 — Thiện: Quản lý xe tháng

**Mục tiêu:** Quản lý thông tin xe đăng ký vé tháng, lịch sử gửi xe và tính phí phát sinh.

**Task 3.1 — Thiết kế cơ sở dữ liệu**
- Xây dựng bảng lưu thông tin xe đăng ký vé tháng và chủ xe.
- Xây dựng bảng ghi nhận lịch sử lấy/gửi xe.
- Thiết kế khóa chính, khóa ngoại và ràng buộc dữ liệu.
- Liên kết thông tin xe với sinh viên đăng ký.

**Task 3.2 — Xử lý nghiệp vụ**
- Thêm, sửa, tra cứu thông tin xe và vé tháng.
- Ghi nhận lịch sử lấy/gửi xe theo ngày, giờ.
- Kiểm tra mỗi sinh viên được đăng ký tối đa 2 xe.
- Áp dụng quy tắc miễn phí 2 lượt mỗi ngày và tính 3.000 đồng cho mỗi lượt phát sinh vượt mức miễn phí.
- Kiểm tra lượt xe và tránh ghi nhận dữ liệu không hợp lệ.
- Tổng hợp phí phát sinh theo sinh viên và thời gian.

**Task 3.3 — Truy vấn SQL**
- Tra cứu xe theo sinh viên.
- Xem lịch sử lấy/gửi xe theo ngày hoặc tháng.
- Thống kê số lượt miễn phí và lượt tính phí.
- Tính tổng phí phát sinh theo sinh viên trong tháng.

**Task 3.4 — Giao diện Java Swing**
- Xây dựng giao diện quản lý xe đăng ký vé tháng.
- Xây dựng giao diện ghi nhận lượt lấy/gửi xe.
- Xây dựng giao diện tra cứu lịch sử và phí phát sinh.
- Hiển thị thông báo khi vượt giới hạn đăng ký hoặc phát sinh phí.

**Sản phẩm bàn giao**
- Script SQL tạo bảng và dữ liệu mẫu.
- Các lớp Model, Repository/DAO, Service và giao diện liên quan.
- Các truy vấn thống kê lượt xe và phí phát sinh.
- Hướng dẫn sử dụng và kết quả kiểm thử module.

**Lưu ý:** Nhóm phải thống nhất cách xác định một lượt xe, thời điểm tính phí và cách xử lý lượt gửi xe qua ngày trước khi triển khai.

---

### Thành viên 4 — Nam: Quản lý khách thăm

**Mục tiêu:** Quản lý thông tin khách đến thăm và lịch sử thăm sinh viên trong ký túc xá.

**Task 4.1 — Thiết kế cơ sở dữ liệu**
- Xây dựng bảng lưu thông tin khách thăm.
- Xây dựng bảng ghi nhận từng lần khách đến thăm.
- Liên kết lần thăm với sinh viên được thăm.
- Thiết kế khóa chính, khóa ngoại và các ràng buộc dữ liệu phù hợp.

**Task 4.2 — Xử lý nghiệp vụ**
- Thêm và tra cứu thông tin khách thăm.
- Ghi nhận sinh viên được thăm, ngày và thời gian đến.
- Hiển thị lịch sử các lần thăm.
- Kiểm tra dữ liệu bắt buộc và định dạng thời gian.
- Hỗ trợ tìm kiếm theo thông tin khách hoặc sinh viên.

**Task 4.3 — Truy vấn SQL**
- Tra cứu lịch sử thăm một sinh viên.
- Thống kê khách thăm theo tuần và tháng.
- Đếm số lần mỗi khách đến thăm.
- Thống kê số lượt khách đến thăm theo từng sinh viên hoặc khoảng thời gian.

**Task 4.4 — Giao diện Java Swing**
- Xây dựng giao diện nhập thông tin khách.
- Xây dựng giao diện ghi nhận lượt thăm.
- Xây dựng giao diện tra cứu lịch sử.
- Hiển thị kết quả tìm kiếm và thống kê.

**Sản phẩm bàn giao**
- Script SQL tạo bảng và dữ liệu mẫu.
- Các lớp Model, Repository/DAO, Service và giao diện liên quan.
- Các truy vấn thống kê khách thăm.
- Hướng dẫn sử dụng và kết quả kiểm thử module.

---

### Thành viên 5 — Phúc: Giao diện tổng thể và tích hợp

**Mục tiêu:** Xây dựng màn hình chính, thống nhất cách điều hướng và tích hợp các module thành một ứng dụng hoàn chỉnh.

**Task 5.1 — Thiết kế giao diện tổng thể**
- Xây dựng màn hình chính của ứng dụng.
- Thiết kế menu hoặc thanh điều hướng giữa các module.
- Thống nhất bố cục, tiêu đề, bảng dữ liệu, nút chức năng và thông báo.
- Đảm bảo giao diện dễ sử dụng và nhất quán.

**Task 5.2 — Tích hợp module**
- Tích hợp module quản lý sinh viên và phòng của trưởng nhóm.
- Tích hợp module dịch vụ và hóa đơn của Mạnh.
- Tích hợp module xe tháng của Thiện.
- Tích hợp module khách thăm của Nam.
- Kết nối các màn hình với lớp nghiệp vụ tương ứng.
- Kiểm tra tính tương thích giữa các module và cơ sở dữ liệu chung.

**Task 5.3 — Kiểm thử tổng thể**
- Kiểm tra chức năng của từng module sau tích hợp.
- Kiểm tra luồng dữ liệu giữa các bảng có liên quan.
- Kiểm tra các trường hợp dữ liệu sai, thiếu hoặc trùng lặp.
- Phát hiện và phối hợp sửa lỗi giao diện, lỗi kết nối và lỗi tích hợp.
- Kiểm tra khả năng chạy ứng dụng từ đầu trên môi trường phát triển.

**Task 5.4 — Hoàn thiện tài liệu**
- Hỗ trợ hoàn thiện hướng dẫn cài đặt và chạy dự án.
- Cập nhật README.md trên GitHub theo trạng thái thực tế.
- Hướng dẫn cách cấu hình MySQL và chạy ứng dụng bằng VS Code.
- Tổng hợp các chức năng đã hoàn thành, còn thiếu và kết quả kiểm thử.

**Sản phẩm bàn giao**
- Màn hình chính và hệ thống điều hướng.
- Các module đã tích hợp.
- Danh sách lỗi và kết quả kiểm thử tổng thể.
- README.md và hướng dẫn chạy ứng dụng đã cập nhật.

## 3. Quy ước phối hợp và tích hợp

### 3.1. Quy ước cơ sở dữ liệu
- Thống nhất tên bảng, tên cột, kiểu dữ liệu, khóa chính và khóa ngoại trước khi tích hợp.
- Các module phải dùng chung schema cơ sở dữ liệu KTX.
- Không tự ý sửa hoặc xóa bảng do thành viên khác phụ trách khi chưa trao đổi.
- Mọi thay đổi cấu trúc CSDL phải được cập nhật trong script SQL và thông báo cho cả nhóm.
- Không lưu mật khẩu MySQL cá nhân trong mã nguồn đưa lên GitHub.

### 3.2. Quy ước mã nguồn
- Mỗi thành viên phát triển module trong nhánh Git riêng.
- Không tự ý đẩy trực tiếp lên nhánh `main`.
- Hoàn thành chức năng và kiểm thử cơ bản trước khi tạo Pull Request.
- Không chỉnh sửa trực tiếp file của thành viên khác nếu chưa thống nhất.
- Sử dụng `PreparedStatement` cho truy vấn có tham số.
- Tách biệt Model, Repository/DAO, Service và giao diện khi phù hợp với cấu trúc dự án.

### 3.3. Quy ước giao diện
- Các module dùng chung phong cách giao diện.
- Giao diện gọi lớp Service để xử lý nghiệp vụ, tránh viết truy vấn SQL trực tiếp trong các thành phần giao diện.
- Thống nhất cách hiển thị thông báo thành công, thất bại và lỗi nhập liệu.
- Không tích hợp module khi chưa xác định rõ cách khởi tạo và gọi màn hình.

## 4. Thứ tự thực hiện đề xuất

1. **Giai đoạn 1 — Thống nhất thiết kế:** Chốt bảng dữ liệu, quan hệ, quy tắc nghiệp vụ và cấu trúc lớp.
2. **Giai đoạn 2 — Phát triển độc lập:** Mạnh, Thiện và Nam triển khai các module riêng; Phúc chuẩn bị màn hình chính và quy ước giao diện.
3. **Giai đoạn 3 — Kiểm thử module:** Mỗi thành viên tự kiểm thử bằng dữ liệu mẫu và sửa lỗi.
4. **Giai đoạn 4 — Tích hợp:** Tạo Pull Request, rà soát thay đổi và tích hợp vào nhánh chung.
5. **Giai đoạn 5 — Kiểm thử tổng thể:** Kiểm tra CSDL, giao diện, nghiệp vụ và các trường hợp lỗi.
6. **Giai đoạn 6 — Hoàn thiện:** Cập nhật README.md, hướng dẫn chạy ứng dụng và chuẩn bị báo cáo.

## 5. Tiêu chí hoàn thành

Một module được xem là hoàn thành khi:
- Có thiết kế CSDL và script SQL
