# PHÂN CÔNG BÀI KIỂM TRA SỐ 1

**Đề tài:** Xây dựng và phát triển hệ thống quản lý đặt lịch sân bóng đá tích hợp quản lý lịch và thanh toán điện tử
**Nhóm 4 người:** Hoàng (nhóm trưởng), Linh, Hùng, Nhật
**Ngày lập:** 26/10/2026

---

## 0. HIỆN TRẠNG & PHẠM VI CHỐT

> **Nhóm đã quyết định xóa code buổi 1 và viết lại từ đầu cùng nhau.** Code cũ lưu ở tag git `buoi-1-goc`, lấy lại bất cứ lúc nào bằng `git checkout buoi-1-goc -- booking-service/src`. Lý do: thiết kế mới đổi gần hết (bỏ `customerName`/`phone` nhập tay, thay `fieldName` bằng `fieldId`, giá do field-service tính, `DELETE` thành `PUT /cancel`), và quan trọng hơn là để Linh tự viết thì mới giải thích được code của mình khi thầy yêu cầu sửa tham số tại chỗ.

| Yêu cầu đề bài | Hiện trạng repo | Còn thiếu |
|---|---|---|
| Ý 1 — RESTful API (1,0đ) | ⬜ 4 service mới có khung chạy được + `/health` | Toàn bộ endpoint nghiệp vụ, validation R1–R9 |
| Ý 2 — Giao tiếp ≥ 2 dịch vụ (1,5đ) | ⬜ Đã chốt hợp đồng API, chưa viết client | `FieldClient`, `RestTemplate`, xử lý lỗi E1–E4, sequence diagram |
| Ý 3 — Lưu CSDL (1,5đ) | 🟨 4 datasource tách biệt đã cấu hình (H2 + profile MySQL) | `@Entity`, `Repository`, seed dữ liệu, nghiệp vụ ghi/đọc |
| Ý 4 — JWT + phân quyền (2,0đ) | 🟨 `common-security` có khung, đang để `mode=open` | `JwtService`, `JwtAuthFilter`, `SecurityConfig`, 2 vai trò |

**Đã có sẵn làm nền:** 5 module Maven build xanh · 4 cổng 8080–8083 chạy độc lập · 4 CSDL tách biệt · hợp đồng API chốt trong `api-contract.md` · bộ Postman 40 request chờ API.
**Phần thực nghiệm hiện ở mức 0/6,0** — repo mới có giàn giáo, chưa có nghiệp vụ.

**Chốt phạm vi: 4 dịch vụ** (bỏ Customer / Schedule / Notification / Gateway, đưa vào "hướng phát triển" Chương 5).

| Dịch vụ | Port | CSDL | Chủ sở hữu dữ liệu | Người phụ trách |
|---|---|---|---|---|
| `auth-service` | 8080 | `auth_db` | Tài khoản, vai trò | **Hoàng** |
| `booking-service` | 8081 | `booking_db` | Lượt đặt sân | **Linh** |
| `field-service` | 8082 | `field_db` | Sân bóng, bảng giá, lịch bận | **Hùng** |
| `payment-service` | 8083 | `payment_db` | Giao dịch thanh toán | **Nhật** |

---

## 0b. KHO CODE & NHÁNH CỦA TỪNG NGƯỜI

**Repo:** https://github.com/Boyngoc/football-booking-service

| Nhánh | Người | Module được phép sửa | Cổng |
|---|---|---|---|
| `feat/hoang-auth-service` | Hoàng | `auth-service/` + `common-security/` | 8080 |
| `feat/linh-booking-service` | Linh | `booking-service/` | 8081 |
| `feat/hung-field-service` | Hùng | `field-service/` | 8082 |
| `feat/nhat-payment-service` | Nhật | `payment-service/` | 8083 |

Ngoài ra có `main` (bản chốt, khóa) và `develop` (nhánh tích hợp — mọi Pull Request merge vào đây).

**Bắt đầu làm việc:**

```bash
git clone https://github.com/Boyngoc/football-booking-service.git
cd football-booking-service
mvn clean install -DskipTests
git checkout feat/linh-booking-service      # đổi thành nhánh của mình
```

Nhánh đã có sẵn trên GitHub nên `git checkout` là bám theo luôn, không cần `-b`.

**Chạy thử (mỗi service một cửa sổ terminal):**

```bash
cd auth-service    && mvn spring-boot:run     # 8080
cd booking-service && mvn spring-boot:run     # 8081
cd field-service   && mvn spring-boot:run     # 8082
cd payment-service && mvn spring-boot:run     # 8083
```

Kiểm tra sống: `curl http://localhost:8082/health`. **Chưa cần cài MySQL** — mặc định dùng H2 ghi ra file.

**Nộp việc:**

```bash
git add . && git commit -m "feat: ..." && git push
```

Rồi mở Pull Request trên GitHub nhắm vào `develop`, Hoàng review và merge.

---

## 1. NGHIỆP VỤ LÕI — "ĐẶT MỘT SÂN BÓNG"

Đây là nghiệp vụ xuyên suốt cả 4 service, dùng chung cho báo cáo, demo và video của cả nhóm.

```
Khách (CUSTOMER) muốn đặt Sân số 2 (sân 7 người), ngày 02/11/2026, 17:00–18:30
        |
        v
 [1] auth-service   : POST /api/auth/login  -> trả JWT (role = CUSTOMER)
        |
        v
 [2] booking-service: POST /api/bookings  (kèm Bearer token)
        |
        |-- [3] GET http://localhost:8082/api/fields/2
        |         -> sân có tồn tại không? status có phải ACTIVE không?
        |
        |-- [4] GET http://localhost:8082/api/fields/2/quote?date=2026-11-02&start=17:00&end=18:30
        |         -> field-service tự tính tiền (17:00-18:30 rơi vào KHUNG GIỜ VÀNG)
        |         -> { "amount": 675000, "goldenHours": 1.5, "normalHours": 0 }
        |
        |-- [5] kiểm tra TRÙNG LỊCH trong booking_db (chống double-booking)
        |
        |-- [6] INSERT bookings (status = PENDING, total_price = 675000)
        v
 [7] payment-service: POST /api/payments { bookingId: 12, method: "MOMO" }
        |-- [8] GET http://localhost:8081/api/bookings/12  -> lấy số tiền thật (KHÔNG tin số tiền client gửi)
        |-- [9] INSERT payments (status = INIT, txnRef = "FB1762...")
        v
 [10] POST /api/payments/9/confirm  (giả lập cổng MoMo báo thành công)
        |-- [11] PUT http://localhost:8081/api/bookings/12/status { "status": "PAID" }
        v
 Sân đã được giữ, trạng thái PAID
```

**Điểm then chốt phải nói được khi thầy hỏi:** `booking-service` **không** tự tính tiền sân và **không** đọc bảng `fields`. Giá là tài sản của `field-service`, nên Booking phải **hỏi qua API**. Tương tự, `payment-service` **không** đọc bảng `bookings` để biết phải thu bao nhiêu, mà gọi `GET /api/bookings/{id}`.

### Dữ liệu mẫu dùng thống nhất cả nhóm (seed vào `field_db`)

| id | Tên sân | Loại | Giá thường (VND/h) | Giá giờ vàng (VND/h) | Trạng thái |
|---|---|---|---|---|---|
| 1 | Sân số 1 | SAN_5 | 200.000 | 300.000 | ACTIVE |
| 2 | Sân số 2 | SAN_7 | 300.000 | 450.000 | ACTIVE |
| 3 | Sân số 3 | SAN_7 | 300.000 | 450.000 | **MAINTENANCE** |
| 4 | Sân số 4 | SAN_11 | 600.000 | 900.000 | ACTIVE |

- **Giờ mở cửa:** 05:00 – 23:00
- **Khung giờ vàng:** 17:00 – 21:00 (hệ số 1,5)
- **Sân số 3 cố tình để MAINTENANCE** → dùng làm ca kiểm thử lỗi 409 trong demo
- **Tài khoản mẫu:** `khach01 / 123456` (CUSTOMER), `chusan / 123456` (OWNER)

### Quy tắc nghiệp vụ bắt buộc kiểm tra (đặc thù sân bóng)

| # | Quy tắc | Vi phạm → mã lỗi |
|---|---|---|
| R1 | Giờ bắt đầu/kết thúc phải là bội số 30 phút (17:00, 17:30) | 400 |
| R2 | Thời lượng tối thiểu 60 phút, tối đa 180 phút | 400 |
| R3 | Chỉ đặt trong giờ mở cửa 05:00–23:00 | 400 |
| R4 | `endTime` phải lớn hơn `startTime` | 400 |
| R5 | Không đặt cho ngày trong quá khứ, không quá 30 ngày tới | 400 |
| R6 | Sân không tồn tại | 404 |
| R7 | Sân đang bảo trì (Sân số 3) | 409 |
| R8 | **Khung giờ đã có người đặt** (chống double-booking) | 409 |
| R9 | Không hủy sân khi còn dưới 2 tiếng là tới giờ đá | 409 |

---

## 2. BẢNG PHÂN CÔNG TỔNG HỢP

| Thành viên | Dịch vụ sở hữu | Hạng mục rubric | Điểm | Tỉ trọng |
|---|---|---|---|---|
| **Hoàng** (trưởng nhóm) | `auth-service` | LT Ý1b + LT Ý3 + TN Ý4 (JWT & phân quyền) + ráp báo cáo + điều phối | **3,1** | 31% |
| **Linh** | `booking-service` | LT Ý1a + TN Ý1 (RESTful API & tài liệu) + TN Ý4 (chặn quyền trên Booking) | **2,4** | 24% |
| **Hùng** | `field-service` | LT Ý2a + TN Ý2 (giao tiếp Booking ↔ Field & xử lý lỗi) | **2,25** | 22,5% |
| **Nhật** | `payment-service` | LT Ý2b + TN Ý3 (CSDL & quyền sở hữu dữ liệu) | **2,25** | 22,5% |
| | | **Tổng** | **10,0** | 100% |

---

## 3. CHI TIẾT TỪNG NGƯỜI

### 🔵 HOÀNG — `auth-service` · Bảo mật sân bóng (3,1đ)

**Bối cảnh nghiệp vụ:** sân bóng có hai loại người dùng — **khách đặt sân** và **chủ sân**. Khách chỉ được xem và quản lý lượt đặt của chính mình; chủ sân mới được xem doanh thu toàn bộ và bật/tắt bảo trì sân.

**Code**
- Entity `User`: `id`, `username`, `password` (BCrypt), `fullName`, `phone`, `role`
- Enum `Role`: `CUSTOMER` (khách đặt sân) · `OWNER` (chủ sân)
- API:
  - `POST /api/auth/register` → đăng ký khách mới, mặc định `CUSTOMER`
  - `POST /api/auth/login` → `{ "token": "eyJ...", "role": "CUSTOMER", "fullName": "Lê Văn Cường" }`
  - `GET /api/auth/me` → thông tin lấy từ token
- JWT (`jjwt`), claim: `sub` = username, `role`, `exp` = 1 giờ
- Viết `JwtAuthFilter` + `SecurityConfig` **dùng chung**, đóng gói cho Linh/Hùng/Nhật nhúng vào service của họ

**Ma trận phân quyền (bảng này phải có trong báo cáo Chương 4)**

| Endpoint | Chưa đăng nhập | CUSTOMER | OWNER |
|---|---|---|---|
| `GET /api/fields` (xem danh sách sân) | ✅ 200 (public) | ✅ 200 | ✅ 200 |
| `POST /api/bookings` (đặt sân) | ❌ 401 | ✅ 201 | ✅ 201 |
| `GET /api/bookings/me` (lượt đặt của tôi) | ❌ 401 | ✅ 200 | ✅ 200 |
| `GET /api/bookings` (toàn bộ lượt đặt) | ❌ 401 | ❌ **403** | ✅ 200 |
| `GET /api/bookings/statistics` (doanh thu) | ❌ 401 | ❌ **403** | ✅ 200 |
| `PUT /api/bookings/{id}/cancel` của **người khác** | ❌ 401 | ❌ **403** | ✅ 200 |
| `PUT /api/fields/{id}/status` (bật bảo trì sân) | ❌ 401 | ❌ **403** | ✅ 200 |

> Dòng **"CUSTOMER hủy lượt đặt của người khác → 403"** chính là tình huống đề bài yêu cầu: *"người dùng đã đăng nhập nhưng không có quyền truy cập tài nguyên"*. Bắt buộc phải demo được dòng này.

**Báo cáo:** Chương 1 (đề tài, kiến trúc 4 dịch vụ, sơ đồ) + Chương 4 (luồng đăng nhập, cấu trúc JWT, ma trận trên) + ráp 5 chương thành 1 file

**Trình bày Câu 1:**
- **Ý 1b (1,0đ)** — 4 dịch vụ của hệ thống sân bóng, trách nhiệm từng cái, 3 API tiêu biểu (`POST /api/bookings`, `GET /api/fields/{id}/quote`, `POST /api/payments`), lý do tách: *giá sân đổi thì chỉ sửa field-service; đổi cổng thanh toán thì chỉ sửa payment-service*
- **Ý 3 (1,0đ)** — authentication (bạn là ai) vs authorization (bạn được làm gì), JWT bảo vệ API thế nào, 401 vs 403, liên hệ vai trò **chủ sân**: khách đăng nhập rồi vẫn không được xem doanh thu → 403 chứ không phải 401

---

### 🟢 LINH — `booking-service` · RESTful API đặt sân (2,4đ)

**Code**
- Viết mới từ đầu: tách `BookingRequest` / `BookingResponse`, không trả Entity thô ra ngoài
- Entity `Booking`: `id`, `customerUsername` (lấy từ JWT, **không** nhận từ client), `fieldId`, `fieldName`, `bookingDate`, `startTime`, `endTime`, `totalPrice`, `status`, `createdAt`
- Bỏ field `customerName` / `phone` nhập tay — lấy từ token, tránh khách giả danh người khác
- API đầy đủ:

| Method | Endpoint | Nghiệp vụ | Mã |
|---|---|---|---|
| POST | `/api/bookings` | Đặt sân | 201 / 400 / 404 / 409 / 503 |
| GET | `/api/bookings/me` | Lượt đặt của tôi | 200 / 401 |
| GET | `/api/bookings` | Toàn bộ (chủ sân) | 200 / 403 |
| GET | `/api/bookings/{id}` | Chi tiết | 200 / 403 / 404 |
| PUT | `/api/bookings/{id}/cancel` | Hủy sân | 200 / 403 / 409 |
| PUT | `/api/bookings/{id}/status` | Payment gọi để đổi sang PAID | 200 / 404 |
| GET | `/api/bookings/statistics` | Doanh thu theo trạng thái (chủ sân) | 200 / 403 |

- Cài toàn bộ **R1–R5, R8, R9** ở mục 1 bằng `@Valid` + validator tự viết
- **Tài liệu API = Postman collection** (nhóm chốt không dùng Swagger): `docs/postman/football-booking.postman_collection.json` — chia 6 thư mục theo dịch vụ, mỗi request có `pm.test` kiểm mã trạng thái, biến `{{token}}` / `{{bookingId}}` tự lưu. Chạy cả bộ bằng Collection Runner, xuất báo cáo làm minh chứng
- Xuất **Postman collection** `docs/postman/football-booking.json`, có biến `{{token}}`, `{{bookingId}}`
- Gắn `@PreAuthorize` theo ma trận của Hoàng
- Viết bộ **test bảo mật**: không token → 401; `khach01` gọi `/statistics` → 403; `chusan` gọi → 200

**Ví dụ request/response phải đưa vào báo cáo**

```http
POST /api/bookings
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{ "fieldId": 2, "bookingDate": "2026-11-02", "startTime": "17:00", "endTime": "18:30" }
```
```json
HTTP/1.1 201 Created
{
  "id": 12, "fieldId": 2, "fieldName": "Sân số 2",
  "customerUsername": "khach01",
  "bookingDate": "2026-11-02", "startTime": "17:00", "endTime": "18:30",
  "totalPrice": 675000, "status": "PENDING"
}
```
```json
HTTP/1.1 409 Conflict      // R8 - đặt trùng khung giờ
{ "status": 409, "message": "Sân số 2 đã có người đặt khung 17:00-18:30 ngày 02/11/2026" }
```

**Báo cáo:** Chương 2 phần A (bảng endpoint, request/response, status code) + phụ lục ảnh Postman và kết quả Collection Runner

**Trình bày Câu 1 — Ý 1a (0,5đ):** đặc điểm SOA; phân biệt **dịch vụ** (`booking-service` đang chạy ở cổng 8081) / **API** (`POST /api/bookings` — bản hợp đồng để gọi vào) / **ứng dụng khách** (Postman, app đặt sân của khách)

---

### 🟠 HÙNG — `field-service` · Giao tiếp liên dịch vụ (2,25đ)

**Code**
- Entity `Field`: `id`, `name`, `type` (`SAN_5`/`SAN_7`/`SAN_11`), `pricePerHour`, `goldenPricePerHour`, `status` (`ACTIVE`/`MAINTENANCE`)
- Seed đúng 4 sân ở bảng dữ liệu mẫu mục 1
- API:
  - `GET /api/fields?type=SAN_7` — lọc theo loại sân
  - `GET /api/fields/{id}` — 200 / **404**
  - `GET /api/fields/{id}/quote?date=&start=&end=` — **field-service tự tính tiền**, tách giờ thường / giờ vàng
  - `PUT /api/fields/{id}/status` — chủ sân bật bảo trì (dùng để demo 403 và 409)
- Logic tính tiền giờ vàng (đây là đoạn code Hùng phải thuộc):
  - 16:00–18:00 trên Sân số 2 = 1h thường (300k) + 1h vàng (450k) = **750.000**
  - 17:00–18:30 = 1,5h vàng = **675.000**

**Giao tiếp Booking → Field** (`RestTemplateBuilder` với `connectTimeout = 2s`, `readTimeout = 3s`, forward header `Authorization`)

| # | Tình huống | Field trả về | Booking trả client | Thông điệp |
|---|---|---|---|---|
| E1 | Đặt `fieldId = 99` | 404 | **404** | "Không tìm thấy sân có mã 99" |
| E2 | Đặt Sân số 3 (MAINTENANCE) | 200 + `status=MAINTENANCE` | **409** | "Sân số 3 đang bảo trì, không nhận đặt lịch" |
| E3 | **Tắt hẳn field-service** rồi đặt sân | `ResourceAccessException` | **503** | "Dịch vụ quản lý sân tạm thời không khả dụng, vui lòng thử lại sau" |
| E4 | Field treo > 3s | timeout | **504** | "Quá thời gian chờ phản hồi từ dịch vụ quản lý sân" |

> E3 là tình huống mạnh nhất để demo: hệ thống **không sập**, không trả 500 trắng, mà báo lỗi có nghĩa cho khách.

**Báo cáo:** Chương 2 phần B — **sequence diagram** luồng đặt sân (PlantUML/draw.io) đủ 4 service, bảng dữ liệu gửi/nhận, bảng E1–E4

**Trình bày Câu 1 — Ý 2a (0,75đ):** mô tả quy trình `booking-service` gọi `field-service` khi khách đặt Sân số 2; nêu rõ đây là HTTP GET đồng bộ, request là URL + query `date/start/end` + header `Authorization`, response là JSON `{amount, goldenHours, normalHours}`, và từng mã 200/404/409/503/504 mang ý nghĩa gì với khách đặt sân

---

### 🟣 NHẬT — `payment-service` + CSDL toàn hệ thống (2,25đ)

**Code — phần CSDL (dùng chung cả nhóm)**
- Chuyển `booking-service` từ `List` trong RAM sang **Spring Data JPA + MySQL** (dự phòng H2 nếu máy demo chưa cài MySQL)
- Tạo **4 database tách biệt**: `auth_db`, `booking_db`, `field_db`, `payment_db` — mỗi service một `spring.datasource.url` riêng, **không** service nào có user truy cập DB của service khác
- Viết `schema.sql` + `data.sql` seed: 2 tài khoản, 4 sân, 3 lượt đặt mẫu
- Bật `spring.jpa.show-sql=true` để lộ câu SQL thật khi demo

**Code — `payment-service`**
- Entity `Payment`: `id`, `bookingId`, `amount`, `method` (`MOMO`/`VNPAY`/`CASH`), `status` (`INIT`/`SUCCESS`/`FAILED`), `txnRef`, `createdAt`, `paidAt`
- API:
  - `POST /api/payments` `{ "bookingId": 12, "method": "MOMO" }` → **gọi `GET http://localhost:8081/api/bookings/12`** để lấy số tiền thật (675.000), không tin số tiền client gửi lên
  - `POST /api/payments/{id}/confirm` → giả lập MoMo báo thành công → `PUT http://localhost:8081/api/bookings/12/status` đổi sang `PAID`
  - `GET /api/payments/booking/{bookingId}` → tra cứu giao dịch của một lượt đặt
- `bookingId` chỉ là **con số tham chiếu**, bảng `payments` không có khóa ngoại sang `bookings` vì khác database

**Minh chứng lưu dữ liệu (phải quay được)**
```sql
USE booking_db;  SELECT id, field_name, booking_date, start_time, total_price, status FROM bookings WHERE id = 12;
USE payment_db;  SELECT id, booking_id, amount, method, status, txn_ref FROM payments WHERE booking_id = 12;
```
Chạy trước khi đặt → không có dòng nào. Chạy sau khi đặt + thanh toán → booking `PAID`, payment `SUCCESS`, hai số tiền khớp nhau.

**Bảng ca kiểm thử:** tối thiểu 15 TC (input, kết quả mong đợi, kết quả thực tế, pass/fail), phủ R1–R9 và E1–E4

**Báo cáo:** Chương 3 (ERD 4 database, Entity, Repository, cấu hình kết nối, bảng quyền sở hữu dữ liệu) + Chương 5 (ca kiểm thử, kết quả, hạn chế)

**Trình bày Câu 1 — Ý 2b (0,75đ):** mỗi dịch vụ quản lý dữ liệu của mình; rủi ro cụ thể nếu vi phạm —
> *"Nếu `payment-service` đọc thẳng `booking_db` để lấy số tiền thay vì gọi API, thì hôm nào Linh đổi cột `price` thành `total_price` để cộng thêm phụ phí đèn sân, payment-service sẽ chết ngay lúc khách đang thanh toán, mà Linh hoàn toàn không biết mình vừa làm hỏng dịch vụ của người khác."*

---

## 4. VIDEO CÁ NHÂN — MỖI NGƯỜI MỘT FILE

**Yêu cầu chung**
- Thời lượng **5–7 phút/người**, quay màn hình có **tiếng nói của chính mình** (bắt buộc, không làm phụ đề chay)
- Mở đầu: *"Em là <tên>, nhóm <số>, phụ trách <phần>, đề tài hệ thống quản lý đặt lịch sân bóng đá."*
- Quay full màn hình, chữ IDE cỡ ≥ 16pt để thầy đọc được
- **Vừa chạy vừa giải thích code của mình** — không đọc slide, không quay lại màn hình người khác làm
- Kết thúc: *"Phần em còn thiếu là ... , hướng hoàn thiện là ..."* (đề bài chấm theo mức độ đáp ứng thực tế, nói thật được điểm cao hơn giấu)
- Đặt tên file: `KT1_<Ten>_<Phan>.mp4` → ví dụ `KT1_Hoang_Auth_JWT.mp4`
- Công cụ: OBS Studio hoặc Windows + G (Xbox Game Bar)

### 🔵 Video Hoàng — "Xác thực và phân quyền trong hệ thống sân bóng" (6–7')

| Phút | Nội dung |
|---|---|
| 0:00–1:00 | Giới thiệu đề tài, sơ đồ 4 dịch vụ, nói rõ ai sở hữu dữ liệu gì |
| 1:00–2:00 | Mở `User.java`, `Role.java`: giải thích vì sao sân bóng cần 2 vai trò khách đặt sân / chủ sân |
| 2:00–3:00 | Chạy `POST /api/auth/login` với `khach01` → mở **jwt.io** dán token vào, chỉ ra claim `sub`, `role`, `exp` |
| 3:00–4:30 | Mở `JwtAuthFilter.doFilterInternal()`, giải thích từng bước: đọc header → verify chữ ký → nạp `SecurityContext` |
| 4:30–6:00 | **Bộ ba minh chứng:** gọi `/api/bookings/statistics` không token → **401**; login `khach01` gọi lại → **403**; login `chusan` gọi lại → **200** |
| 6:00–7:00 | **Sửa code trực tiếp:** đổi hạn token từ 1 giờ xuống 10 giây, restart, login, đợi 10s, gọi lại → **401 token expired** |

### 🟢 Video Linh — "Thiết kế và kiểm thử RESTful API đặt sân" (5–6')

| Phút | Nội dung |
|---|---|
| 0:00–0:40 | Giới thiệu `booking-service`, mở Postman, chỉ cấu trúc collection 6 thư mục và các biến môi trường |
| 0:40–1:40 | Giải thích thiết kế URL: vì sao là `/api/bookings` số nhiều, vì sao hủy sân là `PUT /{id}/cancel` chứ không phải `DELETE` (giữ lịch sử cho chủ sân đối soát) |
| 1:40–3:00 | Đặt sân thành công: `POST /api/bookings` Sân số 2, 17:00–18:30 → **201**, chỉ `totalPrice = 675000` |
| 3:00–4:30 | **Chuỗi dữ liệu sai:** `startTime = 17:17` → 400 (R1); đá 4 tiếng → 400 (R2); đặt lúc 04:00 → 400 (R3); đặt ngày hôm qua → 400 (R5); đặt trùng khung 17:00–18:30 → **409** (R8) |
| 4:30–5:30 | Mở `BookingRequest.java` + `GlobalExceptionHandler.java` giải thích `@Valid` bắt lỗi rồi gom thành JSON lỗi ra sao |
| 5:30–6:00 | **Sửa code trực tiếp:** xóa `@NotNull` khỏi `fieldId`, restart, POST thiếu `fieldId` → không còn 400 mà thành 500 → chứng minh validation đang thật sự chạy. Trả lại code như cũ |

### 🟠 Video Hùng — "Giao tiếp giữa Booking Service và Field Service" (6–7')

| Phút | Nội dung |
|---|---|
| 0:00–0:40 | Giới thiệu `field-service`, mở sequence diagram đã vẽ, chỉ mũi tên Booking → Field |
| 0:40–1:40 | Mở `GET /api/fields` → 4 sân; `GET /api/fields/2/quote?date=2026-11-02&start=16:00&end=18:00` → giải thích 1h thường + 1h vàng = 750.000 |
| 1:40–2:40 | Mở `FieldClient.java` / cấu hình `RestTemplate`: chỉ URL đích, timeout 2s/3s, chỗ forward header `Authorization` |
| 2:40–3:30 | **Luồng thành công:** đặt Sân số 2 → bật log của cả 2 cửa sổ terminal, chỉ rõ Booking gọi sang Field lúc nào, Field trả gì |
| 3:30–4:15 | **E1:** đặt `fieldId = 99` → Field 404 → Booking trả **404** "Không tìm thấy sân có mã 99" |
| 4:15–5:00 | **E2:** đặt Sân số 3 đang bảo trì → **409** |
| 5:00–6:00 | **E3 (điểm nhấn):** `Ctrl+C` tắt hẳn `field-service` → đặt sân lại → Booking trả **503** chứ không sập. Chỉ khối `catch (ResourceAccessException)` trong code |
| 6:00–7:00 | **Sửa code trực tiếp:** đổi `readTimeout` từ 3s xuống 100ms, thêm `Thread.sleep(1000)` bên Field, chạy lại → **504 timeout** |

### 🟣 Video Nhật — "Quản lý dữ liệu và thanh toán" (6–7')

| Phút | Nội dung |
|---|---|
| 0:00–0:50 | Mở MySQL Workbench: chỉ **4 database riêng biệt** `auth_db`, `booking_db`, `field_db`, `payment_db`. Nhấn mạnh: không DB nào có khóa ngoại sang DB khác |
| 0:50–1:40 | Mở `Booking.java` (`@Entity`, `@Table`), `BookingRepository`, `application.properties` của 2 service → chỉ 2 `datasource.url` khác nhau |
| 1:40–2:30 | **Trước khi chạy:** `SELECT * FROM booking_db.bookings` và `payment_db.payments` → chụp số dòng hiện tại |
| 2:30–3:40 | Đặt sân qua API → `SELECT` lại `booking_db.bookings` → có dòng mới `id=12, total_price=675000, status=PENDING` |
| 3:40–4:40 | `POST /api/payments { bookingId: 12, method: "MOMO" }` → mở log chỉ rõ Payment **gọi API** `GET /api/bookings/12` để lấy 675.000, **không** truy vấn `booking_db`. `SELECT payments` → dòng mới `status=INIT` |
| 4:40–5:40 | `POST /api/payments/9/confirm` → Payment gọi ngược `PUT /api/bookings/12/status` → `SELECT` cả 2 bảng: booking `PAID`, payment `SUCCESS`, hai số tiền khớp |
| 5:40–7:00 | **Sửa code trực tiếp:** bật `show-sql`, gọi lại 1 API, chỉ câu `insert into bookings ...` thật trong log. Nói rủi ro nếu Payment đọc thẳng `booking_db` |

### Bảng theo dõi nộp video

| Người | Tên file | Thời lượng | Có tiếng nói | Có sửa code trực tiếp | Trạng thái |
|---|---|---|---|---|---|
| Hoàng | `KT1_Hoang_Auth_JWT.mp4` | 6–7' | ☐ | ☐ | ☐ |
| Linh | `KT1_Linh_RESTful_API.mp4` | 5–6' | ☐ | ☐ | ☐ |
| Hùng | `KT1_Hung_GiaoTiep_DichVu.mp4` | 6–7' | ☐ | ☐ | ☐ |
| Nhật | `KT1_Nhat_CSDL_ThanhToan.mp4` | 6–7' | ☐ | ☐ | ☐ |

---

## 5. KỊCH BẢN DEMO TRỰC TIẾP (12 phút — ai cũng nói)

Khác với video cá nhân (đi sâu từng phần), demo trước lớp chạy **một mạch nghiệp vụ đặt sân** nối tiếp nhau.

| # | Phút | Người | Nội dung |
|---|---|---|---|
| 1 | 2,0 | **Hoàng** | Đề tài + kiến trúc 4 dịch vụ; khởi chạy 4 service + MySQL, `netstat` chỉ 4 cổng 8080–8083 đang LISTEN |
| 2 | 2,5 | **Linh** | Postman: xem danh sách sân → đặt Sân số 2 17:00–18:30 (**201**, 675.000) → đặt lại đúng khung đó (**409** trùng lịch) → `startTime=17:17` (**400**) |
| 3 | 2,5 | **Hùng** | Đặt `fieldId=99` (**404**) → đặt Sân số 3 bảo trì (**409**) → tắt `field-service` → đặt lại (**503**) → bật lại, đặt thành công |
| 4 | 2,0 | **Nhật** | Thanh toán lượt đặt vừa tạo → mở MySQL `SELECT` cả `booking_db` và `payment_db` chứng minh dữ liệu đã lưu và khớp tiền |
| 5 | 2,5 | **Hoàng** | `/api/bookings/statistics` không token (**401**) → login `khach01` (**403**) → login `chusan` (**200**, xem doanh thu toàn sân) |
| 6 | 0,5 | cả 4 | Trả lời câu hỏi, mỗi người sửa 1 tham số trong code của mình (xem cột cuối mục 4) |

---

## 6. TIẾN ĐỘ 7 NGÀY (từ 26/10/2026)

| Ngày | Hoàng | Linh | Hùng | Nhật |
|---|---|---|---|---|
| D1 27/10 | ✅ *(đã xong)* kiến trúc, repo đa module, nhánh git, hợp đồng API, bộ Postman | Viết `Booking` @Entity + 2 DTO, bỏ `customerName` nhập tay | Dựng `field-service` + entity `Field` | Thêm JPA + MySQL cho Booking |
| D2 28/10 | `auth-service`, login trả JWT | Cài R1–R5 validation | Seed 4 sân, API `quote` tính giờ vàng | `payment-service` + entity `Payment` |
| D3 29/10 | `JwtAuthFilter` dùng chung, giao cho 3 bạn | Hoàn thiện Postman collection + `@PreAuthorize` | Booking → Field qua `RestTemplate` | Tách 4 DB, Payment gọi API Booking |
| D4 30/10 | Ma trận phân quyền, test 401/403 | R8 chống trùng lịch + R9 hủy sân | 4 tình huống lỗi E1–E4 | Luồng confirm → đổi booking sang PAID |
| D5 31/10 | Chương 1 + Chương 4 | Chương 2A + Postman collection | Chương 2B + sequence diagram | Chương 3 + Chương 5 + 15 ca kiểm thử |
| D6 01/11 | **Ráp báo cáo**, chạy thử end-to-end | **Quay video** | **Quay video** | **Quay video** |
| D7 02/11 | **Quay video** + tổng duyệt demo 2 lượt | Tổng duyệt | Tổng duyệt | Tổng duyệt |

---

## 7. QUY TẮC LÀM VIỆC

1. **Git:** mỗi người một nhánh đã tạo sẵn ở mục 0b, merge vào `develop` qua Pull Request, Hoàng review. Không ai push thẳng vào `main`.
2. **Không sửa code của người khác** — phát hiện lỗi thì báo chủ service. Riêng `pom.xml` cha và `common-security/` là của Hoàng: cần thêm thư viện thì báo Hoàng thêm, đừng tự sửa, vì cả 4 service cùng phụ thuộc hai thứ đó.
3. **Đọc `docs/api-contract.md` TRƯỚC KHI viết dòng code đầu tiên.** File đó đã chốt sẵn format JSON giữa Booking↔Field và Payment↔Booking, bảng ánh xạ lỗi, và 7 câu dễ cãi nhau. Sai format giữa 2 service là lỗi tốn thời gian nhất.
4. **Dùng chung bộ dữ liệu mẫu ở mục 1** — 4 sân, 2 tài khoản, giờ vàng 17:00–21:00. Tuyệt đối không ai tự chế dữ liệu riêng, nếu không video 4 người sẽ mâu thuẫn số liệu.
5. **Deadline nội bộ sớm hơn 1 ngày** — D6 phải chạy được end-to-end mới quay video.
6. **Minh chứng là bắt buộc:** phần nào chưa xong thì ghi rõ ở Chương 5 mục "Hạn chế" và nói trong video; đề bài nêu rõ thầy chấm theo mức độ đáp ứng thực tế.
