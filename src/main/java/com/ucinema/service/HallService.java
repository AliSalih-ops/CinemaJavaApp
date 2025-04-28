package com.ucinema.service;

import com.ucinema.dao.HallDAO;
import com.ucinema.model.datastructures.HallGraph;
import com.ucinema.model.entities.Hall;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for cinema hall-related operations.
 */
public class HallService {
    private final HallDAO hallDAO;
    private final HallGraph hallGraph;

    public HallService() {
        this.hallDAO = new HallDAO();
        this.hallGraph = new HallGraph();
        initializeHallGraph();
    }

    /**
     * Initialize the hall graph with hall data from the database
     */
    private void initializeHallGraph() {
        List<Hall> halls = hallDAO.findAll();
        if (halls != null) {
            for (Hall hall : halls) {
                createSeatsForHall(hall);
            }
        }
    }

    /**
     * Create seats for a hall and add them to the graph
     * @param hall The hall to create seats for
     */
    private void createSeatsForHall(Hall hall) {
        // This method would parse the seating layout from the hall entity
        // and create the corresponding seats in the graph
        // For simplicity, we'll create a basic layout if none exists

        if (hall.getSeatingLayout() == null || hall.getSeatingLayout().isEmpty()) {
            // Create a simple layout - 10 rows (A-J) with 10 seats each
            int rows = 10;
            int seatsPerRow = 10;

            for (int i = 0; i < rows; i++) {
                char rowChar = (char) ('A' + i);
                for (int j = 1; j <= seatsPerRow; j++) {
                    String seatId = rowChar + String.valueOf(j);
                    String seatType = "standard";

                    // Make some seats premium (e.g., middle rows)
                    if (i >= 3 && i <= 6 && j >= 3 && j <= 8) {
                        seatType = "premium";
                    }

                    // Make some seats accessible (e.g., first row)
                    if (i == 0 && (j == 1 || j == seatsPerRow)) {
                        seatType = "accessible";
                    }

                    HallGraph.Seat seat = new HallGraph.Seat(seatId, hall.getId(), i, j, seatType);
                    hallGraph.addSeat(seat);

                    // Add edges to adjacent seats
                    if (j > 1) {
                        String leftSeatId = rowChar + String.valueOf(j - 1);
                        if (hallGraph.seatExists(leftSeatId)) {
                            hallGraph.addEdge(seatId, leftSeatId);
                        }
                    }

                    if (i > 0) {
                        String aboveSeatId = (char) ('A' + (i - 1)) + String.valueOf(j);
                        if (hallGraph.seatExists(aboveSeatId)) {
                            hallGraph.addEdge(seatId, aboveSeatId);
                        }
                    }
                }
            }

            // Update the hall with the seating layout (simplified - would be JSON in reality)
            StringBuilder layoutBuilder = new StringBuilder();
            layoutBuilder.append("Rows:").append(rows).append(",Seats:").append(seatsPerRow);
            hall.setSeatingLayout(layoutBuilder.toString());
            hallDAO.update(hall);
        }
        // In a real implementation, we would parse the JSON layout from the hall entity
    }

    /**
     * Add a new cinema hall
     * @param name Hall name
     * @param capacity Hall capacity
     * @param location Hall location
     * @param type Hall type
     * @return The added hall or null if operation failed
     */
    public Hall addHall(String name, int capacity, String location, String type) {
        Hall hall = new Hall(name, capacity, location, type);
        Hall savedHall = hallDAO.save(hall);

        if (savedHall != null) {
            createSeatsForHall(savedHall);
        }

        return savedHall;
    }

    /**
     * Find a hall by ID
     * @param hallId The hall ID
     * @return The hall or null if not found
     */
    public Hall findHallById(int hallId) {
        return hallDAO.findById(hallId);
    }

    /**
     * Update hall information
     * @param hall The hall to update
     * @return The updated hall
     */
    public Hall updateHall(Hall hall) {
        return hallDAO.update(hall);
    }

    /**
     * Delete a hall
     * @param hall The hall to delete
     * @return True if successful
     */
    public boolean deleteHall(Hall hall) {
        return hallDAO.delete(hall);
    }

    /**
     * Get all halls
     * @return List of all halls
     */
    public List<Hall> getAllHalls() {
        return hallDAO.findAll();
    }

    /**
     * Find halls by location
     * @param location The location to search for
     * @return List of halls at the specified location
     */
    public List<Hall> findHallsByLocation(String location) {
        return hallDAO.findByLocation(location);
    }

    /**
     * Find halls by type
     * @param type The hall type to search for
     * @return List of halls of the specified type
     */
    public List<Hall> findHallsByType(String type) {
        return hallDAO.findByType(type);
    }

    /**
     * Find halls with minimum capacity
     * @param minCapacity The minimum capacity required
     * @return List of halls with at least the specified capacity
     */
    public List<Hall> findHallsByMinCapacity(int minCapacity) {
        return hallDAO.findByMinCapacity(minCapacity);
    }

    /**
     * Get all seats in a hall
     * @param hallId The hall ID
     * @return List of seats in the hall
     */
    public List<HallGraph.Seat> getSeatsInHall(int hallId) {
        return hallGraph.getSeatsInHall(hallId);
    }

    /**
     * Get available seats in a hall
     * @param hallId The hall ID
     * @return List of available seats
     */
    public List<HallGraph.Seat> getAvailableSeats(int hallId) {
        return hallGraph.getAvailableSeats(hallId);
    }

    /**
     * Find best adjacent seats for a group
     * @param hallId The hall ID
     * @param seatCount Number of seats needed
     * @return List of adjacent seats or empty list if not available
     */
    public List<HallGraph.Seat> findBestAdjacentSeats(int hallId, int seatCount) {
        return hallGraph.findBestAdjacentSeats(hallId, seatCount);
    }

    /**
     * Generate a seating chart for a hall
     * @param hallId The hall ID
     * @return 2D array representing the seating chart
     */
    public String[][] generateSeatingChart(int hallId) {
        return hallGraph.generateSeatingChart(hallId);
    }

    /**
     * Reserve a seat
     * @param seatId The seat ID
     * @return True if successful
     */
    public boolean reserveSeat(String seatId) {
        return hallGraph.reserveSeat(seatId);
    }

    /**
     * Cancel a seat reservation
     * @param seatId The seat ID
     * @return True if successful
     */
    public boolean cancelSeatReservation(String seatId) {
        return hallGraph.cancelReservation(seatId);
    }

    /**
     * Get a seat by ID
     * @param seatId The seat ID
     * @return The seat or null if not found
     */
    public HallGraph.Seat getSeat(String seatId) {
        return hallGraph.getSeat(seatId);
    }
}