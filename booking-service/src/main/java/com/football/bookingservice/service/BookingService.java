package com.football.bookingservice.service;

import com.football.bookingservice.dto.StatisticsResponse;
import com.football.bookingservice.model.Booking;
import com.football.bookingservice.model.BookingStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final List<Booking> bookings = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public BookingService() {
        create(new Booking(null, "Nguyen Van An", "0901234567", "San so 1",
                LocalDate.of(2026, 9, 25), LocalTime.of(18, 0), LocalTime.of(19, 30),
                300000.0, BookingStatus.CONFIRMED));
        create(new Booking(null, "Tran Thi Binh", "0912345678", "San so 2",
                LocalDate.of(2026, 9, 26), LocalTime.of(20, 0), LocalTime.of(21, 30),
                350000.0, BookingStatus.PENDING));
    }

    public List<Booking> findAll() {
        return bookings;
    }

    public Optional<Booking> findById(Long id) {
        return bookings.stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    public Booking create(Booking booking) {
        booking.setId(idGenerator.incrementAndGet());
        if (booking.getStatus() == null) {
            booking.setStatus(BookingStatus.PENDING);
        }
        bookings.add(booking);
        return booking;
    }

    public Optional<Booking> update(Long id, Booking data) {
        return findById(id).map(existing -> {
            existing.setCustomerName(data.getCustomerName());
            existing.setPhone(data.getPhone());
            existing.setFieldName(data.getFieldName());
            existing.setBookingDate(data.getBookingDate());
            existing.setStartTime(data.getStartTime());
            existing.setEndTime(data.getEndTime());
            existing.setPrice(data.getPrice());
            if (data.getStatus() != null) {
                existing.setStatus(data.getStatus());
            }
            return existing;
        });
    }

    public boolean delete(Long id) {
        return bookings.removeIf(b -> b.getId().equals(id));
    }

    // ===== BAI 5 =====

    // A - Tim theo ten khach hang
    public List<Booking> searchByCustomer(String keyword) {
        String k = (keyword == null) ? "" : keyword.toLowerCase();
        return bookings.stream()
                .filter(b -> b.getCustomerName() != null
                        && b.getCustomerName().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }

    // B - Phan trang
    public List<Booking> findPage(int page, int size) {
        int from = page * size;
        if (from >= bookings.size()) {
            return new ArrayList<>();
        }
        int to = Math.min(from + size, bookings.size());
        return bookings.subList(from, to);
    }

    // C - Thong ke
    public StatisticsResponse statistics() {
        long total = bookings.size();
        double revenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PAID)
                .mapToDouble(b -> b.getPrice() == null ? 0 : b.getPrice())
                .sum();
        return new StatisticsResponse(
                total,
                count(BookingStatus.PENDING),
                count(BookingStatus.CONFIRMED),
                count(BookingStatus.PAID),
                count(BookingStatus.CANCELLED),
                revenue);
    }

    private long count(BookingStatus status) {
        return bookings.stream().filter(b -> b.getStatus() == status).count();
    }
}