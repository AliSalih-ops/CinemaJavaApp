package com.ucinema.model.datastructures;

import com.ucinema.model.entities.MovieSchedule;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A Binary Search Tree implementation for managing movie schedules.
 * Schedules are ordered by their start time for efficient searching.
 */
public class ScheduleBST {
    private Node root;

    private class Node {
        MovieSchedule data;
        Node left;
        Node right;

        Node(MovieSchedule data) {
            this.data = data;
            this.left = null;
            this.right = null;
        }
    }

    public ScheduleBST() {
        root = null;
    }

    /**
     * Insert a new movie schedule into the BST
     * @param schedule The movie schedule to insert
     */
    public void insert(MovieSchedule schedule) {
        root = insertRec(root, schedule);
    }

    private Node insertRec(Node root, MovieSchedule schedule) {
        if (root == null) {
            root = new Node(schedule);
            return root;
        }

        // Compare start times for ordering
        if (schedule.getStartTime().isBefore(root.data.getStartTime())) {
            root.left = insertRec(root.left, schedule);
        } else if (schedule.getStartTime().isAfter(root.data.getStartTime())) {
            root.right = insertRec(root.right, schedule);
        } else {
            // If schedules have the same start time, compare by hall ID
            if (schedule.getHallId() < root.data.getHallId()) {
                root.left = insertRec(root.left, schedule);
            } else {
                root.right = insertRec(root.right, schedule);
            }
        }

        return root;
    }

    /**
     * Find a movie schedule by its ID
     * @param scheduleId The ID to search for
     * @return The movie schedule or null if not found
     */
    public MovieSchedule find(int scheduleId) {
        return findRec(root, scheduleId);
    }

    private MovieSchedule findRec(Node root, int scheduleId) {
        if (root == null) {
            return null;
        }

        if (root.data.getId() == scheduleId) {
            return root.data;
        }

        // Since we can't determine which side based on ID alone,
        // we need to search both sides
        MovieSchedule leftResult = findRec(root.left, scheduleId);
        if (leftResult != null) {
            return leftResult;
        }

        return findRec(root.right, scheduleId);
    }

    /**
     * Find all movie schedules within a specified time range
     * @param start The start of the time range
     * @param end The end of the time range
     * @return List of movie schedules within the range
     */
    public List<MovieSchedule> findInTimeRange(LocalDateTime start, LocalDateTime end) {
        List<MovieSchedule> result = new ArrayList<>();
        findInTimeRangeRec(root, start, end, result);
        return result;
    }

    private void findInTimeRangeRec(Node root, LocalDateTime start, LocalDateTime end, List<MovieSchedule> result) {
        if (root == null) {
            return;
        }

        // If root's start time is greater than our start range, check the left subtree
        if (start.isBefore(root.data.getStartTime())) {
            findInTimeRangeRec(root.left, start, end, result);
        }

        // If this node is within our range, add it
        if (!root.data.getStartTime().isBefore(start) && !root.data.getStartTime().isAfter(end)) {
            result.add(root.data);
        }

        // If root's start time is less than our end range, check the right subtree
        if (root.data.getStartTime().isBefore(end)) {
            findInTimeRangeRec(root.right, start, end, result);
        }
    }

    /**
     * Find all schedules for a specific movie
     * @param movieId The movie ID to search for
     * @return List of schedules for the movie
     */
    public List<MovieSchedule> findByMovie(int movieId) {
        List<MovieSchedule> result = new ArrayList<>();
        findByMovieRec(root, movieId, result);
        return result;
    }

    private void findByMovieRec(Node root, int movieId, List<MovieSchedule> result) {
        if (root == null) {
            return;
        }

        findByMovieRec(root.left, movieId, result);

        if (root.data.getMovieId() == movieId) {
            result.add(root.data);
        }

        findByMovieRec(root.right, movieId, result);
    }

    /**
     * Get all schedules in order of start time
     * @return List of all schedules
     */
    public List<MovieSchedule> getAllInOrder() {
        List<MovieSchedule> result = new ArrayList<>();
        inOrderTraversal(root, result);
        return result;
    }

    private void inOrderTraversal(Node root, List<MovieSchedule> result) {
        if (root == null) {
            return;
        }

        inOrderTraversal(root.left, result);
        result.add(root.data);
        inOrderTraversal(root.right, result);
    }

    /**
     * Remove a schedule by its ID
     * @param scheduleId The ID of the schedule to remove
     * @return True if removed, false if not found
     */
    public boolean remove(int scheduleId) {
        MovieSchedule schedule = find(scheduleId);
        if (schedule == null) {
            return false;
        }

        root = removeRec(root, schedule);
        return true;
    }

    private Node removeRec(Node root, MovieSchedule schedule) {
        if (root == null) {
            return null;
        }

        // Navigate to the node
        int comparison = compareSchedules(schedule, root.data);

        if (comparison < 0) {
            root.left = removeRec(root.left, schedule);
        } else if (comparison > 0) {
            root.right = removeRec(root.right, schedule);
        } else {
            // Node with only one child or no child
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }

            // Node with two children: Get the inorder successor
            root.data = minValue(root.right);

            // Delete the inorder successor
            root.right = removeRec(root.right, root.data);
        }

        return root;
    }

    private MovieSchedule minValue(Node root) {
        MovieSchedule minValue = root.data;
        while (root.left != null) {
            minValue = root.left.data;
            root = root.left;
        }
        return minValue;
    }

    private int compareSchedules(MovieSchedule a, MovieSchedule b) {
        int timeComparison = a.getStartTime().compareTo(b.getStartTime());
        if (timeComparison != 0) {
            return timeComparison;
        }
        return Integer.compare(a.getHallId(), b.getHallId());
    }
}