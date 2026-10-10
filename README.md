# KTX Management — Java Swing + JDBC + MySQL

## Chuc nang trong ban giao
- SQL tao schema KTX, bang SinhVien, Phong, ThuePhong va cac bang mo rong DichVu, SuDungDichVu, XeThang, KhachTham.
- Du lieu mau de demo.
- Java Swing: xem/tim/them/sua/xoa sinh vien; xem/them/sua phong; xep phong va tra phong; hien thi suc chua va so cho trong.
- JDBC dung PreparedStatement; xep phong dung transaction va khoa dong phong (`SELECT ... FOR UPDATE`).

## Yeu cau
- JDK 17+
- MySQL Server 8+ va MySQL Workbench
- NetBeans co ho tro Maven

## Cai dat CSDL
1. Mo MySQL Workbench va ket noi den MySQL Server.
2. Mo `database/01_ktx_schema.sql`.
3. Neu tai khoan co quyen tao schema, chay toan bo script. Neu bi `ERROR 1044 Access denied ... database`, khong co quyen tao database: bo qua `CREATE DATABASE` va `USE KTX`, chon schema do giang vien cap, roi chay phan `CREATE TABLE` tro di. Neu schema duoc cap co ten khac, sua URL Java tuong ung.
4. Kiem tra `SHOW TABLES;` va `SELECT * FROM V_SinhVienDangO;`.

## Chay Java
1. Mo thu muc goc `KTXManagement` bang NetBeans: File > Open Project.
2. Mo `DatabaseConnection.java`. Mac dinh user la `root`, mat khau rong. Khong dung duoc thi dat bien moi truong:
   - `KTX_DB_URL=jdbc:mysql://localhost:3306/KTX?serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8`
   - `KTX_DB_USER=root`
   - `KTX_DB_PASSWORD=mat_khau_mysql_cua_ban`
3. Reload Maven project de tai `mysql-connector-j`.
4. Run `ktx.ui.MainFrame`.

## Du lieu mau
MaSV: `SV001`, `SV002`, `SV003`; phong: `P101`, `P102`, `P103`. So CMT mau la TEST..., khong phai giay to that.

## Kiem thu nhanh
1. Mo tab Sinh vien: danh sach 3 sinh vien mau hien ra.
2. Them sinh vien moi; thu them trung MaSV hoac SoCMT.
3. Mo tab Phong: kiem tra so dang o va so cho trong.
4. Xep `SV001` vao phong khac khi dang co hop dong: he thong tu choi.
5. Xep sinh vien moi vao phong da day: he thong tu choi.
6. Tra phong voi ngay khong truoc NgayBatDau; ngay khong hop le se bi tu choi.
7. Xoa sinh vien dang duoc bang ThuePhong tham chieu: MySQL tu choi do khoa ngoai, giup bao toan lich su.

## Luu y quan trong
- Bang DichVu, SuDungDichVu, XeThang, KhachTham la khung CSDL de nhom tich hop. Giao dien CRUD cho cac module nay chua duoc trien khai trong ban giao nay; can ghep voi phan cac thanh vien phu trach.
- Quy tac tinh tien phong theo thang (lam tron ngay, chuyen phong giua thang, mien giam) phu thuoc yeu cau cua giang vien; can thong nhat truoc khi them hoa don.
- Tai khoan MySQL co the khong co quyen CREATE DATABASE. Loi `ERROR 1044` la quyen truy cap, khong phai loi cu phap SQL.
- Chua the xac nhan da chay tren may cua ban; can cau hinh dung user/password va kiem thu tren MySQL local truoc khi nop.
