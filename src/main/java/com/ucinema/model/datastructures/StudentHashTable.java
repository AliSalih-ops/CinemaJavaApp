package com.ucinema.model.datastructures;

import com.ucinema.model.entities.Student;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A hash table implementation for efficient student lookup.
 * Uses separate chaining to handle collisions.
 */
public class StudentHashTable {
    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;

    private LinkedList<Student>[] table;
    private int size;
    private int capacity;

    /**
     * Create a new hash table with default capacity
     */
    @SuppressWarnings("unchecked")
    public StudentHashTable() {
        this.capacity = DEFAULT_CAPACITY;
        this.table = new LinkedList[capacity];
        this.size = 0;

        // Initialize the chains
        for (int i = 0; i < capacity; i++) {
            table[i] = new LinkedList<>();
        }
    }

    /**
     * Create a new hash table with specified capacity
     *
     * @param capacity The initial capacity
     */
    @SuppressWarnings("unchecked")
    public StudentHashTable(int capacity) {
        this.capacity = capacity;
        this.table = new LinkedList[capacity];
        this.size = 0;

        // Initialize the chains
        for (int i = 0; i < capacity; i++) {
            table[i] = new LinkedList<>();
        }
    }

    /**
     * Hash function to determine the index for a student
     *
     * @param studentId The student ID
     * @return The hash value
     */
    private int hash(int studentId) {
        return studentId % capacity;
    }

    /**
     * Insert a student into the hash table
     *
     * @param student The student to insert
     * @return True if successful, false if duplicate
     */
    public boolean insert(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Cannot insert null student");
        }

        // Check if we need to resize
        if ((double) size / capacity >= LOAD_FACTOR_THRESHOLD) {
            resize();
        }

        int index = hash(student.getId());

        // Check for duplicate
        for (Student existingStudent : table[index]) {
            if (existingStudent.getId() == student.getId()) {
                return false; // Duplicate found
            }
        }

        // Insert the student
        table[index].add(student);
        size++;
        return true;
    }

    /**
     * Find a student by ID
     * @param studentId The student ID to find
     * @return The student or null if not found
     */
    public Student find(int studentId) {
        int index = hash(studentId);

        for (Student student : table[index]) {
            if (student.getId() == studentId) {
                return student;
            }
        }

        return null; // Not found
    }

    /**
     * Remove a student from the hash table
     * @param studentId The ID of the student to remove
     * @return The removed student or null if not found
     */
    public Student remove(int studentId) {
        int index = hash(studentId);

        for (int i = 0; i < table[index].size(); i++) {
            Student student = table[index].get(i);
            if (student.getId() == studentId) {
                table[index].remove(i);
                size--;
                return student;
            }
        }

        return null; // Not found
    }

    /**
     * Get all students in the hash table
     * @return List of all students
     */
    public List<Student> getAllStudents() {
        List<Student> allStudents = new ArrayList<>();

        for (LinkedList<Student> chain : table) {
            allStudents.addAll(chain);
        }

        return allStudents;
    }

    /**
     * Resize the hash table when it gets too full
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = capacity * 2;
        LinkedList<Student>[] newTable = new LinkedList[newCapacity];

        // Initialize new chains
        for (int i = 0; i < newCapacity; i++) {
            newTable[i] = new LinkedList<>();
        }

        // Rehash all existing students
        for (LinkedList<Student> chain : table) {
            for (Student student : chain) {
                int newIndex = student.getId() % newCapacity;
                newTable[newIndex].add(student);
            }
        }

        // Update table and capacity
        table = newTable;
        capacity = newCapacity;
    }

    /**
     * Get the number of students in the hash table
     * @return The size
     */
    public int size() {
        return size;
    }

    /**
     * Check if the hash table is empty
     * @return True if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }
}