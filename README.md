# Hệ thống quản lý đặt lịch sân bóng đá

> **Môn học:** Phát triển phần mềm hướng dịch vụ
> **Đề tài:** Xây dựng và phát triển hệ thống quản lý đặt lịch sân bóng đá tích hợp quản lý lịch và thanh toán điện tử.
> **Tài liệu này** là file tổng theo dõi toàn bộ học phần: phần A là thông tin dùng chung cho cả hệ thống; phần B là nhật ký thực hành theo từng buổi (bổ sung dần).

---

# ⚡ CHẠY HỆ THỐNG TRONG 3 PHÚT

## Yêu cầu máy
JDK 17 trở lên · Maven 3.9+ · Git · Postman. **Không cần cài MySQL** để chạy thử — mặc định dùng H2 ghi ra file.

## Lần đầu clone về

```bash
git clone <URL-repo> && cd football-booking-service && mvn clean install -DskipTests
```

## Chạy 4 dịch vụ — mỗi dịch vụ một cửa sổ terminal

```bash
cd auth-service    && mvn spring-boot:run     # cổng 8080 - Hoàng
cd booking-service && mvn spring-boot:run     # cổng 8081 - Linh
cd field-service   && mvn spring-boot:run     # cổng 8082 - Hùng
cd payment-service && mvn spring-boot:run     # cổng 8083 - Nhật
```

Kiểm tra cả 4 đã sống:

```bash
curl http://localhost:8080/health && curl http://localhost:8081/health && curl http://localhost:8082/health && curl http://localhost:8083/health
```

Mỗi lệnh trả về `{"service":"...","port":"...","owner":"...","status":"UP",...}`.

## Kiểm thử bằng Postman

Import `docs/postman/football-booking.postman_collection.json` → chạy lần lượt các thư mục **0 → 5**.
Biến `{{token}}`, `{{bookingId}}`, `{{paymentId}}` tự lưu sau khi đăng nhập / đặt sân, không phải copy tay.

## Chuyển sang MySQL khi quay video / demo

```bash
mysql -u root -p < sql/init_mysql.sql
```

Rồi chạy từng service kèm profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## Bật JWT khi Hoàng làm xong

Mặc định `app.security.mode=open` để cả nhóm gọi API thoải mái trong lúc dựng. Khi Hoàng xong `SecurityConfig`, đổi thành `app.security.mode=jwt` trong `application.properties` của cả 4 service.

## Cấu trúc thư mục

```
football-booking-service/
├── pom.xml                 ← POM cha, khai báo 5 module
├── common-security/        thư viện JWT dùng chung + /health   (Hoàng)
├── auth-service/     :8080 tài khoản, đăng nhập, vai trò       (Hoàng)
├── booking-service/  :8081 lượt đặt sân, chống trùng lịch      (Linh)
├── field-service/    :8082 sân bóng, bảng giá, giờ vàng        (Hùng)
├── payment-service/  :8083 thanh toán điện tử                  (Nhật)
├── sql/init_mysql.sql      tạo 4 database tách biệt
└── docs/
    ├── PHAN_CONG_KT1.md    phân công, kịch bản demo, kịch bản video
    ├── api-contract.md     hợp đồng API giữa các dịch vụ (ĐỌC TRƯỚC KHI CODE)
    └── postman/            bộ kiểm thử 40 request
```

## Quy tắc git

```bash
git checkout -b feat/<ten>-<service>      # ví dụ: feat/hung-field-service
# ... code ...
git push -u origin feat/hung-field-service
```

Mỗi người chỉ sửa file trong module của mình. Merge vào `develop` qua Pull Request, Hoàng review.
Module `common-security` do Hoàng sở hữu — ai cần đổi phải báo trước.

---

# 📘 PHẦN A — TỔNG QUAN HỆ THỐNG (dùng chung)

## A.1. Công nghệ sử dụng

| Thành phần | Phiên bản / Công cụ |
|---|---|
| Ngôn ngữ | Java 17+ |
| Framework | Spring Boot 3.2.5 (Spring Web, Validation) |
| Build tool | Maven |
| Kiểm thử API | Postman |
| Lưu trữ dữ liệu | `List` trong bộ nhớ (buổi 1) → Database (buổi sau) |
| IDE | VS Code / IntelliJ IDEA |

## A.2. Phân rã dịch vụ

Hệ thống được tách thành nhiều **dịch vụ độc lập**, mỗi dịch vụ một trách nhiệm rõ ràng.

| # | Dịch vụ | Trách nhiệm | Dữ liệu quản lý | API tiêu biểu |
|---|---|---|---|---|
| 1 | **Booking Service** ⭐ | Quản lý lượt đặt sân | Lượt đặt: khách, sân, ngày, giờ, giá, trạng thái | `/api/bookings` |
| 2 | **Field Service** | Quản lý sân bóng | Sân: tên, loại (5/7/11 người), giá/giờ, trạng thái | `/api/fields` |
| 3 | **Customer Service** | Quản lý khách hàng | Khách: họ tên, SĐT, email, điểm tích lũy | `/api/customers` |
| 4 | **Schedule Service** | Quản lý lịch & khung giờ trống | Khung giờ, lịch sân theo ngày, trống/đã đặt | `/api/schedules` |
| 5 | **Payment Service** | Thanh toán điện tử | Giao dịch: mã đặt sân, số tiền, phương thức, trạng thái | `/api/payments` |
| 6 | *Notification Service* | Gửi thông báo xác nhận (SMS/Email) | Lịch sử thông báo | `/api/notifications` |
| 7 | *API Gateway / Auth* | Định tuyến request + xác thực | Token, phân quyền | — |

> ⭐ = đã bắt đầu ở buổi 1. *In nghiêng* = dịch vụ hạ tầng, triển khai ở các buổi sau.

> **Phạm vi chốt cho Bài kiểm tra số 1:** triển khai **4 dịch vụ** — `auth-service`, `booking-service`, `field-service`, `payment-service`. Customer / Schedule / Notification / API Gateway giữ nguyên trong thiết kế nhưng không code, trình bày ở phần "hướng phát triển". Chi tiết và lý do xem [docs/PHAN_CONG_KT1.md](docs/PHAN_CONG_KT1.md).

## A.3. Sơ đồ kiến trúc tổng thể (mục tiêu)

```
                         +------------------+
                         |     Client       |   Web / Mobile / Postman
                         +--------+---------+
                                  |
                                  v
                         +------------------+
                         |   API Gateway    |   định tuyến + xác thực
                         +--------+---------+
            +----------------+----+-----+----------------+----------------+
            |                |          |                |                |
            v                v          v                v                v
   +---------------+ +-------------+ +-----------+ +--------------+ +-------------+
   | Booking ⭐    | | Field       | | Customer  | | Schedule     | | Payment     |
   | Service       | | Service     | | Service   | | Service      | | Service     |
   +-------+-------+ +------+------+ +-----+-----+ +------+-------+ +------+------+
           |                |             |              |                |
           v                v             v              v                v
      Booking DB        Field DB      Customer DB    Schedule DB      Payment DB

                 (Mỗi service tự quản lý database riêng)
```

## A.4. Quan hệ liên dịch vụ (luồng đặt sân)

```
Client đặt sân
   |
   v
Booking Service
   | 1. Hỏi Field Service:    sân có tồn tại / đang hoạt động?
   | 2. Hỏi Schedule Service: khung giờ còn trống? -> khóa khung giờ
   | 3. Tạo lượt đặt (status = PENDING)
   | 4. Gọi Payment Service:  tạo giao dịch thanh toán điện tử
   | 5. Thanh toán xong -> cập nhật status = PAID
   | 6. Gọi Notification:     gửi xác nhận cho khách
   v
Trả kết quả + mã HTTP cho Client
```

## A.5. Lộ trình phát triển (10 buổi)

| Buổi | Chủ đề | Bổ sung | Trạng thái |
|---|---|---|---|
| **1** | RESTful Service đầu tiên | Booking Service, CRUD, xử lý lỗi | ✅ Hoàn thành |
| 2 | Kết nối Database | Spring Data JPA, lưu trữ thật | ⏳ |
| 3 | Thêm các dịch vụ | Field, Customer, Schedule, Payment | ⏳ |
| 4 | Giao tiếp liên dịch vụ | RestTemplate / OpenFeign | ⏳ |
| 5 | Service Registry | Eureka | ⏳ |
| 6 | API Gateway | Spring Cloud Gateway | ⏳ |
| 7 | Bảo mật | Spring Security, JWT | ⏳ |
| 8 | Thanh toán điện tử | Tích hợp cổng thanh toán (test mode) | ⏳ |
| 9 | Triển khai | Docker, Docker Compose | ⏳ |
| 10 | Giám sát & tối ưu | Logging, Monitoring | ⏳ |

---

# 📒 PHẦN B — NHẬT KÝ THỰC HÀNH THEO BUỔI

## ✅ BUỔI 1 — Làm quen dịch vụ & RESTful Service đầu tiên

**Mục tiêu:** xây dựng Booking Service độc lập, quản lý đặt sân qua REST API, xử lý đúng mã trạng thái HTTP.

### B1.1. Mô hình miền — thực thể `Booking`

| Thuộc tính | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | Long | Tự sinh | Mã lượt đặt |
| `customerName` | String | `@NotBlank` | Tên khách hàng |
| `phone` | String | `@NotBlank` | Số điện thoại |
| `fieldName` | String | `@NotBlank` | Tên sân |
| `bookingDate` | LocalDate | `@NotNull`, `yyyy-MM-dd` | Ngày đá |
| `startTime` | LocalTime | `@NotNull`, `HH:mm` | Giờ bắt đầu |
| `endTime` | LocalTime | `@NotNull`, `HH:mm` | Giờ kết thúc |
| `price` | Double | `@NotNull`, `@Positive` | Giá thuê (VND) |
| `status` | BookingStatus | Mặc định `PENDING` | Trạng thái |

**Vòng đời trạng thái (`BookingStatus`):**
```
   [PENDING] --- xác nhận --->  [CONFIRMED] --- thanh toán --->  [PAID]
       |                            |
       +----------- hủy ------------+-----------> [CANCELLED]
```

### B1.2. Cấu trúc mã nguồn

> ⚠️ **Code buổi 1 đã được gỡ khỏi nhánh làm việc.** Khi chuẩn bị Bài kiểm tra số 1, nhóm thống nhất viết lại `booking-service` từ đầu cùng nhau vì thiết kế đổi gần hết: bỏ `customerName`/`phone` nhập tay (lấy từ JWT), thay `fieldName` bằng `fieldId`, giá do `field-service` tính thay vì client gửi lên, `DELETE` đổi thành `PUT /{id}/cancel`.
>
> Bản gốc buổi 1 vẫn còn nguyên trong git, lấy lại bằng:
> ```bash
> git checkout buoi-1-goc -- booking-service/src
> ```
> Phần mô tả dưới đây giữ lại làm nhật ký học tập của buổi 1.

```
src/main/java/com/football/bookingservice/
├── BookingServiceApplication.java      # Điểm khởi động
├── controller/BookingController.java   # Tầng tiếp nhận HTTP (REST API)
├── service/BookingService.java         # Tầng nghiệp vụ
├── model/Booking.java                  # Thực thể lượt đặt sân
├── model/BookingStatus.java            # Enum trạng thái
├── dto/StatisticsResponse.java         # Dữ liệu trả về cho thống kê
└── exception/
    ├── ResourceNotFoundException.java  # Lỗi không tìm thấy -> 404
    └── GlobalExceptionHandler.java     # Bắt lỗi tập trung (404/400)
```

### B1.3. Cách chạy

```bash
mvn spring-boot:run
```
Ứng dụng: **http://localhost:8081** (cấu hình `server.port` trong `application.properties`).

### B1.4. Danh sách API

Base URL: `http://localhost:8081/api/bookings`

| Method | Endpoint | Mô tả | Mã |
|---|---|---|---|
| GET | `/hello` | Kiểm tra dịch vụ | 200 |
| GET | `/info` | Thông tin dịch vụ | 200 |
| GET | `/` | Danh sách lượt đặt | 200 |
| GET | `/{id}` | Chi tiết theo mã | 200 / 404 |
| POST | `/` | Thêm lượt đặt | 201 / 400 |
| PUT | `/{id}` | Cập nhật | 200 / 404 |
| DELETE | `/{id}` | Xóa | 200 / 404 |
| GET | `/search?name=An` | Tìm theo tên khách | 200 |
| GET | `/paged?page=0&size=10` | Phân trang | 200 |
| GET | `/statistics` | Thống kê theo trạng thái + doanh thu | 200 |

Ví dụ JSON cho POST / PUT:
```json
{
  "customerName": "Le Van Cuong",
  "phone": "0987654321",
  "fieldName": "San so 3",
  "bookingDate": "2026-10-01",
  "startTime": "17:00",
  "endTime": "18:30",
  "price": 320000,
  "status": "CONFIRMED"
}
```

### B1.5. Xử lý lỗi (HTTP Status Code)

| Tình huống | Mã |
|---|---|
| Thành công | 200 OK |
| Tạo mới | 201 Created |
| Dữ liệu không hợp lệ | 400 Bad Request |
| Không tìm thấy | 404 Not Found |

Ví dụ response 400:
```json
{
  "status": 400,
  "message": "Du lieu khong hop le",
  "errors": { "customerName": "Ten khach hang khong duoc de trong" }
}
```

### B1.6. Bảng kiểm thử

| TC | Request | Mã mong đợi |
|---|---|---|
| TC01 | GET `/api/bookings` | 200 |
| TC02 | GET `/api/bookings/1` | 200 |
| TC03 | GET `/api/bookings/999` | 404 |
| TC04 | POST dữ liệu rỗng | 400 |
| TC05 | DELETE `/api/bookings/999` | 404 |
| TC06 | POST hợp lệ | 201 |
| TC07 | PUT `/api/bookings/1` | 200 |
| TC08 | DELETE `/api/bookings/1` | 200 |
| TC09 | GET `/search?name=An` | 200 |
| TC10 | GET `/paged?page=0&size=1` | 200 |
| TC11 | GET `/statistics` | 200 |

### B1.7. Kết quả đạt được
- ✅ Nhận diện & tách được 7 dịch vụ trong hệ thống.
- ✅ Khởi tạo RESTful Service bằng Spring Boot.
- ✅ CRUD đầy đủ, trao đổi JSON.
- ✅ Xử lý đúng 200 / 201 / 400 / 404.
- ✅ Bổ sung tìm kiếm, phân trang, thống kê.
- ✅ Tổ chức mã nguồn theo tầng, sẵn sàng mở rộng Microservices.

---

## ⏳ BUỔI 2 — Kết nối Database (sẽ bổ sung)

**Dự kiến:** thay `List` trong RAM bằng Database thật với Spring Data JPA.
- [ ] Thêm dependency JPA + driver (H2 / MySQL)
- [ ] Biến `Booking` thành `@Entity`
- [ ] Tạo `BookingRepository`
- [ ] Dữ liệu không mất khi restart

*(Nội dung chi tiết, API, test case sẽ được cập nhật sau buổi 2.)*

---

## ⏳ BUỔI 3 — Thêm các dịch vụ (sẽ bổ sung)

**Dự kiến:** xây dựng Field / Customer / Schedule / Payment Service.

*(Cập nhật sau.)*

---

## ⏳ BUỔI 4 — Giao tiếp liên dịch vụ (sẽ bổ sung)

**Dự kiến:** Booking gọi Field/Schedule/Payment qua RestTemplate hoặc OpenFeign.

*(Cập nhật sau.)*

---

## ⏳ BUỔI 5 — Service Registry (sẽ bổ sung)

**Dự kiến:** Eureka Server, các service tự đăng ký & khám phá.

*(Cập nhật sau.)*

---

## ⏳ BUỔI 6 — API Gateway (sẽ bổ sung)

**Dự kiến:** Spring Cloud Gateway định tuyến tập trung.

*(Cập nhật sau.)*

---

## ⏳ BUỔI 7 — Bảo mật (sẽ bổ sung)

**Dự kiến:** Spring Security + JWT, phân quyền người dùng.

*(Cập nhật sau.)*

---

## ⏳ BUỔI 8 — Thanh toán điện tử (sẽ bổ sung)

**Dự kiến:** tích hợp cổng thanh toán ở chế độ test (sandbox).

*(Cập nhật sau.)*

---

## ⏳ BUỔI 9 — Triển khai (sẽ bổ sung)

**Dự kiến:** đóng gói Docker, chạy nhiều service bằng Docker Compose.

*(Cập nhật sau.)*

---

## ⏳ BUỔI 10 — Giám sát & tối ưu (sẽ bổ sung)

**Dự kiến:** Logging tập trung, Monitoring, tối ưu hiệu năng.

*(Cập nhật sau.)*
