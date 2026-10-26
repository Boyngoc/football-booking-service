package com.football.bookingservice.dto;

public class StatisticsResponse {
    private long total;
    private long pending;
    private long confirmed;
    private long paid;
    private long cancelled;
    private double totalRevenue;

    public StatisticsResponse(long total, long pending, long confirmed,
            long paid, long cancelled, double totalRevenue) {
        this.total = total;
        this.pending = pending;
        this.confirmed = confirmed;
        this.paid = paid;
        this.cancelled = cancelled;
        this.totalRevenue = totalRevenue;
    }

    public long getTotal() {
        return total;
    }

    public long getPending() {
        return pending;
    }

    public long getConfirmed() {
        return confirmed;
    }

    public long getPaid() {
        return paid;
    }

    public long getCancelled() {
        return cancelled;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}