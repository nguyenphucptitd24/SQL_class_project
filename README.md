# KTX Management – Hệ thống quản lý ký túc xá

Bài tập lớn môn **Cơ sở dữ liệu** Nhóm 04

Ứng dụng quản lý ký túc xá được xây dựng bằng **Java Swing, JDBC và MySQL**, hỗ trợ quản lý sinh viên, phòng ở, hợp đồng lưu trú, dịch vụ, hóa đơn, xe tháng, khách thăm và báo hỏng/bảo trì phòng.

## 1. Công nghệ sử dụng

- Java (theo phiên bản JDK được thống nhất trong `pom.xml`; Sử dụng JDK 21 do project đang cấu hình `maven.compiler.release=21`).
- Java Swing.
- JDBC – MySQL Connector/J.
- MySQL 8.0+.
- Maven.
- Visual Studio Code.
- MySQL Workbench.

> Note: README cần thống nhất một cách nhất quán với phiên bản JDK với `pom.xml` thực tế. Sẽ sử dụng JDK 21+ cho project 

## 2. Chức năng hệ thống (Ban đầu và thêm mới)

### 2.1. Quản lý sinh viên – Biên

- Hiển thị danh sách sinh viên.
- Tìm kiếm sinh viên theo mã, lớp, quê quán và thông tin phù hợp.
- Thêm, sửa, xóa sinh viên có kiểm tra ràng buộc.
- Đảm bảo mã sinh viên và số CMT/CCCD không bị trùng.

### 2.2. Quản lý phòng – Biên

- Hiển thị danh sách phòng, loại phòng, đơn giá và sức chứa.
- Thêm, cập nhật thông tin phòng.
- Theo dõi số người đang ở và số chỗ còn trống.
- Kiểm tra đơn giá không âm và sức chứa hợp lệ.

### 2.3. Quản lý thuê phòng – Biên

- Xếp sinh viên vào phòng còn chỗ.
- Ghi nhận ngày bắt đầu và ngày kết thúc thuê.
- Trả phòng và lưu lại lịch sử thuê.
- Không cho phép một sinh viên có nhiều lượt thuê đang hiệu lực cùng lúc.
- Kiểm tra sức chứa trước khi xếp phòng; sử dụng transaction khi cần để tránh hai thao tác đồng thời vượt sức chứa.

### 2.4. Quản lý dịch vụ và sử dụng dịch vụ – Mạnh

- Quản lý danh sách dịch vụ của ký túc xá.
- Ghi nhận sinh viên sử dụng dịch vụ, số lượng, thời điểm và đơn giá áp dụng.
- Tra cứu lịch sử sử dụng dịch vụ.
- Tổng hợp chi phí dịch vụ theo sinh viên và tháng.

### 2.5. Quản lý hóa đơn tháng – Mạnh (chức năng mới)

- Lập hóa đơn theo sinh viên/phòng và kỳ thanh toán.
- Tổng hợp tiền phòng, tiền điện, tiền nước và tiền dịch vụ.
- Tính tổng tiền phải thanh toán.
- Theo dõi trạng thái đã thanh toán/chưa thanh toán.
- Ghi nhận ngày lập hóa đơn và ngày thanh toán.
- Tra cứu hóa đơn theo mã sinh viên, phòng, tháng và trạng thái thanh toán.

Các khoản phí phải được lấy từ dữ liệu nghiệp vụ tương ứng. Không nhập lại thủ công tổng tiền dịch vụ nếu đã có dữ liệu trong `SuDungDichVu`.

### 2.6. Quản lý xe tháng – Thiện

- Quản lý xe đăng ký vé tháng và sinh viên sở hữu.
- Ghi nhận lịch sử lấy/gửi xe.
- Kiểm tra giới hạn tối đa 2 xe đang đăng ký trên mỗi sinh viên.
- Áp dụng quy tắc miễn phí 2 lượt/ngày; các lượt vượt mức tính 3.000 đồng/lượt theo quy định nhóm đã thống nhất.
- Thống kê lượt xe và phí phát sinh theo sinh viên/tháng.

### 2.7. Quản lý lưu trú và trạng thái ở – Thiện (chức năng mới)

- Theo dõi tình trạng lưu trú của sinh viên: đang ở, đã trả phòng, chờ duyệt hoặc hết hạn lưu trú.
- Theo dõi trạng thái hợp đồng thuê: chờ duyệt, đang hiệu lực, đã kết thúc hoặc hết hạn.
- Quản lý thông tin ngày ký hợp đồng, ngày bắt đầu, ngày hết hạn và tiền cọc.
- Tra cứu sinh viên đang ở theo phòng và danh sách sinh viên đã trả phòng.
- Đồng bộ trạng thái lưu trú với dữ liệu thuê phòng; không tự ý sửa số người hiện tại nếu hệ thống đã tính từ hợp đồng đang hiệu lực.

### 2.8. Quản lý khách thăm – Nam

- Lưu thông tin khách đến thăm.
- Ghi nhận sinh viên được thăm và từng lượt thăm.
- Tra cứu lịch sử khách thăm theo sinh viên, khách hoặc thời gian.
- Thống kê số lượt khách thăm theo ngày/tháng.

### 2.9. Quản lý báo hỏng và bảo trì phòng – Nam (chức năng mới)

- Sinh viên gửi báo cáo sự cố của phòng đang lưu trú.
- Ghi nhận loại sự cố, mô tả, mức độ và thời điểm báo.
- Theo dõi trạng thái xử lý: mới báo, đang xử lý, đã hoàn thành hoặc từ chối.
- Ghi nhận nhân viên xử lý và ngày xử lý.
- Tra cứu lịch sử báo hỏng/bảo trì theo phòng và trạng thái.

## 3. Công nghệ và cấu trúc dự án

Cấu trúc package Java dự kiến demo :

```text
src/main/java/ktx/
├── database/
├── model/
├── repository/
├── service/
└── ui/
pomxl
Readme


```text
database/
└── 01_ktx_schema.sql
└── 02_truy_van.sql
└── 03.sql
└── 04.sql
......
src/main/java/ktx/
pom.xml
README.md
docs/
└── task-breakdown.md
```

Tên package hoặc đường dẫn thực tế có thể khác tùy cấu trúc source hiện tại; sản phẩm được thống nhất tên gọi trong link github prj 

## 4. Các bảng dữ liệu chính

### Nhóm cốt lõi

- `SinhVien`
- `Phong`
- `ThuePhong`

### Nhóm dịch vụ và hóa đơn

- `DichVu`
- `SuDungDichVu`
- `HoaDon`

### Nhóm xe tháng

- `XeThang`
- `LuotXe`

### Nhóm khách thăm

- `Khach`
- `LuotTham`

### Nhóm báo hỏng/bảo trì

- `BaoHong`

Các cột mở rộng về trạng thái lưu trú/hợp đồng có thể được bổ sung vào `SinhVien`, `Phong` và `ThuePhong` sau khi nhóm rà soát schema hiện tại. Không tạo cột trùng với cột đã tồn tại.

## 5. Quy tắc nghiệp vụ quan trọng

1. Mã sinh viên và số CMT/CCCD phải duy nhất.
2. Đơn giá phòng không âm; sức chứa phòng phải lớn hơn 0.
3. Ngày kết thúc thuê không được trước ngày bắt đầu thuê.
4. Một sinh viên chỉ có tối đa một hợp đồng thuê đang hiệu lực tại một thời điểm.
5. Không xếp sinh viên vào phòng đã đủ sức chứa.
6. Không giảm sức chứa xuống thấp hơn số người đang ở.
7. Không xóa dữ liệu sinh viên/phòng đang được lịch sử thuê tham chiếu nếu việc xóa làm mất tính toàn vẹn dữ liệu.
8. Tiền phòng tính tròn tháng theo quy tắc nhóm thống nhất: nếu thời gian thuê giao với tháng lập hóa đơn thì tính đủ đơn giá tháng đó. Trường hợp chuyển phòng trong tháng cần quy định rõ cách tính.
9. Hóa đơn cần có kỳ thanh toán rõ ràng và không được lập trùng cho cùng đối tượng/kỳ theo quy tắc khóa duy nhất nhóm thống nhất.
10. Tổng tiền hóa đơn phải bằng tổng các khoản thành phần; trạng thái thanh toán và ngày thanh toán phải nhất quán.
11. Báo hỏng phải gắn với phòng hợp lệ; nếu gắn với sinh viên thì cần kiểm tra sinh viên có liên quan đến phòng tại thời điểm báo.
12. Việc cập nhật trạng thái phòng sau sự cố phải phân biệt tình trạng kỹ thuật với trạng thái sử dụng/đang có người ở.

## 6. Cài đặt và chạy chương trình

1. Cài JDK phù hợp với cấu hình Maven trong `pom.xml`.
2. Cài MySQL 8.0+ và mở MySQL Workbench.
3. Chạy script `database/01_ktx_schema.sql` trên schema được cấp.
4. Cấu hình thông tin kết nối database bằng biến môi trường hoặc cấu hình riêng:
   - `KTX_DB_URL`
   - `KTX_DB_USER`
   - `KTX_DB_PASSWORD`
5. Mở project Maven bằng VS Code.
6. Tải dependency Maven và chạy lớp main `ktx.ui.MainFrame` (nếu đây vẫn là lớp main trong source hiện tại).

Nếu tài khoản MySQL không có quyền `CREATE DATABASE`, chọn schema do giảng viên cấp, bỏ qua lệnh `CREATE DATABASE`/`USE KTX` và chạy các lệnh tạo bảng trong schema đó.

**Không commit mật khẩu database cá nhân lên GitHub.**

## 7. Phân công

| Thành viên | Module cũ / nền tảng | Module bổ sung |
|---|---|---|
| Biên | Sinh viên, Phòng, Thuê phòng; thiết kế CSDL cốt lõi | Điều phối schema và tích hợp dữ liệu cốt lõi |
| Mạnh | Dịch vụ, Sử dụng dịch vụ | Quản lý hóa đơn tháng |
| Thiện | Xe tháng, lượt xe | Quản lý lưu trú và trạng thái ở |
| Nam | Khách thăm, lượt thăm | Báo hỏng và bảo trì phòng |

Chi tiết đầu việc, truy vấn, giao diện và tiêu chí bàn giao xem tại [`docs/task-breakdown.md`](docs/task-breakdown.md).

## 8. Kiểm thử đề xuất

- Thêm sinh viên trùng mã hoặc số CMT/CCCD: phải từ chối.
- Thêm phòng có sức chứa không hợp lệ hoặc đơn giá âm: phải từ chối.
- Xếp sinh viên vào phòng còn chỗ: thành công.
- Xếp sinh viên đã có hợp đồng hiệu lực hoặc xếp vào phòng đầy: phải từ chối.
- Trả phòng với ngày kết thúc trước ngày bắt đầu: phải từ chối.
- Lập hóa đơn: tổng tiền phải khớp các khoản phòng, điện, nước, dịch vụ.
- Lập hóa đơn trùng kỳ cho cùng đối tượng: phải được kiểm soát theo quy tắc nhóm.
- Thanh toán hóa đơn: trạng thái và ngày thanh toán được cập nhật đúng.
- Gửi báo hỏng: lưu đúng phòng, sinh viên, loại sự cố và thời điểm.
- Cập nhật bảo trì: trạng thái và thông tin nhân viên xử lý được lưu đúng.
- Sinh viên trả phòng: trạng thái lưu trú/hợp đồng được cập nhật đồng bộ.
- Xe thứ ba đang hoạt động của cùng sinh viên: phải từ chối nếu giới hạn 2 xe vẫn áp dụng.

> Danh sách trên là kịch bản kiểm thử đề xuất. Chỉ đánh dấu “Đạt” sau khi chạy trên source code và database thực tế.
> Kết quả sản phẩn được hoàn thành sau khi đánh giá một cách tổng quan và chính xác nhất 