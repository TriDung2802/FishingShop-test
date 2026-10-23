package com.fishing.servlet;

import com.fishing.dao.AddressDAO;
import com.fishing.model.Address;
import com.fishing.util.AuthUtil;
import com.fishing.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Sổ địa chỉ của khách (phần của Dũng).
 * GET : /account/addresses            -> danh sách + form thêm
 *       /account/addresses?edit=1     -> form sửa
 * POST: action=save | default | delete
 * AuthFilter đã chặn /account/*: chỉ khách đã đăng nhập mới vào được.
 */
@WebServlet("/account/addresses")
public class AddressServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/customer/account/addresses.jsp";
    private static final Pattern PHONE = Pattern.compile("^0\\d{9}$");

    private final AddressDAO addressDAO = new AddressDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long customerId = AuthUtil.getCustomerId(req);
        Long editId = WebUtil.parseId(req.getParameter("edit"));
        if (editId != null) {
            req.setAttribute("form", addressDAO.findById(editId, customerId));
        }
        show(req, resp, customerId);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long customerId = AuthUtil.getCustomerId(req);
        String action = req.getParameter("action");
        Long id = WebUtil.parseId(req.getParameter("id"));

        if ("default".equals(action) && id != null) {
            addressDAO.setDefault(id, customerId);
            WebUtil.flash(req, "success", "Đã đặt làm địa chỉ mặc định.");
        } else if ("delete".equals(action) && id != null) {
            addressDAO.delete(id, customerId);
            WebUtil.flash(req, "success", "Đã xóa địa chỉ.");
        } else if ("save".equals(action)) {
            Address a = new Address();
            a.setId(id);
            a.setReceiverName(WebUtil.trim(req.getParameter("receiverName")));
            a.setReceiverPhone(WebUtil.trim(req.getParameter("receiverPhone")).replaceAll("[\\s.\\-]", ""));
            a.setStreet(WebUtil.trim(req.getParameter("street")));
            a.setWard(WebUtil.emptyToNull(WebUtil.trim(req.getParameter("ward"))));
            a.setDistrict(WebUtil.emptyToNull(WebUtil.trim(req.getParameter("district"))));
            a.setCity(WebUtil.trim(req.getParameter("city")));
            a.setDefault(req.getParameter("isDefault") != null);

            String error = validate(a);
            if (error != null) {
                req.setAttribute("error", error);
                req.setAttribute("form", a);
                show(req, resp, customerId);
                return;
            }
            addressDAO.save(a, customerId);
            WebUtil.flash(req, "success", id == null ? "Đã thêm địa chỉ." : "Đã cập nhật địa chỉ.");
        }
        resp.sendRedirect(req.getContextPath() + "/account/addresses");
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, Long customerId)
            throws ServletException, IOException {
        req.setAttribute("addresses", addressDAO.findByCustomer(customerId));
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private String validate(Address a) {
        if (a.getReceiverName().isEmpty() || a.getReceiverName().length() > 100) {
            return "Họ tên người nhận không được trống (tối đa 100 ký tự).";
        }
        if (!PHONE.matcher(a.getReceiverPhone()).matches()) {
            return "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.";
        }
        if (a.getStreet().isEmpty() || a.getStreet().length() > 150) {
            return "Vui lòng nhập số nhà, đường (tối đa 150 ký tự).";
        }
        if ((a.getWard() != null && a.getWard().length() > 80)
                || (a.getDistrict() != null && a.getDistrict().length() > 80)) {
            return "Phường/Quận tối đa 80 ký tự.";
        }
        if (a.getCity().isEmpty() || a.getCity().length() > 80) {
            return "Vui lòng nhập Tỉnh/Thành phố (tối đa 80 ký tự).";
        }
        return null;
    }
}
