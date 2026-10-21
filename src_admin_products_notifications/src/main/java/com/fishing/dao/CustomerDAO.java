package com.fishing.dao;

import com.fishing.model.Customer;
import com.fishing.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

public class CustomerDAO {

    /** Tìm theo email (không phân biệt hoa thường). */
    public Customer findByEmail(String email) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Customer c WHERE LOWER(c.email) = LOWER(:email)", Customer.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Customer findById(Long id) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.find(Customer.class, id);
        } finally {
            em.close();
        }
    }

    public void save(Customer customer) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(customer);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean updatePassword(String email, String hashedPassword) {
        EntityManager em = JPAUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int updated = em.createQuery(
                            "UPDATE Customer c SET c.password = :password WHERE LOWER(c.email) = LOWER(:email)")
                    .setParameter("password", hashedPassword)
                    .setParameter("email", email)
                    .executeUpdate();
            tx.commit();
            return updated > 0;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}