package com.ucinema.service;

import com.ucinema.dao.HallDAO;
import com.ucinema.model.datastructures.HallGraph;
import com.ucinema.model.entities.Hall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for cinema hall-related operations.
 */
public class HallService {
    private final HallDAO hallDAO;
    private final HallGraph hallGraph;

    // Predefined layouts for standard capacity halls
    private final Map<Integer, int[]> STANDARD_LAYOUTS = new HashMap<>();

    public HallService() {
        this.hallDAO = new HallDAO();
        this.hallGraph = new HallGraph();
        initializeStandardLayouts();
        initializeHallGraph();
    }

    /**
     * Initialize standard layouts for fixed capacity options
     */
    private void initializeStandardLayouts() {
        // Format: capacity -> [rows, seatsPerRow, hasCenterAisle]
        STANDARD_LAYOUTS.put(25, new int[]{5, 5, 0}); // 5 rows x 5 seats
        STANDARD_LAYOUTS.put(50, new int[]{5, 10, 0}); // 5 rows x 10 seats
        STANDARD_LAYOUTS.put(75, new int[]{6, 13, 1}); // 6 rows x 13 seats with center aisle
        STANDARD_LAYOUTS.put(100, new int[]{8, 13, 1}); // 8 rows x 13 seats with center aisle
        STANDARD_LAYOUTS.put(150, new int[]{10, 16, 0}); // 10 rows x 16 seats without center aisle
        STANDARD_LAYOUTS.put(200, new int[]{11, 19, 1}); // 11 rows x 19 seats with center aisle
    }

    /**
     * Initialize the hall graph with hall data from the database
     */
    private void initializeHallGraph() {
        List<Hall> halls = hallDAO.findAll();
        if (halls != null) {
            System.out.println("Initializing hall graph with " + halls.size() + " halls");
            for (Hall hall : halls) {
                createSeatsForHall(hall);
            }
        } else {
            System.out.println("No halls found in database for initialization");
        }
    }

    /**
     * Get the standard layout for a given capacity
     * @param capacity The hall capacity
     * @return int[] array with [rows, seatsPerRow, hasCenterAisle]
     */
    private int[] getStandardLayout(int capacity) {
        // If capacity matches one of our standards, use that layout
        if (STANDARD_LAYOUTS.containsKey(capacity)) {
            return STANDARD_LAYOUTS.get(capacity);
        }

        // Otherwise find the closest standard capacity
        int closestCapacity = 25; // Default
        int minDiff = Math.abs(capacity - closestCapacity);

        for (Integer standardCapacity : STANDARD_LAYOUTS.keySet()) {
            int diff = Math.abs(capacity - standardCapacity);
            if (diff < minDiff) {
                minDiff = diff;
                closestCapacity = standardCapacity;
            }
        }

        System.out.println("Using standard layout for capacity " + closestCapacity +
                " instead of requested " + capacity);

        return STANDARD_LAYOUTS.get(closestCapacity);
    }

    /**
     * Create seats for a hall and add them to the graph using standardized layouts
     * @param hall The hall to create seats for
     */
    private void createSeatsForHall(Hall hall) {
        System.out.println("Creating seats for hall: " + hall.getName() + " (ID: " + hall.getId() + ")");

        // Check if hall already has seats in the graph
        List<HallGraph.Seat> existingSeats = hallGraph.getSeatsInHall(hall.getId());
        if (existingSeats != null && !existingSeats.isEmpty()) {
            System.out.println("Hall already has " + existingSeats.size() + " seats, skipping creation");
            return;
        }

        // Get standardized layout
        int[] layout = getStandardLayout(hall.getCapacity());
        int totalRows = layout[0];
        int maxSeatsInRow = layout[1];
        boolean hasCenterAisle = layout[2] == 1;

        System.out.println("Creating a " + totalRows + " x " + maxSeatsInRow +
                (hasCenterAisle ? " layout with center aisle" : " layout") +
                " for hall " + hall.getName() + " (capacity: " + hall.getCapacity() + ")");

        int seatsCreated = 0;
        int targetCapacity = hall.getCapacity();

        // Create a flattened list of all seat positions to create
        List<int[]> seatPositions = new ArrayList<>();

        // First, generate all possible seat positions based on layout
        for (int i = 0; i < totalRows; i++) {
            char rowChar = (char) ('A' + i);
            int seatsInThisRow;

            // For small halls (25 seats)
            if (targetCapacity == 25) {
                seatsInThisRow = 5; // All rows have 5 seats
            }
            // For medium halls (50 seats)
            else if (targetCapacity == 50) {
                seatsInThisRow = 10; // All rows have 10 seats
            }
            // Special handling for 150-seat halls
            else if (targetCapacity == 150) {
                if (i < 2) {
                    // First two rows have 12 seats (75% of max)
                    seatsInThisRow = 12;
                } else if (i < 4) {
                    // Next two rows have 14 seats (87.5% of max)
                    seatsInThisRow = 14;
                } else {
                    // All other rows have full seats
                    seatsInThisRow = maxSeatsInRow;
                }
            }
            // For larger halls, create a curved layout
            else {
                if (i == 0) {
                    // First row has 80% of max seats
                    seatsInThisRow = (int) Math.round(maxSeatsInRow * 0.8);
                } else if (i == 1) {
                    // Second row has 90% of max seats
                    seatsInThisRow = (int) Math.round(maxSeatsInRow * 0.9);
                } else {
                    // All other rows have full seats
                    seatsInThisRow = maxSeatsInRow;
                }

                // Ensure even number for center aisle
                if (hasCenterAisle && seatsInThisRow % 2 != 0) {
                    seatsInThisRow--;
                }
            }

            // Calculate center position for proper alignment
            int leftPadding = (maxSeatsInRow - seatsInThisRow) / 2;

            // Add each seat position to our list
            for (int j = 0; j < seatsInThisRow; j++) {
                // Calculate actual column number with centering
                int actualCol = leftPadding + j + 1;

                // Skip center seat if hall has center aisle
                if (hasCenterAisle && seatsInThisRow >= 8) {
                    int middleCol = maxSeatsInRow / 2;

                    // Skip middle seat
                    if (actualCol == middleCol) {
                        continue;
                    }

                    // Adjust column numbers after the aisle
                    if (actualCol > middleCol) {
                        actualCol++;
                    }
                }

                // Add this seat position [row, column]
                seatPositions.add(new int[] {i, actualCol});
            }
        }

        // Determine seat priorities
        // We want middle rows, center seats to have higher priority
        seatPositions.sort((a, b) -> {
            int rowA = a[0];
            int rowB = b[0];
            int colA = a[1];
            int colB = b[1];

            // Calculate distance from ideal center row
            int idealRow = totalRows / 2;
            int rowDistA = Math.abs(rowA - idealRow);
            int rowDistB = Math.abs(rowB - idealRow);

            // Calculate distance from center column
            int idealCol = maxSeatsInRow / 2;
            int colDistA = Math.abs(colA - idealCol);
            int colDistB = Math.abs(colB - idealCol);

            // Create a priority score - lower is better (closer to center)
            int scoreA = rowDistA * 100 + colDistA;
            int scoreB = rowDistB * 100 + colDistB;

            // Sort by score (best seats first)
            return Integer.compare(scoreA, scoreB);
        });

        // If we have more positions than needed capacity, trim the list
        if (seatPositions.size() > targetCapacity) {
            seatPositions = seatPositions.subList(0, targetCapacity);
        }

        // Now create the seats
        for (int[] pos : seatPositions) {
            int i = pos[0];
            int actualCol = pos[1];
            char rowChar = (char) ('A' + i);

            // Create seat ID (e.g., A1, B5)
            String seatId = rowChar + String.valueOf(actualCol);
            String seatType = "standard";

            // Assign seat types based on standardized patterns for each hall size

            // For small halls (25 seats)
            if (targetCapacity == 25) {
                // Premium seats in the middle row
                if (i == 2 && actualCol >= 2 && actualCol <= 4) {
                    seatType = "premium";
                }
                // Accessible seats in corners
                if ((i == 0 && (actualCol == 1 || actualCol == 5)) ||
                        (i == 4 && (actualCol == 1 || actualCol == 5))) {
                    seatType = "accessible";
                }
            }
            // For medium halls (50 seats)
            else if (targetCapacity == 50) {
                // Premium seats in middle rows and center
                if ((i >= 1 && i <= 3) && (actualCol >= 4 && actualCol <= 7)) {
                    seatType = "premium";
                }
                // Accessible seats in corners
                if ((i == 0 && (actualCol == 1 || actualCol == 10)) ||
                        (i == 4 && (actualCol == 1 || actualCol == 10))) {
                    seatType = "accessible";
                }
            }
            // For larger halls (75, 100, 150, 200 seats)
            else {
                // Calculate middle rows and center seats
                int middleRowStart = totalRows / 3;
                int middleRowEnd = (2 * totalRows / 3);

                // Premium seats in the middle section
                if (i >= middleRowStart && i <= middleRowEnd) {
                    // Center 40-60% of seats are premium
                    if (actualCol >= maxSeatsInRow * 0.2 && actualCol <= maxSeatsInRow * 0.8) {
                        seatType = "premium";
                    }
                }

                // Accessible seats in strategic locations
                if ((i == 0 && (actualCol == 1 || actualCol == maxSeatsInRow)) ||
                        (i == totalRows - 1 && (actualCol == 1 || actualCol == maxSeatsInRow))) {
                    seatType = "accessible";
                }
            }

            // Create the seat
            HallGraph.Seat seat = new HallGraph.Seat(seatId, hall.getId(), i, actualCol, seatType);
            hallGraph.addSeat(seat);
            seatsCreated++;

            // Add edges to adjacent seats
            for (int[] otherPos : seatPositions) {
                // Skip the current seat
                if (otherPos[0] == i && otherPos[1] == actualCol) {
                    continue;
                }

                // Check if this seat is adjacent (horizontally, vertically or diagonally)
                boolean isAdjacent = false;

                // Same row, adjacent column
                if (otherPos[0] == i && Math.abs(otherPos[1] - actualCol) == 1) {
                    isAdjacent = true;
                }
                // Adjacent row, same or adjacent column
                else if (Math.abs(otherPos[0] - i) == 1 && Math.abs(otherPos[1] - actualCol) <= 1) {
                    isAdjacent = true;
                }

                if (isAdjacent) {
                    char otherRowChar = (char) ('A' + otherPos[0]);
                    String otherSeatId = otherRowChar + String.valueOf(otherPos[1]);

                    if (hallGraph.seatExists(otherSeatId)) {
                        hallGraph.addEdge(seatId, otherSeatId);
                    }
                }
            }
        }

        System.out.println("Created " + seatsCreated + " seats for hall: " + hall.getName());

        // Verify we created exactly the right number of seats
        if (seatsCreated != targetCapacity) {
            System.out.println("WARNING: Created " + seatsCreated + " seats, but target capacity was " +
                    targetCapacity + ". This may indicate a layout issue.");
        }

        // Update the hall with the seating layout information
        StringBuilder layoutBuilder = new StringBuilder();
        layoutBuilder.append("Rows:").append(totalRows).append(",Seats:").append(maxSeatsInRow);
        if (hasCenterAisle) {
            layoutBuilder.append(",CenterAisle:true");
        }
        hall.setSeatingLayout(layoutBuilder.toString());
        hallDAO.update(hall);
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
        // Ensure capacity is one of the standard options
        if (!STANDARD_LAYOUTS.containsKey(capacity)) {
            System.out.println("Warning: Non-standard capacity " + capacity +
                    ". Using closest standard capacity.");

            // Find closest standard capacity
            int closestCapacity = findClosestStandardCapacity(capacity);
            capacity = closestCapacity;
        }

        Hall hall = new Hall(name, capacity, location, type);
        Hall savedHall = hallDAO.save(hall);

        if (savedHall != null) {
            System.out.println("Hall saved successfully with ID: " + savedHall.getId());
            createSeatsForHall(savedHall);
        } else {
            System.out.println("Failed to save hall: " + name);
        }

        return savedHall;
    }

    /**
     * Find the closest standard capacity
     * @param capacity The target capacity
     * @return The closest standard capacity
     */
    private int findClosestStandardCapacity(int capacity) {
        int closestCapacity = 25; // Default smallest
        int minDiff = Math.abs(capacity - closestCapacity);

        for (Integer standardCapacity : STANDARD_LAYOUTS.keySet()) {
            int diff = Math.abs(capacity - standardCapacity);
            if (diff < minDiff) {
                minDiff = diff;
                closestCapacity = standardCapacity;
            }
        }

        return closestCapacity;
    }

    /**
     * Find a hall by ID
     * @param hallId The hall ID
     * @return The hall or null if not found
     */
    public Hall findHallById(int hallId) {
        Hall hall = hallDAO.findById(hallId);
        if (hall == null) {
            System.out.println("Hall not found with ID: " + hallId);
        }
        return hall;
    }

    /**
     * Update hall information
     * @param hall The hall to update
     * @return The updated hall
     */
    public Hall updateHall(Hall hall) {
        // Ensure capacity is one of the standard options
        if (!STANDARD_LAYOUTS.containsKey(hall.getCapacity())) {
            int standardCapacity = findClosestStandardCapacity(hall.getCapacity());
            System.out.println("Warning: Changing non-standard capacity " + hall.getCapacity() +
                    " to standard capacity " + standardCapacity);
            hall.setCapacity(standardCapacity);
        }

        Hall updatedHall = hallDAO.update(hall);

        // Check if the capacity changed
        Hall oldHall = findHallById(hall.getId());
        if (oldHall != null && oldHall.getCapacity() != hall.getCapacity()) {
            // Force refresh of seats for this hall
            List<HallGraph.Seat> existingSeats = hallGraph.getSeatsInHall(hall.getId());
            if (existingSeats != null && !existingSeats.isEmpty()) {
                System.out.println("Capacity changed, removing existing seats and recreating layout");
                // For now, we'll just mark them all as reserved to simulate removal
                for (HallGraph.Seat seat : existingSeats) {
                    hallGraph.reserveSeat(seat.getId());
                }
            }

            // Create new seating layout
            createSeatsForHall(updatedHall);
        }

        return updatedHall;
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
        List<Hall> halls = hallDAO.findAll();
        if (halls == null || halls.isEmpty()) {
            System.out.println("No halls found in database");
            return new ArrayList<>();
        }
        System.out.println("Found " + halls.size() + " halls in database");
        return halls;
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
        List<HallGraph.Seat> seats = hallGraph.getSeatsInHall(hallId);

        // If no seats are found, try to create them
        if (seats == null || seats.isEmpty()) {
            System.out.println("No seats found for hall ID: " + hallId + ", attempting to create seats");
            Hall hall = findHallById(hallId);
            if (hall != null) {
                createSeatsForHall(hall);
                seats = hallGraph.getSeatsInHall(hallId);
            }
        }

        System.out.println("Found " + (seats != null ? seats.size() : 0) + " seats for hall ID: " + hallId);
        return seats != null ? seats : new ArrayList<>();
    }

    /**
     * Get available seats in a hall
     * @param hallId The hall ID
     * @return List of available seats
     */
    public List<HallGraph.Seat> getAvailableSeats(int hallId) {
        List<HallGraph.Seat> seats = hallGraph.getAvailableSeats(hallId);
        if (seats == null || seats.isEmpty()) {
            // If no available seats are found, check if any seats exist at all
            List<HallGraph.Seat> allSeats = getSeatsInHall(hallId);
            if (allSeats.isEmpty()) {
                System.out.println("No seats found for hall ID: " + hallId);
            } else {
                System.out.println("All seats are reserved for hall ID: " + hallId);
            }
            return new ArrayList<>();
        }
        return seats;
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
        // Make sure seats exist first
        List<HallGraph.Seat> seats = getSeatsInHall(hallId);
        if (seats.isEmpty()) {
            // Return an empty seating chart
            return new String[0][0];
        }
        return hallGraph.generateSeatingChart(hallId);
    }

    /**
     * Reserve a seat
     * @param seatId The seat ID
     * @return True if successful
     */
    public boolean reserveSeat(String seatId) {
        boolean success = hallGraph.reserveSeat(seatId);
        if (!success) {
            System.out.println("Failed to reserve seat: " + seatId);
        }
        return success;
    }

    /**
     * Cancel a seat reservation
     * @param seatId The seat ID
     * @return True if successful
     */
    public boolean cancelSeatReservation(String seatId) {
        boolean success = hallGraph.cancelReservation(seatId);
        if (!success) {
            System.out.println("Failed to cancel reservation for seat: " + seatId);
        }
        return success;
    }

    /**
     * Get a seat by ID
     * @param seatId The seat ID
     * @return The seat or null if not found
     */
    public HallGraph.Seat getSeat(String seatId) {
        HallGraph.Seat seat = hallGraph.getSeat(seatId);
        if (seat == null) {
            System.out.println("Seat not found: " + seatId);
        }
        return seat;
    }

    /**
     * Force refresh of all seats for all halls
     * This can be called if seats are not appearing correctly
     */
    public void refreshAllHallSeats() {
        System.out.println("Refreshing all hall seats");
        List<Hall> halls = getAllHalls();
        for (Hall hall : halls) {
            System.out.println("Refreshing seats for hall: " + hall.getName());

            // Remove existing seats (would need implementation in a real system)
            // For each existing seat in this hall, remove it from the graph
            List<HallGraph.Seat> existingSeats = hallGraph.getSeatsInHall(hall.getId());
            if (existingSeats != null && !existingSeats.isEmpty()) {
                // In a real implementation, you would remove these from the graph
                System.out.println("Would remove " + existingSeats.size() + " existing seats");
                // For now we'll just mark them all as reserved to simulate removal
                for (HallGraph.Seat seat : existingSeats) {
                    hallGraph.reserveSeat(seat.getId());
                }
            }

            // Create new seats
            createSeatsForHall(hall);
        }
    }

    /**
     * Count total number of seats in the system
     * @return Total seat count
     */
    public int getTotalSeatCount() {
        int count = 0;
        List<Hall> halls = getAllHalls();
        for (Hall hall : halls) {
            List<HallGraph.Seat> seats = getSeatsInHall(hall.getId());
            count += seats.size();
        }
        return count;
    }
}