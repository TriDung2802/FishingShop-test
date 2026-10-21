package com.fishing.listener;

import com.fishing.dao.AdminDAO;
import com.fishing.model.Admin;
import com.fishing.util.JPAUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Khởi động: khởi tạo Hibernate (tự tạo bảng) và tạo tài khoản admin đầu tiên nếu CHƯA có admin nào.
 * Tên và mật khẩu lấy từ biến môi trường ADMIN_USERNAME, ADMIN_PASSWORD (tùy chọn ADMIN_NAME);
 * không đặt biến thì không tạo gì. Mật khẩu không bao giờ nằm trong code.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            seedAdmin(sce);
        } catch (Exception e) {
            sce.getServletContext().log("Không khởi tạo được database / tài khoản admin.", e);
        }
    }

    private void seedAdmin(ServletContextEvent sce) {
        String username = env("ADMIN_USERNAME");
        String password = env("ADMIN_PASSWORD");
        AdminDAO adminDAO = new AdminDAO();   // chạm DB lần đầu -> Hibernate tạo/cập nhật bảng

        if (username == null || password == null) {
            return;
        }
        if (password.length() < 8) {
            sce.getServletContext().log("ADMIN_PASSWORD phải có ít nhất 8 ký tự, bỏ qua việc tạo admin.");
            return;
        }
        if (adminDAO.count() > 0) {
            return;
        }
        String name = env("ADMIN_NAME");
        adminDAO.save(new Admin(username, BCrypt.hashpw(password, BCrypt.gensalt()),
                name != null ? name : "Quản trị viên"));
        sce.getServletContext().log("Đã tạo tài khoản admin \"" + username + "\".");
    }

    private String env(String name) {
        String v = System.getenv(name);
        return v == null || v.isBlank() ? null : v.trim();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.close();
    }
}
