package com.ucinema.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class representing a cinema hall in the system.
 */
@Entity
@Table(name = "halls")
public class Hall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column
    private String location;

    @Column
    private String type; // e.g., "standard", "IMAX", "VIP"

    @Column(name = "seating_layout", length = 10000)
    private String seatingLayout; // JSON representation of the seating layout

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor required by JPA
    public Hall() {
    }

    // Constructor for creating a new hall
    public Hall(String name, int capacity, String location, String type) {
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.type = type;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeatingLayout() {
        return seatingLayout;
    }

    public void setSeatingLayout(String seatingLayout) {
        this.seatingLayout = seatingLayout;
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
        return "Hall{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}