package com.ucinema.dao;

import com.ucinema.model.entities.MovieSchedule;
import com.ucinema.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object for MovieSchedule entity.
 */
public class MovieScheduleDAO {

    /**
     * Save a new movie schedule to the database
     * @param schedule The movie schedule to save
     * @return The saved schedule with ID
     */
    public MovieSchedule save(MovieSchedule schedule) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(schedule);
            transaction.commit();
            return schedule;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update an existing movie schedule
     * @param schedule The movie schedule to update
     * @return The updated schedule
     */
    public MovieSchedule update(MovieSchedule schedule) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(schedule);
            transaction.commit();
            return schedule;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete a movie schedule from the database
     * @param schedule The movie schedule to delete
     * @return True if successful
     */
    public boolean delete(MovieSchedule schedule) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(schedule);
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
     * Find a movie schedule by ID
     * @param id The schedule ID
     * @return The movie schedule or null if not found
     */
    public MovieSchedule findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(MovieSchedule.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all movie schedules
     * @return List of all movie schedules
     */
    public List<MovieSchedule> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM MovieSchedule ORDER BY startTime", MovieSchedule.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find movie schedules for a specific movie
     * @param movieId The movie ID
     * @return List of schedules for the movie
     */
    public List<MovieSchedule> findByMovie(int movieId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSchedule> query = session.createQuery(
                    "FROM MovieSchedule WHERE movieId = :movieId ORDER BY startTime", MovieSchedule.class);
            query.setParameter("movieId", movieId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find movie schedules for a specific hall
     * @param hallId The hall ID
     * @return List of schedules for the hall
     */
    public List<MovieSchedule> findByHall(int hallId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSchedule> query = session.createQuery(
                    "FROM MovieSchedule WHERE hallId = :hallId ORDER BY startTime", MovieSchedule.class);
            query.setParameter("hallId", hallId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find movie schedules within a time range
     * @param start The start time
     * @param end The end time
     * @return List of schedules within the time range
     */
    public List<MovieSchedule> findInTimeRange(LocalDateTime start, LocalDateTime end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSchedule> query = session.createQuery(
                    "FROM MovieSchedule WHERE startTime >= :start AND startTime <= :end ORDER BY startTime",
                    MovieSchedule.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find active movie schedules (starting from now)
     * @return List of active schedules
     */
    public List<MovieSchedule> findActiveSchedules() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime now = LocalDateTime.now();
            Query<MovieSchedule> query = session.createQuery(
                    "FROM MovieSchedule WHERE startTime >= :now AND isActive = true ORDER BY startTime",
                    MovieSchedule.class);
            query.setParameter("now", now);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if a hall is available during a time range
     * @param hallId The hall ID
     * @param start The start time
     * @param end The end time
     * @param excludeScheduleId Schedule ID to exclude (for updates)
     * @return True if the hall is available
     */
    public boolean isHallAvailable(int hallId, LocalDateTime start, LocalDateTime end, Integer excludeScheduleId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String queryStr = "SELECT COUNT(*) FROM MovieSchedule "
                    + "WHERE hallId = :hallId AND isActive = true "
                    + "AND ((startTime <= :end AND endTime >= :start))";

            if (excludeScheduleId != null) {
                queryStr += " AND id != :excludeId";
            }

            Query<Long> query = session.createQuery(queryStr, Long.class);
            query.setParameter("hallId", hallId);
            query.setParameter("start", start);
            query.setParameter("end", end);

            if (excludeScheduleId != null) {
                query.setParameter("excludeId", excludeScheduleId);
            }

            Long count = query.uniqueResult();
            return count == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}