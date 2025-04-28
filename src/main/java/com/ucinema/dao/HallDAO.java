package com.ucinema.dao;

import com.ucinema.model.entities.Hall;
import com.ucinema.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object for Hall entity.
 */
public class HallDAO {

    /**
     * Save a new hall to the database
     * @param hall The hall to save
     * @return The saved hall with ID
     */
    public Hall save(Hall hall) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(hall);
            transaction.commit();
            return hall;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update an existing hall
     * @param hall The hall to update
     * @return The updated hall
     */
    public Hall update(Hall hall) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(hall);
            transaction.commit();
            return hall;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete a hall from the database
     * @param hall The hall to delete
     * @return True if successful
     */
    public boolean delete(Hall hall) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(hall);
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
     * Find a hall by ID
     * @param id The hall ID
     * @return The hall or null if not found
     */
    public Hall findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Hall.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all halls
     * @return List of all halls
     */
    public List<Hall> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Hall ORDER BY name", Hall.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find halls by location
     * @param location The location to search for
     * @return List of halls at the specified location
     */
    public List<Hall> findByLocation(String location) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Hall> query = session.createQuery(
                    "FROM Hall WHERE location LIKE :location ORDER BY name", Hall.class);
            query.setParameter("location", "%" + location + "%");
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find halls by type
     * @param type The hall type to search for
     * @return List of halls of the specified type
     */
    public List<Hall> findByType(String type) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Hall> query = session.createQuery(
                    "FROM Hall WHERE type = :type ORDER BY name", Hall.class);
            query.setParameter("type", type);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find halls with minimum capacity
     * @param minCapacity The minimum capacity required
     * @return List of halls with at least the specified capacity
     */
    public List<Hall> findByMinCapacity(int minCapacity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Hall> query = session.createQuery(
                    "FROM Hall WHERE capacity >= :minCapacity ORDER BY capacity DESC", Hall.class);
            query.setParameter("minCapacity", minCapacity);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find a hall by name
     * @param name The hall name
     * @return The hall or null if not found
     */
    public Hall findByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Hall> query = session.createQuery("FROM Hall WHERE name = :name", Hall.class);
            query.setParameter("name", name);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}