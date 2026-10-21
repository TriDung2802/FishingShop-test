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
import java.util.regex.Pattern;

/**
 * Quản lý mã giảm giá (Admin).
 * GET : /admin/promotions  |  ?action=new  |  ?action=edit&id=1
 * POST: action=save | delete | toggle
 */
@WebServlet("/admin/promotions")
public class PromotionServlet extends HttpServlet {

    private static final String LIST_VIEW = "/WEB-INF/views/admin/promotion/list.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/admin/promotion/form.jsp";
    private static final Pattern CODE = Pattern.compile("^[A-Z0-9_-]{3,30}$");

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
                redirectToList(req, resp);
                return;
            }
            req.setAttribute("promotion", p);
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else {
            req.setAttribute("promotions", promotionDAO.findAll());
            req.setAttribute("today", LocalDate.now());
            req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        Long id = WebUtil.parseId(req.getParameter("id"));
        if ("save".equals(action)) {
            save(req, resp, id);
            return;
        }
        if (id == null) {
            WebUtil.flash(req, "error", "Không tìm thấy mã giảm giá.");
        } else if ("delete".equals(action)) {
            promotionDAO.delete(id);
            WebUtil.flash(req, "success", "Đã xóa mã giảm giá. Các đơn đã đặt không bị ảnh hưởng.");
        } else if ("toggle".equals(action)) {
            promotionDAO.toggleEnabled(id);
            WebUtil.flash(req, "success", "Đã đổi trạng thái mã giảm giá.");
        }
        redirectToList(req, resp);
    }

    private void save(HttpServletRequest req, HttpServletResponse resp, Long id)
            throws ServletException, IOException {
        Promotion p = new Promotion();
        p.setId(id);
        p.setCode(WebUtil.trim(req.getParameter("code")).toUpperCase());
        p.setName(WebUtil.trim(req.getParameter("name")));
        p.setEnabled("on".equals(req.getParameter("enabled")));

        String error = null;
        try {
            p.setDiscountPercent(new BigDecimal(WebUtil.trim(req.getParameter("discountPercent"))));
        } catch (NumberFormatException e) {
            error = "% giảm không hợp lệ.";
        }
        String maxText = WebUtil.trim(req.getParameter("maxDiscount")).replace(",", "");
        if (error == null && !maxText.isEmpty()) {
            try {
                p.setMaxDiscount(new BigDecimal(maxText));
            } catch (NumberFormatException e) {
                error = "Giảm tối đa không hợp lệ.";
            }
        }
        if (error == null) {
            try {
                p.setStartDate(LocalDate.parse(WebUtil.trim(req.getParameter("startDate"))));
                p.setEndDate(LocalDate.parse(WebUtil.trim(req.getParameter("endDate"))));
            } catch (DateTimeParseException e) {
                error = "Ngày bắt đầu hoặc ngày kết thúc không hợp lệ.";
            }
        }
        if (error == null) {
            error = validate(p, id);
        }
        if (error != null) {
            req.setAttribute("error", error);
            req.setAttribute("promotion", p);
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
            return;
        }

        promotionDAO.save(p);
        WebUtil.flash(req, "success", id == null ? "Đã thêm mã giảm giá." : "Đã cập nhật mã giảm giá.");
        redirectToList(req, resp);
    }

    private String validate(Promotion p, Long id) {
        if (!CODE.matcher(p.getCode()).matches()) {
            return "Mã gồm 3-30 ký tự chữ HOA, số, gạch ngang hoặc gạch dưới (không dấu, không khoảng trắng).";
        }
        if (promotionDAO.existsByCode(p.getCode(), id)) {
            return "Mã giảm giá này đã tồn tại.";
        }
        if (p.getName().isEmpty() || p.getName().length() > 100) {
            return "Tên chương trình không được để trống và tối đa 100 ký tự.";
        }
        if (p.getDiscountPercent().signum() <= 0 || p.getDiscountPercent().compareTo(BigDecimal.valueOf(100)) > 0) {
            return "% giảm phải lớn hơn 0 và không quá 100.";
        }
        if (p.getMaxDiscount() != null && p.getMaxDiscount().signum() <= 0) {
            return "Giảm tối đa phải lớn hơn 0 (để trống nếu không giới hạn).";
        }
        if (p.getEndDate().isBefore(p.getStartDate())) {
            return "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.";
        }
        return null;
    }

    private void redirectToList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/promotions");
    }
}
