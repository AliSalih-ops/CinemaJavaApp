package com.ucinema.model.datastructures;

import java.util.*;

/**
 * A graph implementation to represent cinema hall seating arrangements.
 * Each seat is a vertex in the graph, with edges representing adjacency.
 */
public class HallGraph {
    private Map<String, Seat> seats;
    private Map<String, List<String>> adjacencyList;

    public HallGraph() {
        seats = new HashMap<>();
        adjacencyList = new HashMap<>();
    }

    /**
     * Represents a seat in the hall
     */
    public static class Seat {
        private String id; // E.g., "A1", "B5"
        private boolean reserved;
        private String type; // E.g., "standard", "premium", "accessible"
        private int hallId;
        private int row;
        private int column;

        public Seat(String id, int hallId, int row, int column, String type) {
            this.id = id;
            this.hallId = hallId;
            this.row = row;
            this.column = column;
            this.type = type;
            this.reserved = false;
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public boolean isReserved() {
            return reserved;
        }

        public void setReserved(boolean reserved) {
            this.reserved = reserved;
        }

        public String getType() {
            return type;
        }

        public int getHallId() {
            return hallId;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return id + (reserved ? " (Reserved)" : " (Available)");
        }
    }

    /**
     * Add a seat to the graph
     * @param seat The seat to add
     */
    public void addSeat(Seat seat) {
        seats.put(seat.getId(), seat);
        adjacencyList.put(seat.getId(), new ArrayList<>());
    }

    /**
     * Add an edge between two seats (indicating they are adjacent)
     * @param seat1Id The ID of the first seat
     * @param seat2Id The ID of the second seat
     */
    public void addEdge(String seat1Id, String seat2Id) {
        if (!seats.containsKey(seat1Id) || !seats.containsKey(seat2Id)) {
            throw new IllegalArgumentException("Both seats must exist in the graph");
        }

        // Add bidirectional edge
        adjacencyList.get(seat1Id).add(seat2Id);
        adjacencyList.get(seat2Id).add(seat1Id);
    }

    /**
     * Get all adjacent seats to a given seat
     * @param seatId The ID of the seat
     * @return List of adjacent seat IDs
     */
    public List<String> getAdjacentSeats(String seatId) {
        if (!seats.containsKey(seatId)) {
            throw new IllegalArgumentException("Seat does not exist in the graph");
        }
        return adjacencyList.get(seatId);
    }

    /**
     * Get a seat by its ID
     * @param seatId The seat ID
     * @return The seat or null if not found
     */
    public Seat getSeat(String seatId) {
        return seats.get(seatId);
    }

    /**
     * Reserve a seat
     * @param seatId The ID of the seat to reserve
     * @return True if successful, false if already reserved
     */
    public boolean reserveSeat(String seatId) {
        Seat seat = seats.get(seatId);
        if (seat == null || seat.isReserved()) {
            return false;
        }
        seat.setReserved(true);
        return true;
    }

    /**
     * Cancel a seat reservation
     * @param seatId The ID of the seat
     * @return True if successful, false if not reserved
     */
    public boolean cancelReservation(String seatId) {
        Seat seat = seats.get(seatId);
        if (seat == null || !seat.isReserved()) {
            return false;
        }
        seat.setReserved(false);
        return true;
    }

    /**
     * Get all seats in a specific hall
     * @param hallId The hall ID
     * @return List of seats in the hall
     */
    public List<Seat> getSeatsInHall(int hallId) {
        List<Seat> hallSeats = new ArrayList<>();
        for (Seat seat : seats.values()) {
            if (seat.getHallId() == hallId) {
                hallSeats.add(seat);
            }
        }
        return hallSeats;
    }

    /**
     * Find available seats in a hall
     * @param hallId The hall ID
     * @return List of available seats
     */
    public List<Seat> getAvailableSeats(int hallId) {
        List<Seat> availableSeats = new ArrayList<>();
        for (Seat seat : seats.values()) {
            if (seat.getHallId() == hallId && !seat.isReserved()) {
                availableSeats.add(seat);
            }
        }
        return availableSeats;
    }

    /**
     * Find the best available adjacent seats for a group
     * @param hallId The hall ID
     * @param count Number of seats needed
     * @return List of adjacent seats or empty list if not available
     */
    public List<Seat> findBestAdjacentSeats(int hallId, int count) {
        // This is a simplified implementation - a real one would consider more factors

        // First get all available seats in the hall
        List<Seat> availableSeats = getAvailableSeats(hallId);

        // Sort by row and column for easier processing
        availableSeats.sort(Comparator
                .comparing(Seat::getRow)
                .thenComparing(Seat::getColumn));

        // Look for consecutive seats in the same row
        int currentRow = -1;
        List<Seat> consecutiveSeats = new ArrayList<>();

        for (Seat seat : availableSeats) {
            if (seat.getRow() != currentRow) {
                // Starting a new row
                currentRow = seat.getRow();
                consecutiveSeats.clear();
                consecutiveSeats.add(seat);
            } else if (seat.getColumn() == consecutiveSeats.get(consecutiveSeats.size() - 1).getColumn() + 1) {
                // This seat is adjacent to the last one in our list
                consecutiveSeats.add(seat);

                // If we have enough consecutive seats, return them
                if (consecutiveSeats.size() == count) {
                    return new ArrayList<>(consecutiveSeats);
                }
            } else {
                // This seat is in the same row but not consecutive
                consecutiveSeats.clear();
                consecutiveSeats.add(seat);
            }
        }

        // If we couldn't find enough consecutive seats, return empty list
        return new ArrayList<>();
    }

    /**
     * Generate a visual representation of the seating arrangement
     * @param hallId The hall ID
     * @return 2D array representing the seating chart
     */
    public String[][] generateSeatingChart(int hallId) {
        List<Seat> hallSeats = getSeatsInHall(hallId);

        if (hallSeats.isEmpty()) {
            return new String[0][0];
        }

        // Find dimensions of the hall
        int maxRow = 0;
        int maxCol = 0;

        for (Seat seat : hallSeats) {
            maxRow = Math.max(maxRow, seat.getRow());
            maxCol = Math.max(maxCol, seat.getColumn());
        }

        // Create chart with room for labels (hence the +1)
        String[][] chart = new String[maxRow + 1][maxCol + 1];

        // Initialize with empty spaces
        for (int i = 0; i < chart.length; i++) {
            for (int j = 0; j < chart[i].length; j++) {
                chart[i][j] = "   ";
            }
        }

        // Fill in seats
        for (Seat seat : hallSeats) {
            String status = seat.isReserved() ? "X" : "O";
            chart[seat.getRow()][seat.getColumn()] = seat.getId() + status;
        }

        return chart;
    }

    /**
     * Get all seats in the graph
     * @return Collection of all seats
     */
    public Collection<Seat> getAllSeats() {
        return seats.values();
    }

    /**
     * Check if a seat exists in the graph
     * @param seatId The seat ID
     * @return True if the seat exists
     */
    public boolean seatExists(String seatId) {
        return seats.containsKey(seatId);
    }
}