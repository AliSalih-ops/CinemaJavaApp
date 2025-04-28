package com.ucinema.service;

import com.ucinema.dao.ReservationDAO;
import com.ucinema.model.datastructures.ReservationLinkedList;
import com.ucinema.model.entities.MovieSchedule;
import com.ucinema.model.entities.Reservation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for reservation-related operations.
 */
public class ReservationService {
    private final ReservationDAO reservationDAO;
    private final MovieScheduleService scheduleService;
    private final HallService hallService;
    private final ReservationLinkedList reservationList;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.scheduleService = new MovieScheduleService();
        this.hallService = new HallService();
        this.reservationList = new ReservationLinkedList();
        initializeReservationList();
    }

    /**
     * Initialize the reservation list with data from the database
     */
    private void initializeReservationList() {
        List<Reservation> reservations = reservationDAO.findAll();
        if (reservations != null) {
            for (Reservation reservation : reservations) {
                reservationList.add(reservation);
            }
        }
    }

    /**
     * Make a new reservation
     * @param studentId Student ID
     * @param scheduleId Schedule ID
     * @param seatId Seat ID
     * @return The created reservation or null if operation failed
     */
    public Reservation makeReservation(int studentId, int scheduleId, String seatId) {
        // Check if seat is already reserved
        if (reservationDAO.isSeatReserved(scheduleId, seatId)) {
            throw new IllegalStateException("This seat is already reserved");
        }

        // Get the schedule to get the price
        MovieSchedule schedule = scheduleService.findScheduleById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule not found");
        }

        // Reserve the seat in the hall graph
        boolean seatReserved = hallService.reserveSeat(seatId);
        if (!seatReserved) {
            throw new IllegalStateException("Failed to reserve seat");
        }

        // Create and save the reservation
        Reservation reservation = new Reservation(studentId, scheduleId, seatId, schedule.getPrice());
        Reservation savedReservation = reservationDAO.save(reservation);

        // Add to linked list
        if (savedReservation != null) {
            reservationList.add(savedReservation);
        } else {
            // If saving fails, unreserve the seat
            hallService.cancelSeatReservation(seatId);
        }

        return savedReservation;
    }

    /**
     * Cancel a reservation
     * @param reservationId The reservation ID
     * @return True if successful
     */
    public boolean cancelReservation(int reservationId) {
        Reservation reservation = findReservationById(reservationId);
        if (reservation == null) {
            return false;
        }

        // Cancel the reservation in the database
        boolean cancelled = reservationDAO.cancelReservation(reservationId);

        if (cancelled) {
            // Unreserve the seat in the hall graph
            hallService.cancelSeatReservation(reservation.getSeatId());

            // Remove from linked list
            reservationList.remove(reservationId);

            return true;
        }

        return false;
    }

    /**
     * Find a reservation by ID
     * @param reservationId The reservation ID
     * @return The reservation or null if not found
     */
    public Reservation findReservationById(int reservationId) {
        // Try to find in linked list first for faster access
        Reservation reservation = reservationList.find(reservationId);
        if (reservation != null) {
            return reservation;
        }

        // If not in linked list, fetch from database
        return reservationDAO.findById(reservationId);
    }

    /**
     * Find all reservations for a student
     * @param studentId The student ID
     * @return Array of reservations
     */
    public Reservation[] findReservationsByStudent(int studentId) {
        return reservationList.findByStudent(studentId);
    }

    /**
     * Get all reservations from linked list
     * @return Array of all reservations
     */
    public Reservation[] getAllReservationsFromList() {
        return reservationList.toArray();
    }

    /**
     * Get all reservations from database
     * @return List of all reservations
     */
    public List<Reservation> getAllReservations() {
        return reservationDAO.findAll();
    }

    /**
     * Find reservations for a specific schedule
     * @param scheduleId The schedule ID
     * @return List of reservations
     */
    public List<Reservation> findReservationsBySchedule(int scheduleId) {
        return reservationDAO.findBySchedule(scheduleId);
    }

    /**
     * Get all reserved seats for a schedule
     * @param scheduleId The schedule ID
     * @return List of reserved seat IDs
     */
    public List<String> getReservedSeats(int scheduleId) {
        return reservationDAO.getReservedSeats(scheduleId);
    }

    /**
     * Check if a seat is reserved for a schedule
     * @param scheduleId The schedule ID
     * @param seatId The seat ID
     * @return True if the seat is reserved
     */
    public boolean isSeatReserved(int scheduleId, String seatId) {
        return reservationDAO.isSeatReserved(scheduleId, seatId);
    }

    /**
     * Find reservations by status
     * @param status The reservation status
     * @return List of reservations with the specified status
     */
    public List<Reservation> findReservationsByStatus(String status) {
        return reservationDAO.findByStatus(status);
    }

    /**
     * Find reservations made within a time range
     * @param start The start time
     * @param end The end time
     * @return List of reservations made within the time range
     */
    public List<Reservation> findReservationsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return reservationDAO.findByReservationTimeRange(start, end);
    }
}