package com.ucinema.dao;

import com.ucinema.model.entities.Reservation;
import com.ucinema.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object for Reservation entity.
 */
public class ReservationDAO {

    /**
     * Save a new reservation to the database
     * @param reservation The reservation to save
     * @return The saved reservation with ID
     */
    public Reservation save(Reservation reservation) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(reservation);
            transaction.commit();
            return reservation;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update an existing reservation
     * @param reservation The reservation to update
     * @return The updated reservation
     */
    public Reservation update(Reservation reservation) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(reservation);
            transaction.commit();
            return reservation;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete a reservation from the database
     * @param reservation The reservation to delete
     * @return True if successful
     */
    public boolean delete(Reservation reservation) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(reservation);
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
     * Find a reservation by ID
     * @param id The reservation ID
     * @return The reservation or null if not found
     */
    public Reservation findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Reservation.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all reservations
     * @return List of all reservations
     */
    public List<Reservation> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Reservation ORDER BY reservationTime DESC", Reservation.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find reservations for a specific student
     * @param studentId The student ID
     * @return List of reservations for the student
     */
    public List<Reservation> findByStudent(int studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Reservation> query = session.createQuery(
                    "FROM Reservation WHERE studentId = :studentId ORDER BY reservationTime DESC", Reservation.class);
            query.setParameter("studentId", studentId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find reservations for a specific movie schedule
     * @param scheduleId The schedule ID
     * @return List of reservations for the schedule
     */
    public List<Reservation> findBySchedule(int scheduleId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Reservation> query = session.createQuery(
                    "FROM Reservation WHERE scheduleId = :scheduleId ORDER BY seatId", Reservation.class);
            query.setParameter("scheduleId", scheduleId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if a seat is reserved for a specific schedule
     * @param scheduleId The schedule ID
     * @param seatId The seat ID
     * @return True if the seat is reserved
     */
    public boolean isSeatReserved(int scheduleId, String seatId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM Reservation WHERE scheduleId = :scheduleId AND seatId = :seatId AND status != 'cancelled'",
                    Long.class);
            query.setParameter("scheduleId", scheduleId);
            query.setParameter("seatId", seatId);
            Long count = query.uniqueResult();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Assume reserved on error
        }
    }

    /**
     * Get all reserved seats for a schedule
     * @param scheduleId The schedule ID
     * @return List of reserved seat IDs
     */
    public List<String> getReservedSeats(int scheduleId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery(
                    "SELECT seatId FROM Reservation WHERE scheduleId = :scheduleId AND status != 'cancelled'",
                    String.class);
            query.setParameter("scheduleId", scheduleId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find reservations by status
     * @param status The reservation status
     * @return List of reservations with the specified status
     */
    public List<Reservation> findByStatus(String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Reservation> query = session.createQuery(
                    "FROM Reservation WHERE status = :status ORDER BY reservationTime DESC", Reservation.class);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find reservations made within a time range
     * @param start The start time
     * @param end The end time
     * @return List of reservations made within the time range
     */
    public List<Reservation> findByReservationTimeRange(LocalDateTime start, LocalDateTime end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Reservation> query = session.createQuery(
                    "FROM Reservation WHERE reservationTime >= :start AND reservationTime <= :end ORDER BY reservationTime DESC",
                    Reservation.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cancel a reservation by changing its status
     * @param reservationId The reservation ID
     * @return True if successful
     */
    public boolean cancelReservation(int reservationId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Reservation reservation = session.get(Reservation.class, reservationId);
            if (reservation != null) {
                reservation.setStatus("cancelled");
                reservation.setUpdatedAt(LocalDateTime.now());
                session.merge(reservation);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Find reservations by student ID - alternative method name for consistency
     * @param studentId The student ID
     * @return List of reservations for the student
     */
    public List<Reservation> findByStudentId(int studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Reservation> query = session.createQuery(
                    "FROM Reservation WHERE studentId = :studentId AND status != 'cancelled' ORDER BY reservationTime DESC",
                    Reservation.class);
            query.setParameter("studentId", studentId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}