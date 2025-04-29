package com.ucinema.service;

import com.ucinema.dao.MovieScheduleDAO;
import com.ucinema.model.datastructures.ScheduleBST;
import com.ucinema.model.entities.Movie;
import com.ucinema.model.entities.MovieSchedule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for movie schedule-related operations.
 */
public class MovieScheduleService {
    private final MovieScheduleDAO scheduleDAO;
    private final MovieService movieService;
    private final ScheduleBST scheduleBST;

    public MovieScheduleService() {
        this.scheduleDAO = new MovieScheduleDAO();
        this.movieService = new MovieService();
        this.scheduleBST = new ScheduleBST();
        initializeScheduleBST();
    }

    /**
     * Initialize the schedule BST with data from the database
     */
    private void initializeScheduleBST() {
        List<MovieSchedule> schedules = scheduleDAO.findAll();
        if (schedules != null) {
            for (MovieSchedule schedule : schedules) {
                scheduleBST.insert(schedule);
            }
        }
    }

    /**
     * Add a new movie schedule
     * @param movieId Movie ID
     * @param hallId Hall ID
     * @param startTime Screening start time
     * @param price Ticket price
     * @return The added schedule or null if operation failed
     */
    public MovieSchedule addSchedule(int movieId, int hallId, LocalDateTime startTime, double price) {
        // Fetch the movie to get its duration
        Movie movie = movieService.findMovieById(movieId);
        if (movie == null) {
            throw new IllegalArgumentException("Movie not found");
        }

        // Calculate end time based on movie duration
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        // Check if the hall is available during this time
        if (!scheduleDAO.isHallAvailable(hallId, startTime, endTime, null)) {
            throw new IllegalArgumentException("Hall is not available during this time");
        }

        // Create and save the schedule
        MovieSchedule schedule = new MovieSchedule(movieId, hallId, startTime, endTime, price);
        MovieSchedule savedSchedule = scheduleDAO.save(schedule);

        // Add to BST
        if (savedSchedule != null) {
            scheduleBST.insert(savedSchedule);
        }

        return savedSchedule;
    }

    /**
     * Find a schedule by ID
     * @param scheduleId The schedule ID
     * @return The schedule or null if not found
     */
    public MovieSchedule findScheduleById(int scheduleId) {
        // Try to find in BST first for faster access
        MovieSchedule schedule = scheduleBST.find(scheduleId);
        if (schedule != null) {
            return schedule;
        }

        // If not in BST, fetch from database
        return scheduleDAO.findById(scheduleId);
    }

    /**
     * Update schedule information
     * @param schedule The schedule to update
     * @return The updated schedule
     */
    public MovieSchedule updateSchedule(MovieSchedule schedule) {
        // For updates that change time, check hall availability
        if (schedule.getId() > 0) {
            MovieSchedule existing = scheduleDAO.findById(schedule.getId());
            if (existing != null &&
                    (!existing.getStartTime().equals(schedule.getStartTime()) ||
                            !existing.getEndTime().equals(schedule.getEndTime()) ||
                            existing.getHallId() != schedule.getHallId())) {

                // Check if the hall is available during the new time
                if (!scheduleDAO.isHallAvailable(schedule.getHallId(),
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        schedule.getId())) {
                    throw new IllegalArgumentException("Hall is not available during this time");
                }
            }
        }

        MovieSchedule updatedSchedule = scheduleDAO.update(schedule);

        // Update BST
        if (updatedSchedule != null) {
            // Remove old entry and insert updated one
            scheduleBST.remove(schedule.getId());
            scheduleBST.insert(updatedSchedule);
        }

        return updatedSchedule;
    }

    /**
     * Delete a schedule
     * @param schedule The schedule to delete
     * @return True if successful
     */
    public boolean deleteSchedule(MovieSchedule schedule) {
        boolean result = scheduleDAO.delete(schedule);

        // Remove from BST
        if (result) {
            scheduleBST.remove(schedule.getId());
        }

        return result;
    }

    /**
     * Get all schedules
     * @return List of all schedules
     */
    public List<MovieSchedule> getAllSchedules() {
        return scheduleDAO.findAll();
    }

    /**
     * Get all schedules from BST (sorted by start time)
     * @return List of all schedules
     */
    public List<MovieSchedule> getAllSchedulesFromBST() {
        return scheduleBST.getAllInOrder();
    }

    /**
     * Find schedules for a specific movie
     * @param movieId The movie ID
     * @return List of schedules for the movie
     */
    public List<MovieSchedule> findSchedulesByMovie(int movieId) {
        return scheduleBST.findByMovie(movieId);
    }

    /**
     * Find schedules for a specific hall
     * @param hallId The hall ID
     * @return List of schedules for the hall
     */
    public List<MovieSchedule> findSchedulesByHall(int hallId) {
        return scheduleDAO.findByHall(hallId);
    }

    /**
     * Find schedules within a time range
     * @param start The start time
     * @param end The end time
     * @return List of schedules within the time range
     */
    public List<MovieSchedule> findSchedulesInTimeRange(LocalDateTime start, LocalDateTime end) {
        return scheduleBST.findInTimeRange(start, end);
    }

    /**
     * Find active schedules (starting from now)
     * @return List of active schedules
     */
    public List<MovieSchedule> findActiveSchedules() {
        return scheduleDAO.findActiveSchedules();
    }

}