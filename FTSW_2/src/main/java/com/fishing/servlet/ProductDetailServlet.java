package com.fishing.servlet;

import com.fishing.dao.FeedbackDAO;
import com.fishing.dao.ProductDAO;
import com.fishing.dao.ProductImageDAO;
import com.fishing.model.Product;
import com.fishing.util.AuthUtil;
import com.fishing.util.ProductRules;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Trang chi tiết sản phẩm cho khách hàng: GET /product?id=1 */
@WebServlet("/product")
public class ProductDetailServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/customer/product/detail.jsp";

    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO imageDAO = new ProductImageDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = parseId(req.getParameter("id"));
        Product p = id == null ? null : productDAO.findById(id);
        if (!ProductRules.isVisibleToCustomer(p)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        req.setAttribute("product", p);
        req.setAttribute("images", imageDAO.findByProduct(id));
        req.setAttribute("summary", feedbackDAO.getSummary(id));
        req.setAttribute("feedbacks", feedbackDAO.findByProduct(id));
        req.setAttribute("reviewState", reviewState(req, id));
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    /** Ai được gửi đánh giá: LOGIN (chưa đăng nhập), CAN (đã mua), NEED_PURCHASE (chưa mua), NONE (admin). */
    private String reviewState(HttpServletRequest req, Long productId) {
        Long customerId = AuthUtil.getCustomerId(req);
        if (customerId != null) {
            return feedbackDAO.hasPurchased(customerId, productId) ? "CAN" : "NEED_PURCHASE";
        }
        return AuthUtil.isAdmin(req) ? "NONE" : "LOGIN";
    }

    private Long parseId(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
