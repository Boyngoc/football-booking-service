# HỢP ĐỒNG API GIỮA CÁC DỊCH VỤ

> **File này là luật.** Ai đổi format trong đây phải báo cả nhóm trước, vì sai một tên trường là service của người khác chết.
> Chốt lần đầu: D1 (27/10/2026) — Linh × Hùng × Nhật × Hoàng.

---

## 0. Quy ước chung

| Mục | Quy ước |
|---|---|
| Kiểu dữ liệu ngày | `yyyy-MM-dd` — ví dụ `2026-11-02` |
| Kiểu dữ liệu giờ | `HH:mm` — ví dụ `17:00` (luôn 2 chữ số) |
| Tiền | số nguyên VND, **không** có dấu phân cách — `675000` |
| Header xác thực | `Authorization: Bearer <token>` |
| Encoding | UTF-8, `Content-Type: application/json` |

**Định dạng lỗi thống nhất cho cả 4 service** (Hoàng chốt, ai cũng phải trả đúng dạng này):

```json
{
  "timestamp": "2026-11-02T17:05:12",
  "status": 409,
  "error": "Conflict",
  "message": "Sân số 2 đã có người đặt khung 17:00-18:30 ngày 02/11/2026",
  "path": "/api/bookings"
}
```

Riêng lỗi 400 do validation có thêm trường `errors`:

```json
{
  "timestamp": "2026-11-02T17:05:12",
  "status": 400,
  "error": "Bad Request",
  "message": "Dữ liệu không hợp lệ",
  "path": "/api/bookings",
  "errors": { "startTime": "Giờ bắt đầu phải là bội số 30 phút" }
}
```

---

## 1. booking-service → field-service

**Chủ gọi:** Hùng · **Chủ bị gọi:** Hùng · **Người tiêu thụ kết quả:** Linh

### 1.1. Kiểm tra sân tồn tại và đang hoạt động

```
GET http://localhost:8082/api/fields/{id}
Authorization: Bearer <token của khách, được forward nguyên vẹn>
```

**200 OK**
```json
{
  "id": 2,
  "name": "Sân số 2",
  "type": "SAN_7",
  "pricePerHour": 300000,
  "goldenPricePerHour": 450000,
  "status": "ACTIVE"
}
```

| `status` | Booking phải làm gì |
|---|---|
| `ACTIVE` | đi tiếp |
| `MAINTENANCE` | dừng, trả **409** "Sân số 3 đang bảo trì, không nhận đặt lịch" |

**404 Not Found** → Booking trả **404** "Không tìm thấy sân có mã {id}"

### 1.2. Hỏi giá — booking-service KHÔNG tự tính tiền

```
GET http://localhost:8082/api/fields/{id}/quote?date=2026-11-02&start=17:00&end=18:30
```

**200 OK**
```json
{
  "fieldId": 2,
  "fieldName": "Sân số 2",
  "date": "2026-11-02",
  "startTime": "17:00",
  "endTime": "18:30",
  "normalHours": 0.0,
  "goldenHours": 1.5,
  "amount": 675000
}
```

Booking lấy đúng `amount` lưu vào cột `total_price`. Tuyệt đối không nhân chia lại.

**Ví dụ chuẩn để cả nhóm đối chiếu khi test:**

| Sân | Khung giờ | Giờ thường | Giờ vàng | `amount` |
|---|---|---|---|---|
| 2 | 17:00–18:30 | 0 | 1,5 | 675.000 |
| 2 | 16:00–18:00 | 1 | 1 | 750.000 |
| 1 | 08:00–09:30 | 1,5 | 0 | 300.000 |
| 4 | 19:00–21:00 | 0 | 2 | 1.800.000 |

### 1.3. Bảng ánh xạ lỗi (Hùng cài trong booking-service)

| Field trả về / sự cố | Booking trả cho khách | Mã |
|---|---|---|
| 200, `status = ACTIVE` | đi tiếp | — |
| 200, `status = MAINTENANCE` | "Sân {tên} đang bảo trì, không nhận đặt lịch" | **409** |
| 404 | "Không tìm thấy sân có mã {id}" | **404** |
| `ResourceAccessException` (service tắt) | "Dịch vụ quản lý sân tạm thời không khả dụng, vui lòng thử lại sau" | **503** |
| timeout > 3s | "Quá thời gian chờ phản hồi từ dịch vụ quản lý sân" | **504** |
| 5xx bất kỳ | "Dịch vụ quản lý sân gặp sự cố" | **502** |

---

## 2. payment-service → booking-service

**Chủ gọi:** Nhật · **Chủ bị gọi:** Linh

### 2.1. Lấy số tiền thật của lượt đặt

Payment **không** đọc `booking_db`. Khi khách gửi `POST /api/payments { bookingId: 12 }`, Payment phải tự đi hỏi:

```
GET http://localhost:8081/api/bookings/{id}
```

**200 OK**
```json
{
  "id": 12,
  "fieldId": 2,
  "fieldName": "Sân số 2",
  "customerUsername": "khach01",
  "bookingDate": "2026-11-02",
  "startTime": "17:00",
  "endTime": "18:30",
  "totalPrice": 675000,
  "status": "PENDING",
  "createdAt": "2026-10-26T20:14:03"
}
```

Payment lấy `totalPrice` làm `amount`. Nếu client gửi kèm số tiền khác thì **bỏ qua**.

| Booking trả về | Payment trả cho khách | Mã |
|---|---|---|
| 200, `status = PENDING` hoặc `CONFIRMED` | tạo giao dịch | **201** |
| 200, `status = PAID` | "Lượt đặt này đã thanh toán rồi" | **409** |
| 200, `status = CANCELLED` | "Lượt đặt đã bị hủy, không thể thanh toán" | **409** |
| 404 | "Không tìm thấy lượt đặt có mã {id}" | **404** |
| service tắt | "Dịch vụ đặt sân tạm thời không khả dụng" | **503** |

### 2.2. Báo thanh toán thành công — đổi trạng thái lượt đặt

```
PUT http://localhost:8081/api/bookings/{id}/status
Content-Type: application/json
Authorization: Bearer <token>

{ "status": "PAID" }
```

**200 OK** → trả về đối tượng booking đã cập nhật (cùng định dạng mục 2.1).

> Linh lưu ý: endpoint này **chỉ** nhận giá trị `PAID` hoặc `CANCELLED`, giá trị khác trả 400. Đây là endpoint nội bộ giữa 2 service, không phải cho khách gọi.

---

## 3. Mọi service → auth-service

**Chủ sở hữu:** Hoàng

### 3.1. Đăng nhập

```
POST http://localhost:8080/api/auth/login
{ "username": "khach01", "password": "123456" }
```

**200 OK**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "khach01",
  "fullName": "Lê Văn Cường",
  "role": "CUSTOMER",
  "expiresIn": 3600
}
```

**401** nếu sai tài khoản hoặc mật khẩu.

### 3.2. Nội dung bắt buộc trong token

```json
{
  "sub": "khach01",
  "role": "CUSTOMER",
  "fullName": "Lê Văn Cường",
  "iat": 1793000000,
  "exp": 1793003600
}
```

- 3 service còn lại **không gọi lại auth-service** để kiểm tra token. Chúng tự verify chữ ký bằng `app.jwt.secret` dùng chung → đây là ưu điểm của JWT so với session, phải nói được khi demo.
- Đổi `app.jwt.secret` thì phải đổi ở **cả 4** `application.properties`, nếu không token ký ở auth sẽ bị 3 service kia từ chối.

### 3.3. Ai được gọi gì

| Endpoint | Chưa đăng nhập | CUSTOMER | OWNER |
|---|---|---|---|
| `GET /api/fields`, `GET /api/fields/{id}` | ✅ 200 | ✅ | ✅ |
| `GET /api/fields/{id}/quote` | ✅ 200 | ✅ | ✅ |
| `PUT /api/fields/{id}/status` | 401 | **403** | ✅ |
| `POST /api/bookings` | 401 | ✅ | ✅ |
| `GET /api/bookings/me` | 401 | ✅ | ✅ |
| `GET /api/bookings` | 401 | **403** | ✅ |
| `GET /api/bookings/{id}` — của mình | 401 | ✅ | ✅ |
| `GET /api/bookings/{id}` — của người khác | 401 | **403** | ✅ |
| `PUT /api/bookings/{id}/cancel` — của mình | 401 | ✅ | ✅ |
| `PUT /api/bookings/{id}/cancel` — của người khác | 401 | **403** | ✅ |
| `GET /api/bookings/statistics` | 401 | **403** | ✅ |
| `POST /api/payments` | 401 | ✅ | ✅ |
| `GET /health` (cả 4 service) | ✅ 200 | ✅ | ✅ |

---

## 4. Những chỗ dễ cãi nhau — chốt luôn

| Vấn đề | Chốt |
|---|---|
| Client có được gửi `customerName` khi đặt sân không? | **Không.** Lấy từ claim `sub` trong token. Gửi lên cũng bị bỏ qua. |
| Client có được gửi `totalPrice` không? | **Không.** Booking hỏi field-service. |
| Hủy sân dùng `DELETE` hay `PUT /cancel`? | **`PUT /{id}/cancel`** — giữ lại bản ghi để chủ sân đối soát doanh thu. |
| `endTime` 18:30 nghĩa là sân bận đến đúng 18:30 hay 18:29? | Khoảng **nửa mở `[start, end)`** — đặt tiếp 18:30–20:00 là hợp lệ, không tính trùng. |
| Giờ vàng tính thế nào khi đá vắt qua 17:00? | Tính theo **từng phút** rơi vào 17:00–21:00, không làm tròn cả ca. |
| Service nào forward token? | Mọi lời gọi liên dịch vụ đều forward nguyên header `Authorization`. |
| Mã lỗi khi service đích chết? | **503**, không bao giờ để lọt 500 ra ngoài. |
