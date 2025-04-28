package com.ucinema.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class representing a seat reservation in the system.
 */
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "student_id", nullable = false)
    private int studentId;

    @Column(name = "schedule_id", nullable = false)
    private int scheduleId;

    @Column(name = "seat_id", nullable = false)
    private String seatId;

    @Column(name = "reservation_time", nullable = false)
    private LocalDateTime reservationTime;

    @Column
    private double price;

    @Column
    private String status; // e.g., "confirmed", "cancelled", "pending"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor required by JPA
    public Reservation() {
    }

    // Constructor for creating a new reservation
    public Reservation(int studentId, int scheduleId, String seatId, double price) {
        this.studentId = studentId;
        this.scheduleId = scheduleId;
        this.seatId = seatId;
        this.price = price;
        this.status = "confirmed";
        this.reservationTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public LocalDateTime getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", scheduleId=" + scheduleId +
                ", seatId='" + seatId + '\'' +
                ", reservationTime=" + reservationTime +
                ", status='" + status + '\'' +
                '}';
    }
}