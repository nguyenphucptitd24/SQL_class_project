# Hệ thống quản lý ký túc xá (KTX Management)

Ứng dụng quản lý ký túc xá được xây dựng bằng Java Swing, JDBC và MySQL để hỗ trợ các bài tập lớn về cơ sở dữ liệu và lập trình Java.

## Vai trò thành viên cuối
- Tích hợp các module đã hoàn thiện từ các thành viên khác vào một project thống nhất.
- Kiểm tra đầy đủ về code, cấu trúc package, UI và tính tương thích giữa các module.
- Làm phần giao diện tổng thể, xử lý lỗi tích hợp và chuẩn bị README + hướng dẫn chạy.
- Không cần đợi tất cả thành viên khác hoàn tất code mới bắt đầu; chỉ cần ghép các phần có sẵn và xử lý phần còn thiếu theo mục tiêu dự án.

## 1. Vai trò từng thành viên

### Thành viên Biên
- Thiết kế CSDL tổng thể + module Sinh viên và Phòng ở.
- Chốt quy tắc nghiệp vụ, danh sách bảng, khóa chính và khóa ngoại.
- Vẽ hoặc phối hợp hoàn thiện mô hình EER.
- Làm CRUD sinh viên, phòng ở, quản lý sinh viên thuê phòng.
- Kiểm tra số người tối đa trong phòng.
- Ghép code, kiểm thử toàn hệ thống, quản lý GitHub và hoàn thiện báo cáo.

### Thành viên Mạnh
- Quản lý dịch vụ và hóa đơn.
- Xây dựng bảng dịch vụ và các lần sinh viên sử dụng dịch vụ.
- Ghi nhận số lượng, thời gian, đơn giá áp dụng.
- Tính tổng tiền dịch vụ theo sinh viên và tháng.
- Viết truy vấn doanh thu từng dịch vụ mỗi tháng.
- Làm giao diện ghi nhận dịch vụ và xem hóa đơn.

### Thành viên Thiện
- Quản lý xe tháng.
- Quản lý xe đăng ký vé tháng và chủ xe.
- Theo dõi lượt lấy/gửi xe, tính phí vượt lượt miễn phí.
- Kiểm tra mỗi sinh viên đăng ký tối đa 2 xe.
- Xử lý quy tắc miễn phí 2 lần/ngày và phí 3.000 đồng/lượt phát sinh.
- Làm giao diện quản lý xe và lịch sử gửi xe.

### Thành viên Nam
- Quản lý khách thăm.
- Quản lý thông tin khách đến chơi và sinh viên được thăm.
- Ghi nhận ngày, thời gian đến thăm.
- Truy vấn khách thăm theo tuần/tháng và số lần mỗi khách đến.
- Làm giao diện nhập khách và tra cứu lịch sử.

### Thành viên Phúc
- Giao diện tổng thể và tích hợp.
- Tạo màn hình chính, menu điều hướng và bố cục thống nhất.
- Tích hợp các module của thành viên 1–4.
- Kiểm thử các chức năng, xử lý lỗi giao diện.
- Hỗ trợ chạy thử, hướng dẫn cài đặt và hoàn thiện README GitHub.

## 2. Chức năng chính

### 2.1. Quản lý sinh viên
- Hiển thị danh sách sinh viên.
- Tìm kiếm sinh viên.
- Thêm, sửa, xóa sinh viên.
- Kiểm tra mã sinh viên và số CCCD không trùng lặp.

### 2.2. Quản lý phòng
- Hiển thị danh sách phòng.
- Thêm và sửa phòng.
- Hiển thị sức chứa, số sinh viên đang ở và số chỗ còn trống.
- Kiểm tra giới hạn tối đa người trong phòng.

### 2.3. Quản lý thuê phòng
- Xếp sinh viên vào phòng.
- Trả phòng.
- Chặn điều kiện thuê phòng không hợp lệ.
- Kiểm tra ngày bắt đầu / kết thúc, sức chứa phòng và hợp đồng đang hoạt động.

### 2.4. Module mở rộng
- Dịch vụ và hóa đơn.
- Xe tháng và phí phát sinh.
- Quản lý khách thăm.
- Thống kê và lịch sử hoạt động.

## 3. Công nghệ sử dụng
- Java Swing
- JDBC
- MySQL 8+
- Maven
- JDK 17+
- VS Code + Extension Pack for Java

## 4. Cấu trúc dự án

```text
KTXManagement/
├── database/
│   ├── 01_ktx_schema.sql
│   ├── 02_truy_van_phan_Bien.sql
│   ├── 03_hoa_don_migration.sql
│   ├── 03_khach_tham_phan_Nam.sql
│   ├── 04_truy_van_khach_tham_phan_Nam.sql
│   └── 05_truy_van_xe_thang_thien.sql
├── docs/
├── src/
│   └── main/
│       └── java/
│           └── ktx/
│               ├── database/
│               ├── model/
│               ├── repository/
│               ├── service/
│               └── ui/
├── pom.xml
├── README.md
└── .gitignore
```

## 5. Cài đặt và chạy

### 5.1. Cài đặt môi trường
- JDK 17+
- MySQL Server 8+
- VS Code + Java Extension Pack
- Maven

### 5.2. Kết nối MySQL
Project đọc kết nối MySQL từ biến môi trường nếu có, nếu không thì dùng giá trị mặc định trong file `src/main/java/ktx/database/DatabaseConnection.java`.

#### 4.2.1. Cấu hình theo máy local
Mỗi máy có thể có username/password MySQL khác nhau. Vì vậy nên cấu hình bằng biến môi trường thay vì hardcode mật khẩu cứng vào code.

Ví dụ trên Windows PowerShell:

```powershell
$env:KTX_DB_URL = "jdbc:mysql://localhost:3306/KTX?serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8"
$env:KTX_DB_USER = "root"
$env:KTX_DB_PASSWORD = "your_password"
```

Nếu không dùng biến môi trường, kiểm tra file:

```java
private static final String URL = System.getenv().getOrDefault(
    "KTX_DB_URL",
    "jdbc:mysql://localhost:3306/KTX?serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8"
);
private static final String USER = System.getenv().getOrDefault("KTX_DB_USER", "root");
private static final String PASSWORD = System.getenv().getOrDefault("KTX_DB_PASSWORD", "your_password");
```

Lưu ý: mật khẩu MySQL trên máy khác nhau, nên mỗi máy cần thay `your_password` bằng mật khẩu thực tế của máy đó. Không nên cố dùng chung 1 cài đặt trên mọi máy.

### 5.3. Tạo schema
Mở file SQL trong thư mục `database/` và thực thi theo thứ tự phù hợp.

Với database đã có sẵn, bạn có thể bỏ qua các lệnh `CREATE DATABASE` / `USE KTX` nếu tài khoản MySQL không có quyền tạo schema.

### 5.4. Chạy ứng dụng
Từ thư mục dự án:

```powershell
mvn clean compile
mvn --% org.codehaus.mojo:exec-maven-plugin:3.6.4:java -Dexec.mainClass=ktx.ui.MainFrame -Dexec.classpathScope=runtime
```

Hoặc chạy trực tiếp bằng Java sau khi build xong:

```powershell
mvn dependency:copy-dependencies -DincludeScope=runtime
java -cp "target\classes;target\dependency\*" ktx.ui.MainFrame
```

## 6. Lưu ý
- Nếu tài khoản MySQL không có quyền tạo database, hãy chọn schema sẵn có và bỏ qua các lệnh `CREATE DATABASE` / `USE KTX`.
- Mật khẩu MySQL khác nhau giữa các máy, nên cần cập nhật đúng `KTX_DB_PASSWORD` theo máy đang chạy.
- Không đăng mật khẩu MySQL thật lên GitHub.
- Khi ghép các module khác nhau, ưu tiên xử lý conflict theo cấu trúc package và tên class thống nhất.
- Nếu gặp lỗi `No suitable driver found for jdbc:mysql://...`, cần đảm bảo dependency MySQL Connector/J đã được tải đúng bằng Maven và classpath runtime đã chứa file `.jar`.

**Cách chạy trực tiếp trong VS Code:**

1. Mở `src/main/java/ktx/ui/MainFrame.java`.
2. Tìm phương thức `main`.
3. Nhấn nút **Run** hoặc **Run Java** phía trên phương thức `main`.
4. Chờ ứng dụng Swing mở cửa sổ giao diện.

Nếu lớp `MainFrame` không có phương thức `main`, hãy chạy lớp khởi động thực tế của dự án.

### Bước 6. Xử lý lỗi thường gặp

- **Không tìm thấy MySQL Connector/J:** kiểm tra khai báo dependency trong `pom.xml`, sau đó tải lại dự án Maven.
- **Access denied:** kiểm tra tên tài khoản, mật khẩu và quyền truy cập schema.
- **Unknown database:** kiểm tra tên cơ sở dữ liệu trong MySQL và URL kết nối Java.
- **Communications link failure:** kiểm tra MySQL Server đã khởi động chưa, địa chỉ máy chủ và cổng kết nối.
- **Could not find or load main class:** kiểm tra package, tên lớp chính và cấu trúc thư mục mã nguồn.
- **Không có dữ liệu hiển thị:** kiểm tra dữ liệu mẫu, schema kết nối và các câu truy vấn trong ứng dụng.

## 7. Dữ liệu mẫu

Dự án sử dụng dữ liệu mẫu để phục vụ kiểm thử:

- Mã sinh viên: `SV001`, `SV002`, `SV003`.
- Mã phòng: `P101`, `P102`, `P103`.
- Số CCCD: sử dụng các giá trị giả lập dành riêng cho kiểm thử.

Dữ liệu mẫu không đại diện cho thông tin cá nhân có thật.

## 8. Kiểm thử chức năng

Sau khi khởi động ứng dụng và kết nối cơ sở dữ liệu thành công, thực hiện các trường hợp sau:

1. Mở tab Sinh viên và kiểm tra danh sách sinh viên mẫu.
2. Thêm một sinh viên mới với dữ liệu hợp lệ.
3. Thử thêm sinh viên có mã sinh viên trùng lặp.
4. Thử thêm sinh viên có số CCCD trùng lặp.
5. Sửa thông tin và tìm kiếm sinh viên.
6. Mở tab Phòng, kiểm tra sức chứa, số sinh viên đang ở và số chỗ trống.
7. Thử thêm hoặc sửa thông tin phòng.
8. Thử xếp sinh viên vào phòng còn chỗ.
9. Thử xếp sinh viên vào phòng đã đầy và kiểm tra hệ thống từ chối.
10. Thử xếp lại sinh viên đang có hợp đồng thuê phòng và kiểm tra hệ thống từ chối.
11. Thử trả phòng với ngày trả trước ngày bắt đầu thuê và kiểm tra hệ thống từ chối.
12. Thử xóa sinh viên đang được bảng thuê phòng tham chiếu và kiểm tra cách hệ thống xử lý ràng buộc khóa ngoại.
13. Mở tab Xe vé tháng, đăng ký xe cho sinh viên và thử đăng ký xe thứ ba đang hoạt động cho cùng sinh viên.
14. Ghi nhận thao tác lấy/gửi xe: hai thao tác đầu tiên trong ngày miễn phí, thao tác thứ ba trở đi tính 3.000 VND mỗi lần.
15. Ghi nhận lấy xe vào ngày cuối tháng và gửi xe vào tháng sau; kiểm tra phí xuất hiện đúng tháng của từng thao tác trong lịch sử và thống kê.

Kết quả kiểm thử cần được ghi nhận dựa trên việc chạy ứng dụng thực tế.

## 9. Các ràng buộc và lưu ý

- JDBC sử dụng `PreparedStatement` để truyền tham số vào câu lệnh SQL.
- Nghiệp vụ xếp phòng sử dụng transaction và `SELECT ... FOR UPDATE` để hạn chế xung đột khi nhiều thao tác xếp phòng diễn ra đồng thời.
- Các ràng buộc khóa chính, khóa ngoại và tính duy nhất giúp bảo vệ tính toàn vẹn dữ liệu.
- Cách tính tiền phòng theo tháng, làm tròn ngày, chuyển phòng giữa tháng và miễn giảm cần được thống nhất theo yêu cầu của giảng viên trước khi triển khai chức năng hóa đơn.
- Giao diện CRUD cho các bảng dịch vụ, sử dụng dịch vụ, xe tháng và khách thăm cần được tích hợp thêm nếu thuộc phạm vi yêu cầu cuối cùng của nhóm.

## 10. Đóng góp và phát triển

Dự án có thể tiếp tục được mở rộng theo các hướng:

- Hoàn thiện giao diện cho các module còn thiếu.
- Bổ sung chức năng quản lý hợp đồng và hóa đơn.
- Bổ sung kiểm thử tự động.
- Cải thiện xử lý lỗi và thông báo cho người dùng.
- Hoàn thiện phân quyền và bảo mật thông tin kết nối cơ sở dữ liệu.

---

**Trạng thái dự án:** Cần chạy thử trên môi trường thực tế để xác nhận việc cài đặt, kết nối cơ sở dữ liệu và các chức năng trước khi nghiệm thu.
