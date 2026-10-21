package com.fishing.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Tạo EntityManagerFactory. Thông tin kết nối KHÔNG nằm trong persistence.xml mà lấy từ biến môi trường:
 *
 *  - Chạy trên Render + Neon: đặt DB_HOST, DB_NAME, DB_USER, DB_PASSWORD (tùy chọn DB_SSL_MODE, mặc định require).
 *  - Chạy máy cá nhân: đặt DB_PASSWORD (tùy chọn DB_URL, DB_USER;
 *    mặc định jdbc:postgresql://localhost:5432/fishing_shop và user postgres).
 */
public class JPAUtil {

    private static final EntityManagerFactory emf = createFactory();

    private JPAUtil() {}

    private static EntityManagerFactory createFactory() {
        Map<String, Object> props = new HashMap<>();
        String host = env("DB_HOST");

        if (host != null) {
            // Render + Neon
            String database = env("DB_NAME");
            String username = env("DB_USER");
            String password = env("DB_PASSWORD");
            if (database == null || username == null || password == null) {
                throw new IllegalStateException("Thiếu biến môi trường DB_NAME, DB_USER hoặc DB_PASSWORD.");
            }
            String sslMode = env("DB_SSL_MODE");
            if (sslMode == null) {
                sslMode = "require";
            }
            props.put("jakarta.persistence.jdbc.url",
                    "jdbc:postgresql://" + host + "/" + database + "?sslmode=" + sslMode);
            props.put("jakarta.persistence.jdbc.user", username);
            props.put("jakarta.persistence.jdbc.password", password);
        } else {
            // Máy cá nhân
            String password = env("DB_PASSWORD");
            if (password == null) {
                throw new IllegalStateException(
                        "Chưa đặt biến môi trường DB_PASSWORD (mật khẩu PostgreSQL). Xem README.");
            }
            String url = env("DB_URL");
            String user = env("DB_USER");
            props.put("jakarta.persistence.jdbc.url", url != null ? url : "jdbc:postgresql://localhost:5432/fishing_shop");
            props.put("jakarta.persistence.jdbc.user", user != null ? user : "postgres");
            props.put("jakarta.persistence.jdbc.password", password);
        }
        return Persistence.createEntityManagerFactory("fishingPU", props);
    }

    private static String env(String name) {
        String v = System.getenv(name);
        return v == null || v.isBlank() ? null : v.trim();
    }

    public static EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
