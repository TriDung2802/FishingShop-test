package com.fishing.dao;

import com.fishing.enums.ProductStatus;
import com.fishing.model.Product;
import com.fishing.util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dữ liệu riêng cho trang chủ. */
public class HomeDAO {

    private static final List<ProductStatus> LISTED =
            List.of(ProductStatus.ACTIVE, ProductStatus.OUT_OF_STOCK);

    /** Sản phẩm có điểm trung bình từ 4 sao trở lên, điểm cao nhất trước. */
    public List<Product> topRated(int limit) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            // Bước 1: lấy danh sách id đã sắp xếp theo điểm
            List<Long> ids = em.createQuery(
                    "SELECT f.product.id FROM Feedback f "
                  + "WHERE f.product.status IN :st AND f.product.category.visible = true "
                  + "GROUP BY f.product.id "
                  + "HAVING AVG(f.rating) >= 4 "
                  + "ORDER BY AVG(f.rating) DESC, COUNT(f) DESC, f.product.id DESC", Long.class)
                    .setParameter("st", LISTED)
                    .setMaxResults(limit)
                    .getResultList();
            if (ids.isEmpty()) {
                return new ArrayList<>();
            }

            // Bước 2: nạp sản phẩm (kèm danh mục, thương hiệu) rồi xếp lại theo thứ tự id
            List<Product> found = em.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.brand "
                  + "WHERE p.id IN :ids", Product.class)
                    .setParameter("ids", ids)
                    .getResultList();
            Map<Long, Product> byId = new HashMap<>();
            for (Product p : found) {
                byId.put(p.getId(), p);
            }
            List<Product> result = new ArrayList<>();
            for (Long id : ids) {
                Product p = byId.get(id);
                if (p != null) {
                    result.add(p);
                }
            }
            return result;
        } finally {
            em.close();
        }
    }
}
