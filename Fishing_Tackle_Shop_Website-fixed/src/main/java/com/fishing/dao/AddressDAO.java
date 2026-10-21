package com.fishing.dao;

import com.fishing.model.Address;
import com.fishing.model.Customer;
import com.fishing.util.BusinessException;
import com.fishing.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

/** Sổ địa chỉ của khách. Mọi hàm đều kiểm tra địa chỉ thuộc đúng khách để khách không sửa được địa chỉ người khác. */
public class AddressDAO {

    public static final int MAX_PER_CUSTOMER = 10;

    /** Địa chỉ mặc định lên đầu, sau đó mới nhất trước. */
    public List<Address> findByCustomer(Long customerId) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT a FROM Address a WHERE a.customer.id = :cid "
                                    + "ORDER BY a.isDefault DESC, a.id DESC", Address.class)
                    .setParameter("cid", customerId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Address findById(Long id, Long customerId) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT a FROM Address a WHERE a.id = :id AND a.customer.id = :cid", Address.class)
                    .setParameter("id", id)
                    .setParameter("cid", customerId)
                    .getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    /**
     * Thêm mới (a.getId() == null) hoặc cập nhật. Địa chỉ đầu tiên của khách tự thành mặc định;
     * đặt một địa chỉ làm mặc định thì các địa chỉ khác bị bỏ mặc định.
     */
    public void save(Long customerId, Address a, boolean makeDefault) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            long count = em.createQuery("SELECT COUNT(x) FROM Address x WHERE x.customer.id = :cid", Long.class)
                    .setParameter("cid", customerId).getSingleResult();

            Address target;
            if (a.getId() == null) {
                if (count >= MAX_PER_CUSTOMER) {
                    throw new BusinessException("Mỗi tài khoản chỉ lưu tối đa " + MAX_PER_CUSTOMER + " địa chỉ.");
                }
                target = new Address();
                target.setCustomer(em.getReference(Customer.class, customerId));
            } else {
                target = em.createQuery(
                                "SELECT x FROM Address x WHERE x.id = :id AND x.customer.id = :cid", Address.class)
                        .setParameter("id", a.getId())
                        .setParameter("cid", customerId)
                        .getResultStream().findFirst().orElse(null);
                if (target == null) {
                    throw new BusinessException("Không tìm thấy địa chỉ.");
                }
            }
            target.setReceiverName(a.getReceiverName());
            target.setReceiverPhone(a.getReceiverPhone());
            target.setStreet(a.getStreet());
            target.setWard(a.getWard());
            target.setDistrict(a.getDistrict());
            target.setCity(a.getCity());

            boolean becomeDefault = makeDefault || (a.getId() == null && count == 0);
            if (becomeDefault) {
                clearDefault(em, customerId);
                target.setDefault(true);
            }
            if (a.getId() == null) {
                em.persist(target);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void setDefault(Long id, Long customerId) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            Address a = em.createQuery(
                            "SELECT x FROM Address x WHERE x.id = :id AND x.customer.id = :cid", Address.class)
                    .setParameter("id", id)
                    .setParameter("cid", customerId)
                    .getResultStream().findFirst().orElse(null);
            if (a == null) {
                throw new BusinessException("Không tìm thấy địa chỉ.");
            }
            clearDefault(em, customerId);
            a.setDefault(true);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Xóa địa chỉ. Nếu xóa địa chỉ mặc định thì địa chỉ mới nhất còn lại thành mặc định. */
    public void delete(Long id, Long customerId) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            Address a = em.createQuery(
                            "SELECT x FROM Address x WHERE x.id = :id AND x.customer.id = :cid", Address.class)
                    .setParameter("id", id)
                    .setParameter("cid", customerId)
                    .getResultStream().findFirst().orElse(null);
            if (a == null) {
                throw new BusinessException("Không tìm thấy địa chỉ.");
            }
            boolean wasDefault = a.isDefault();
            em.remove(a);
            em.flush();
            if (wasDefault) {
                Address next = em.createQuery(
                                "SELECT x FROM Address x WHERE x.customer.id = :cid ORDER BY x.id DESC", Address.class)
                        .setParameter("cid", customerId)
                        .setMaxResults(1)
                        .getResultStream().findFirst().orElse(null);
                if (next != null) {
                    next.setDefault(true);
                }
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private void clearDefault(EntityManager em, Long customerId) {
        em.createQuery("UPDATE Address a SET a.isDefault = false WHERE a.customer.id = :cid AND a.isDefault = true")
                .setParameter("cid", customerId)
                .executeUpdate();
    }
}
