package com.fishing.servlet;

import com.fishing.dao.AdminDAO;
import com.fishing.dao.CustomerDAO;
import com.fishing.model.Admin;
import com.fishing.model.Customer;
import com.fishing.util.AuthUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

/**
 * Đăng nhập chung cho Admin (username) và Khách (email).
 * GET /login[?next=/duong-dan-noi-bo] -> form; POST -> kiểm tra, đặt session, chuyển hướng.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Đã đăng nhập rồi thì không cần hiện form
        if (AuthUtil.isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/products");
            return;
        }
        if (AuthUtil.isCustomer(request)) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String next = AuthUtil.safeNext(request, request.getParameter("next"));

        if (username == null || password == null || username.isBlank() || password.isEmpty()) {
            fail(request, response, "Vui lòng nhập email/username và mật khẩu.");
            return;
        }
        username = username.trim();

        // 1. Admin (đăng nhập bằng username)
        Admin admin = adminDAO.findByUsername(username);
        if (admin != null && checkPassword(password, admin.getPassword())) {
            AuthUtil.loginAdmin(request, admin);
            response.sendRedirect(request.getContextPath() + "/admin/products");
            return;
        }

        // 2. Khách (đăng nhập bằng email)
        Customer customer = customerDAO.findByEmail(username);
        if (customer != null && checkPassword(password, customer.getPassword())) {
            AuthUtil.loginCustomer(request, customer);
            response.sendRedirect(next != null ? next : request.getContextPath() + "/");
            return;
        }

        // Cùng một thông báo cho mọi trường hợp sai để không lộ tài khoản nào tồn tại
        fail(request, response, "Email/username hoặc mật khẩu không đúng.");
    }

    private boolean checkPassword(String plain, String hash) {
        try {
            return hash != null && BCrypt.checkpw(plain, hash);
        } catch (IllegalArgumentException e) {
            return false;   // hash trong DB sai định dạng
        }
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}
