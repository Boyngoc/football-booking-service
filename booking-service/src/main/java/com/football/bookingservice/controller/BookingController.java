package com.football.bookingservice.controller;

import com.football.bookingservice.dto.StatisticsResponse;
import com.football.bookingservice.exception.ResourceNotFoundException;
import com.football.bookingservice.model.Booking;
import com.football.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Welcome to Booking Service");
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of("service", "Booking Service", "version", "1.0", "status", "UP");
    }

    // ===== BAI 5: dat TRUOC /{id} de khong bi nham la id =====

    @GetMapping("/search")
    public List<Booking> search(@RequestParam String name) {
        return bookingService.searchByCustomer(name);
    }

    @GetMapping("/paged")
    public List<Booking> paged(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return bookingService.findPage(page, size);
    }

    @GetMapping("/statistics")
    public StatisticsResponse statistics() {
        return bookingService.statistics();
    }

    // ===== BAI 3 + 4: CRUD =====

    @GetMapping
    public List<Booking> getAll() {
        return bookingService.findAll();
    }

    @GetMapping("/{id}")
    public Booking getById(@PathVariable Long id) {
        return bookingService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dat lich id=" + id));
    }

    @PostMapping
    public ResponseEntity<Booking> create(@Valid @RequestBody Booking booking) {
        return ResponseEntity.status(201).body(bookingService.create(booking));
    }

    @PutMapping("/{id}")
    public Booking update(@PathVariable Long id, @Valid @RequestBody Booking booking) {
        return bookingService.update(id, booking)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dat lich id=" + id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        boolean deleted = bookingService.delete(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Khong tim thay dat lich id=" + id);
        }
        return ResponseEntity.ok(Map.of("message", "Da xoa dat lich id=" + id));
    }
}