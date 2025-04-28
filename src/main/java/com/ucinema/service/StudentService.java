package com.ucinema.service;

import com.ucinema.dao.StudentDAO;
import com.ucinema.model.datastructures.StudentHashTable;
import com.ucinema.model.entities.Student;

import java.util.List;

/**
 * Service class for student-related operations.
 */
public class StudentService {
    private final StudentDAO studentDAO;
    private final StudentHashTable studentCache;

    public StudentService() {
        this.studentDAO = new StudentDAO();
        this.studentCache = new StudentHashTable();
        initializeCache();
    }

    /**
     * Initialize the cache with students from the database
     */
    private void initializeCache() {
        List<Student> students = studentDAO.findAll();
        if (students != null) {
            for (Student student : students) {
                studentCache.insert(student);
            }
        }
    }

    /**
     * Register a new student
     * @param name Student name
     * @param email Student email
     * @param password Student password
     * @param studentNumber Student ID number
     * @return The registered student or null if registration failed
     */
    public Student registerStudent(String name, String email, String password, String studentNumber) {
        // Check if email or student number already exists
        if (studentDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (studentDAO.findByStudentNumber(studentNumber) != null) {
            throw new IllegalArgumentException("Student number already registered");
        }

        // Create and save the student
        Student student = new Student(name, email, password, studentNumber);
        Student savedStudent = studentDAO.save(student);

        // Add to cache
        if (savedStudent != null) {
            studentCache.insert(savedStudent);
        }

        return savedStudent;
    }

    /**
     * Authenticate a student
     * @param email Student email
     * @param password Student password
     * @return The authenticated student or null if authentication failed
     */
    public Student login(String email, String password) {
        return studentDAO.authenticate(email, password);
    }

    /**
     * Find a student by ID
     * @param studentId The student ID
     * @return The student or null if not found
     */
    public Student findStudentById(int studentId) {
        // Try to find in cache first for faster access
        Student student = studentCache.find(studentId);
        if (student != null) {
            return student;
        }

        // If not in cache, fetch from database
        return studentDAO.findById(studentId);
    }

    /**
     * Find a student by email
     * @param email The email to search for
     * @return The student or null if not found
     */
    public Student findStudentByEmail(String email) {
        return studentDAO.findByEmail(email);
    }

    /**
     * Find a student by student number
     * @param studentNumber The student number to search for
     * @return The student or null if not found
     */
    public Student findStudentByNumber(String studentNumber) {
        return studentDAO.findByStudentNumber(studentNumber);
    }

    /**
     * Search students by name
     * @param name The name to search for
     * @return List of matching students
     */
    public List<Student> searchStudentsByName(String name) {
        return studentDAO.searchByName(name);
    }

    /**
     * Update student information
     * @param student The student to update
     * @return The updated student
     */
    public Student updateStudent(Student student) {
        Student updatedStudent = studentDAO.update(student);

        // Update cache
        if (updatedStudent != null) {
            studentCache.remove(student.getId());
            studentCache.insert(updatedStudent);
        }

        return updatedStudent;
    }

    /**
     * Get all students
     * @return List of all students
     */
    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }

    /**
     * Get all students from cache
     * @return List of all cached students
     */
    public List<Student> getAllStudentsFromCache() {
        return studentCache.getAllStudents();
    }
}