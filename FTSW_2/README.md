# Fishing Tackle Shop Website

Final Term Project Of Web Programming Course

Diep Chi Binh An · Cao Do Tri Dung · Ngo Tran Ngan Do (Leader) · Nguyen Cao Dai Loc

Java 21, Jakarta Servlet/JSP (Tomcat 11), Hibernate 6, PostgreSQL.

## Chạy trên máy cá nhân

1. Cài PostgreSQL, tạo database `fishing_shop`.
2. Đặt biến môi trường (KHÔNG ghi mật khẩu vào code):

| Biến | Bắt buộc | Ý nghĩa |
| --- | --- | --- |
| `DB_PASSWORD` | có | Mật khẩu PostgreSQL |
| `DB_URL` | không | Mặc định `jdbc:postgresql://localhost:5432/fishing_shop` |
| `DB_USER` | không | Mặc định `postgres` |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | để tạo admin | Tài khoản admin đầu tiên (mật khẩu >= 8 ký tự). Chỉ tạo khi trong DB chưa có admin nào. |
| `ADMIN_NAME` | không | Tên hiển thị của admin |
| `GMAIL_EMAIL`, `GMAIL_APP_PASSWORD` | để gửi OTP | Gửi mã OTP quên mật khẩu |

3. Build và chạy: `mvn clean package`, rồi deploy `target/fishingshop.war` lên Tomcat 11
   (hoặc chạy bằng plugin Tomcat trong IDE; nhớ đặt biến môi trường cho cấu hình chạy đó).
4. Bảng được Hibernate tự tạo khi khởi động (`hbm2ddl.auto=update`).

## Chạy trên Render + Neon

Đặt `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (tùy chọn `DB_SSL_MODE`, mặc định `require`)
cùng các biến `ADMIN_*` và `GMAIL_*` ở trên.

## Phân quyền

- `/admin/*`: chỉ tài khoản admin. Admin đăng nhập bằng **username**.
- Giỏ hàng, đặt hàng, đơn hàng, đánh giá, thông báo, sổ địa chỉ (`/cart`, `/checkout`, `/orders`,
  `/feedback`, `/notifications`, `/account/*`): chỉ khách đã đăng nhập. Khách đăng nhập bằng **email**.
- Chỉ khách đã mua sản phẩm (đơn chưa hủy) mới gửi được đánh giá.

## Phân công

Xem `DANH MỤC PHÂN CÔNG VÀ YÊU CẦU DỰ ÁN.docx`.
