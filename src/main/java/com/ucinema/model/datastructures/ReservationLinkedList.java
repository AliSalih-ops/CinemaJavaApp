package com.ucinema.model.datastructures;

import com.ucinema.model.entities.Reservation;
import java.util.NoSuchElementException;

/**
 * A custom LinkedList implementation to manage reservations.
 */
public class ReservationLinkedList {
    private Node head;
    private int size;

    private class Node {
        Reservation data;
        Node next;

        Node(Reservation data) {
            this.data = data;
            this.next = null;
        }
    }

    public ReservationLinkedList() {
        head = null;
        size = 0;
    }

    /**
     * Add a new reservation to the list
     * @param reservation The reservation to add
     */
    public void add(Reservation reservation) {
        Node newNode = new Node(reservation);
        if (head == null) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    /**
     * Remove a reservation from the list
     * @param reservationId The ID of the reservation to remove
     * @return The removed reservation or null if not found
     */
    public Reservation remove(int reservationId) {
        if (head == null) {
            return null;
        }

        if (head.data.getId() == reservationId) {
            Reservation removed = head.data;
            head = head.next;
            size--;
            return removed;
        }

        Node current = head;
        while (current.next != null && current.next.data.getId() != reservationId) {
            current = current.next;
        }

        if (current.next != null) {
            Reservation removed = current.next.data;
            current.next = current.next.next;
            size--;
            return removed;
        }

        return null;
    }

    /**
     * Find a reservation by its ID
     * @param reservationId The ID to search for
     * @return The reservation or null if not found
     */
    public Reservation find(int reservationId) {
        Node current = head;
        while (current != null) {
            if (current.data.getId() == reservationId) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }

    /**
     * Find all reservations for a specific student
     * @param studentId The student ID to search for
     * @return Array of reservations for the student
     */
    public Reservation[] findByStudent(int studentId) {
        // First count matching reservations
        int count = 0;
        Node current = head;
        while (current != null) {
            if (current.data.getStudentId() == studentId) {
                count++;
            }
            current = current.next;
        }

        // Create array of exact size
        Reservation[] result = new Reservation[count];
        current = head;
        int index = 0;
        while (current != null) {
            if (current.data.getStudentId() == studentId) {
                result[index++] = current.data;
            }
            current = current.next;
        }

        return result;
    }

    /**
     * Get all reservations as an array
     * @return Array of all reservations
     */
    public Reservation[] toArray() {
        Reservation[] array = new Reservation[size];
        Node current = head;
        int index = 0;
        while (current != null) {
            array[index++] = current.data;
            current = current.next;
        }
        return array;
    }

    /**
     * Get the size of the list
     * @return The number of reservations in the list
     */
    public int size() {
        return size;
    }

    /**
     * Check if the list is empty
     * @return True if the list is empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }
}