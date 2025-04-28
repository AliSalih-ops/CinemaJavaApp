package com.ucinema.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import com.ucinema.model.entities.*;
import java.io.File;
import java.util.Properties;

/**
 * Utility class for Hibernate configuration and setup.
 */
public class HibernateUtil {
    private static SessionFactory sessionFactory;

    /**
     * Get the Hibernate SessionFactory (singleton pattern)
     * @return The SessionFactory instance
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                // Add this debug statement
                File dbFile = new File("university_cinema.db");
                System.out.println("Looking for database at: " + dbFile.getAbsolutePath());

                Configuration configuration = new Configuration();

                // Hibernate settings
                Properties settings = new Properties();
                settings.put(Environment.DRIVER, "org.sqlite.JDBC");

                // You can also update the connection URL if needed
                // String dbPath = System.getProperty("user.dir") + "/university_cinema.db";
                // settings.put(Environment.URL, "jdbc:sqlite:" + dbPath);

                settings.put(Environment.URL, "jdbc:sqlite:university_cinema.db");
                settings.put(Environment.DIALECT, "org.hibernate.community.dialect.SQLiteDialect");
                settings.put(Environment.SHOW_SQL, "true");
                settings.put(Environment.FORMAT_SQL, "true");
                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                settings.put(Environment.HBM2DDL_AUTO, "update");

                configuration.setProperties(settings);

                // Register entity classes
                configuration.addAnnotatedClass(Student.class);
                configuration.addAnnotatedClass(Movie.class);
                configuration.addAnnotatedClass(Hall.class);
                configuration.addAnnotatedClass(MovieSchedule.class);
                configuration.addAnnotatedClass(Reservation.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }

    /**
     * Close the session factory
     */
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}