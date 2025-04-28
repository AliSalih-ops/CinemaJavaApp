package com.ucinema.dao;

import com.ucinema.model.entities.Movie;
import com.ucinema.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for Movie entity.
 */
public class MovieDAO {

    /**
     * Save a new movie to the database
     * @param movie The movie to save
     * @return The saved movie with ID
     */
    public Movie save(Movie movie) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(movie);
            transaction.commit();
            return movie;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update an existing movie
     * @param movie The movie to update
     * @return The updated movie
     */
    public Movie update(Movie movie) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(movie);
            transaction.commit();
            return movie;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete a movie from the database
     * @param movie The movie to delete
     * @return True if successful
     */
    public boolean delete(Movie movie) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(movie);
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
     * Find a movie by ID
     * @param id The movie ID
     * @return The movie or null if not found
     */
    public Movie findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Movie.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all movies
     * @return List of all movies
     */
    public List<Movie> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Movie ORDER BY title", Movie.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Search movies by title (partial match)
     * @param title The title to search for
     * @return List of matching movies
     */
    public List<Movie> searchByTitle(String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Movie> query = session.createQuery(
                    "FROM Movie WHERE title LIKE :title ORDER BY title", Movie.class);
            query.setParameter("title", "%" + title + "%");
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find movies by genre
     * @param genre The genre to search for
     * @return List of movies in the specified genre
     */
    public List<Movie> findByGenre(String genre) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Movie> query = session.createQuery(
                    "FROM Movie WHERE genre LIKE :genre ORDER BY title", Movie.class);
            query.setParameter("genre", "%" + genre + "%");
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find recent movies (released within the last X days)
     * @param days Number of days back to search
     * @return List of recent movies
     */
    public List<Movie> findRecentMovies(int days) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDate cutoffDate = LocalDate.now().minusDays(days);
            Query<Movie> query = session.createQuery(
                    "FROM Movie WHERE releaseDate >= :cutoffDate ORDER BY releaseDate DESC", Movie.class);
            query.setParameter("cutoffDate", cutoffDate);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find upcoming movies (not yet released)
     * @return List of upcoming movies
     */
    public List<Movie> findUpcomingMovies() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDate today = LocalDate.now();
            Query<Movie> query = session.createQuery(
                    "FROM Movie WHERE releaseDate > :today ORDER BY releaseDate", Movie.class);
            query.setParameter("today", today);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}