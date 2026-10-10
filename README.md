# Hệ thống quản lý ký túc xá (KTX Management)

Ứng dụng quản lý ký túc xá được xây dựng bằng **Java Swing, JDBC và MySQL**, phục vụ bài tập lớn môn Cơ sở dữ liệu.

## 1. Chức năng của hệ thống

### 1.1. Quản lý sinh viên
- Hiển thị danh sách sinh viên.
- Tìm kiếm sinh viên.
- Thêm sinh viên mới.
- Sửa thông tin sinh viên.
- Xóa sinh viên khi đáp ứng các ràng buộc của cơ sở dữ liệu.
- Kiểm tra mã sinh viên và số CCCD không bị trùng lặp.

### 1.2. Quản lý phòng
- Hiển thị danh sách phòng.
- Thêm phòng mới.
- Sửa thông tin phòng.
- Hiển thị sức chứa, số sinh viên đang ở và số chỗ còn trống.

### 1.3. Quản lý thuê phòng
- Xếp sinh viên vào phòng.
- Trả phòng.
- Kiểm tra sức chứa trước khi xếp phòng.
- Ngăn sinh viên đang có hợp đồng thuê phòng được xếp sang phòng khác khi chưa trả phòng.
- Kiểm tra ngày trả phòng hợp lệ.

### 1.4. Các bảng dữ liệu mở rộng

Cơ sở dữ liệu có thêm các bảng phục vụ việc phát triển những chức năng sau:
- Quản lý dịch vụ.
- Theo dõi việc sử dụng dịch vụ.
- Quản lý xe tháng.
- Quản lý khách thăm.

**Lưu ý:** Các bảng dữ liệu mở rộng đã được chuẩn bị trong cơ sở dữ liệu. Giao diện CRUD cho các module này cần được tích hợp thêm trước khi sử dụng.

## 2. Công nghệ sử dụng

- Ngôn ngữ lập trình: Java.
- Giao diện: Java Swing.
- Kết nối cơ sở dữ liệu: JDBC.
- Hệ quản trị cơ sở dữ liệu: MySQL 8.0 trở lên.
- Công cụ lập trình: Visual Studio Code (VS Code).
- Công cụ quản lý cơ sở dữ liệu: MySQL Workbench.
- Quản lý thư viện: Maven.
- Phiên bản Java: JDK 17 trở lên.

## 3. Cấu trúc dự án

Dự án có cấu trúc tổng quát như sau:

```text
KTXManagement/
├── database/
│   └── 01_ktx_schema.sql
├── src/
│   └── main/
│       └── java/
│           └── ktx/
│               ├── ui/
│               │   └── MainFrame.java
│               └── ...
├── pom.xml
└── README.md
```

Cấu trúc thực tế có thể có thêm các thư mục `model`, `repository`, `service` và những thành phần khác tùy theo mã nguồn của dự án.

## 4. Cài đặt môi trường

Trước khi chạy dự án, cần cài đặt:

1. **JDK 17 trở lên:** dùng để biên dịch và chạy Java.
2. **Visual Studio Code:** dùng để mở và phát triển mã nguồn.
3. **Extension Pack for Java:** hỗ trợ Java trong VS Code.
4. **Maven for Java:** hỗ trợ quản lý dự án Maven.
5. **MySQL Server 8.0 trở lên:** lưu trữ dữ liệu.
6. **MySQL Workbench:** tạo cơ sở dữ liệu và thực thi câu lệnh SQL.

Kiểm tra Java và Maven bằng Terminal:

```bash
java -version
javac -version
mvn -version
```

Nếu các lệnh không hoạt động, hãy kiểm tra việc cài đặt và cấu hình biến môi trường `PATH`.

## 5. Cài đặt cơ sở dữ liệu MySQL

### Bước 1. Kết nối MySQL

Mở MySQL Workbench và kết nối đến MySQL Server đang sử dụng.

### Bước 2. Mở file SQL

Mở file:

```text
database/01_ktx_schema.sql
```

### Bước 3. Tạo cơ sở dữ liệu và các bảng

Nếu tài khoản MySQL có quyền tạo cơ sở dữ liệu, chạy script theo thứ tự từ đầu đến cuối.

Script tạo schema KTX, các bảng sinh viên, phòng, thuê phòng và những bảng mở rộng, đồng thời thêm dữ liệu mẫu để kiểm thử.

Nếu gặp lỗi:

```text
ERROR 1044: Access denied
```

Tài khoản hiện tại có thể không có quyền truy cập hoặc tạo cơ sở dữ liệu được yêu cầu.

Nếu giảng viên đã cấp sẵn một schema, hãy:
1. Bỏ qua câu lệnh `CREATE DATABASE` và `USE KTX` trong script.
2. Chọn schema được cấp.
3. Chạy phần tạo bảng và dữ liệu mẫu phù hợp với schema đó.
4. Cập nhật tên schema trong cấu hình kết nối Java nếu cần.

Không xóa hoặc sửa các câu lệnh khác nếu chưa kiểm tra sự phụ thuộc giữa chúng.

### Bước 4. Kiểm tra dữ liệu

Chạy:

```sql
SHOW TABLES;
```

Nếu script đã tạo view `V_SinhVienDangO`, kiểm tra bằng:

```sql
SELECT * FROM V_SinhVienDangO;
```

Nếu truy vấn thành công, tiếp tục cấu hình ứng dụng Java.

## 6. Cấu hình kết nối MySQL

Mở file `DatabaseConnection.java` trong mã nguồn.

Thông tin kết nối cần tương ứng với MySQL Server của bạn:

- Địa chỉ máy chủ: `localhost`.
- Cổng mặc định: `3306`.
- Tên cơ sở dữ liệu: `KTX`.
- Tên người dùng: tài khoản MySQL của bạn.
- Mật khẩu: mật khẩu của tài khoản MySQL.

Có thể sử dụng các biến môi trường sau:

```text
KTX_DB_URL=jdbc:mysql://localhost:3306/KTX?serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
KTX_DB_USER=root
KTX_DB_PASSWORD=mat_khau_mysql_cua_ban
```

Thay `mat_khau_mysql_cua_ban` bằng mật khẩu thực tế.

Nếu sử dụng schema do giảng viên cấp với tên khác, sửa tên schema trong `KTX_DB_URL`.

**Lưu ý bảo mật:** Không đưa mật khẩu MySQL thật lên GitHub. Không lưu mật khẩu cá nhân trực tiếp trong mã nguồn khi đẩy dự án lên kho mã nguồn công khai.

## 7. Chạy dự án bằng Visual Studio Code

### Bước 1. Mở dự án

Mở VS Code, chọn:

`File → Open Folder...`

Sau đó chọn thư mục gốc `KTXManagement`, nơi chứa file `pom.xml`.

Không chỉ mở riêng thư mục `src` vì Maven cần file `pom.xml` để nhận diện dự án.

### Bước 2. Cài đặt extension

Mở mục Extensions trong VS Code và cài đặt:
- Extension Pack for Java.
- Maven for Java.

Nếu VS Code yêu cầu chọn JDK, hãy chọn JDK 17 trở lên.

### Bước 3. Tải thư viện Maven

Mở Terminal trong VS Code:

`Terminal → New Terminal`

Di chuyển đến thư mục chứa `pom.xml` nếu Terminal chưa ở đúng thư mục, sau đó chạy:

```bash
mvn clean install
```

Nếu chỉ cần biên dịch dự án, có thể chạy:

```bash
mvn clean compile
```

Đảm bảo `pom.xml` đã khai báo thư viện MySQL Connector/J phù hợp.

### Bước 4. Chạy ứng dụng

Sau khi Maven biên dịch thành công, chạy lớp chính của ứng dụng:

```bash
mvn exec:java -Dexec.mainClass="ktx.ui.MainFrame"
```

Lệnh trên yêu cầu dự án đã cấu hình Maven Exec Plugin trong `pom.xml`. Nếu chưa có plugin này, có thể thêm cấu hình phù hợp hoặc sử dụng cách chạy trực tiếp dưới đây.

**Cách chạy trực tiếp trong VS Code:**

1. Mở `src/main/java/ktx/ui/MainFrame.java`.
2. Tìm phương thức `main`.
3. Nhấn nút **Run** hoặc **Run Java** phía trên phương thức `main`.
4. Chờ ứng dụng Swing mở cửa sổ giao diện.

Nếu lớp `MainFrame` không có phương thức `main`, hãy chạy lớp khởi động thực tế của dự án.

### Bước 5. Xử lý lỗi thường gặp

- **Không tìm thấy MySQL Connector/J:** kiểm tra khai báo dependency trong `pom.xml`, sau đó tải lại dự án Maven.
- **Access denied:** kiểm tra tên tài khoản, mật khẩu và quyền truy cập schema.
- **Unknown database:** kiểm tra tên cơ sở dữ liệu trong MySQL và URL kết nối Java.
- **Communications link failure:** kiểm tra MySQL Server đã khởi động chưa, địa chỉ máy chủ và cổng kết nối.
- **Could not find or load main class:** kiểm tra package, tên lớp chính và cấu trúc thư mục mã nguồn.
- **Không có dữ liệu hiển thị:** kiểm tra dữ liệu mẫu, schema kết nối và các câu truy vấn trong ứng dụng.

## 8. Dữ liệu mẫu

Dự án sử dụng dữ liệu mẫu để phục vụ kiểm thử:

- Mã sinh viên: `SV001`, `SV002`, `SV003`.
- Mã phòng: `P101`, `P102`, `P103`.
- Số CCCD: sử dụng các giá trị giả lập dành riêng cho kiểm thử.

Dữ liệu mẫu không đại diện cho thông tin cá nhân có thật.

## 9. Kiểm thử chức năng

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

Kết quả kiểm thử cần được ghi nhận dựa trên việc chạy ứng dụng thực tế.

## 10. Các ràng buộc và lưu ý

- JDBC sử dụng `PreparedStatement` để truyền tham số vào câu lệnh SQL.
- Nghiệp vụ xếp phòng sử dụng transaction và `SELECT ... FOR UPDATE` để hạn chế xung đột khi nhiều thao tác xếp phòng diễn ra đồng thời.
- Các ràng buộc khóa chính, khóa ngoại và tính duy nhất giúp bảo vệ tính toàn vẹn dữ liệu.
- Cách tính tiền phòng theo tháng, làm tròn ngày, chuyển phòng giữa tháng và miễn giảm cần được thống nhất theo yêu cầu của giảng viên trước khi triển khai chức năng hóa đơn.
- Giao diện CRUD cho các bảng dịch vụ, sử dụng dịch vụ, xe tháng và khách thăm cần được tích hợp thêm nếu thuộc phạm vi yêu cầu cuối cùng của nhóm.

## 11. Đóng góp và phát triển

Dự án có thể tiếp tục được mở rộng theo các hướng:

- Hoàn thiện giao diện cho các module còn thiếu.
- Bổ sung chức năng quản lý hợp đồng và hóa đơn.
- Bổ sung kiểm thử tự động.
- Cải thiện xử lý lỗi và thông báo cho người dùng.
- Hoàn thiện phân quyền và bảo mật thông tin kết nối cơ sở dữ liệu.

---

**Trạng thái dự án:** Cần chạy thử trên môi trường thực tế để xác nhận việc cài đặt, kết nối cơ sở dữ liệu và các chức năng trước khi nghiệm thu.

