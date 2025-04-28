package com.ucinema.dao;

import com.ucinema.model.entities.Student;
import com.ucinema.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object for Student entity.
 */
public class StudentDAO {

    /**
     * Save a new student to the database
     * @param student The student to save
     * @return The saved student with ID
     */
    public Student save(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(student);
            transaction.commit();
            return student;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update an existing student
     * @param student The student to update
     * @return The updated student
     */
    public Student update(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(student);
            transaction.commit();
            return student;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete a student from the database
     * @param student The student to delete
     * @return True if successful
     */
    public boolean delete(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(student);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Find a student by ID
     * @param id The student ID
     * @return The student or null if not found
     */
    public Student findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Student.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find a student by email
     * @param email The email to search for
     * @return The student or null if not found
     */
    public Student findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery("FROM Student WHERE email = :email", Student.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find a student by student number
     * @param studentNumber The student number to search for
     * @return The student or null if not found
     */
    public Student findByStudentNumber(String studentNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery("FROM Student WHERE studentNumber = :studentNumber", Student.class);
            query.setParameter("studentNumber", studentNumber);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all students
     * @return List of all students
     */
    public List<Student> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Student", Student.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Authenticate a student by email and password
     * @param email The email
     * @param password The password
     * @return The student if authentication is successful, null otherwise
     */
    public Student authenticate(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                    "FROM Student WHERE email = :email AND password = :password", Student.class);
            query.setParameter("email", email);
            query.setParameter("password", password);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Search students by name (partial match)
     * @param name The name to search for
     * @return List of matching students
     */
    public List<Student> searchByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                    "FROM Student WHERE name LIKE :name", Student.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}