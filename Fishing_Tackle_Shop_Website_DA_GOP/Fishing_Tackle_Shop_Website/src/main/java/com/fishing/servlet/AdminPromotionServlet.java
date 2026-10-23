package com.fishing.servlet;

import com.fishing.dao.PromotionDAO;
import com.fishing.model.Promotion;
import com.fishing.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Quản lý mã giảm giá (Admin, phần của Dũng).
 * GET : /admin/promotions                  -> danh sách
 *       /admin/promotions?action=new       -> form thêm
 *       /admin/promotions?action=edit&id=1 -> form sửa
 * POST: action=save | toggle | delete
 */
@WebServlet("/admin/promotions")
public class AdminPromotionServlet extends HttpServlet {

    private static final String LIST_VIEW = "/WEB-INF/views/admin/promotion/list.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/admin/promotion/form.jsp";

    private final PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("new".equals(action)) {
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else if ("edit".equals(action)) {
            Long id = WebUtil.parseId(req.getParameter("id"));
            Promotion p = id == null ? null : promotionDAO.findById(id);
            if (p == null) {
                WebUtil.flash(req, "error", "Không tìm thấy mã giảm giá.");
                resp.sendRedirect(req.getContextPath() + "/admin/promotions");
                return;
            }
            req.setAttribute("promotion", p);
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else {
            req.setAttribute("promotions", promotionDAO.findAll());
            req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        Long id = WebUtil.parseId(req.getParameter("id"));

        if ("toggle".equals(action) && id != null) {
            promotionDAO.toggleEnabled(id);
            WebUtil.flash(req, "success", "Đã đổi trạng thái mã.");
        } else if ("delete".equals(action) && id != null) {
            promotionDAO.delete(id);
            WebUtil.flash(req, "success", "Đã xóa mã giảm giá.");
        } else if ("save".equals(action)) {
            Promotion p = id == null ? new Promotion() : promotionDAO.findById(id);
            if (p == null) {
                WebUtil.flash(req, "error", "Không tìm thấy mã giảm giá.");
                resp.sendRedirect(req.getContextPath() + "/admin/promotions");
                return;
            }
            p.setCode(WebUtil.trim(req.getParameter("code")).toUpperCase());
            p.setName(WebUtil.trim(req.getParameter("name")));
            BigDecimal percent = parseDecimal(req.getParameter("discountPercent"));
            BigDecimal max = parseDecimal(req.getParameter("maxDiscount"));
            LocalDate start = parseDate(req.getParameter("startDate"));
            LocalDate end = parseDate(req.getParameter("endDate"));
            p.setDiscountPercent(percent);
            p.setMaxDiscount(max);
            p.setStartDate(start);
            p.setEndDate(end);
            p.setEnabled(req.getParameter("enabled") != null);

            String error = validate(p, id);
            if (error != null) {
                req.setAttribute("error", error);
                req.setAttribute("promotion", p);
                req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
                return;
            }
            promotionDAO.save(p);
            WebUtil.flash(req, "success", id == null ? "Đã thêm mã " + p.getCode() + "." : "Đã cập nhật mã.");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/promotions");
    }

    private String validate(Promotion p, Long id) {
        if (!p.getCode().matches("^[A-Z0-9_-]{3,30}$")) {
            return "Mã gồm 3–30 ký tự chữ, số, - hoặc _.";
        }
        if (promotionDAO.existsByCode(p.getCode(), id)) {
            return "Mã " + p.getCode() + " đã tồn tại.";
        }
        if (p.getName().isEmpty() || p.getName().length() > 100) {
            return "Tên chương trình không được trống (tối đa 100 ký tự).";
        }
        if (p.getDiscountPercent() == null || p.getDiscountPercent().signum() <= 0
                || p.getDiscountPercent().compareTo(BigDecimal.valueOf(100)) > 0) {
            return "Phần trăm giảm phải lớn hơn 0 và không quá 100.";
        }
        if (p.getMaxDiscount() != null && p.getMaxDiscount().signum() <= 0) {
            return "Giảm tối đa phải lớn hơn 0 (bỏ trống nếu không giới hạn).";
        }
        if (p.getStartDate() == null || p.getEndDate() == null) {
            return "Ngày bắt đầu / kết thúc không hợp lệ.";
        }
        if (p.getEndDate().isBefore(p.getStartDate())) {
            return "Ngày kết thúc phải sau ngày bắt đầu.";
        }
        return null;
    }

    private BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
