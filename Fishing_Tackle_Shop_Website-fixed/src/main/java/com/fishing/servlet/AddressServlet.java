package com.fishing.servlet;

import com.fishing.model.Address;
import com.fishing.util.AuthUtil;
import com.fishing.util.BusinessException;
import com.fishing.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Sổ địa chỉ giao hàng của khách.
 * GET : /account/addresses            -> danh sách + form thêm
 *       /account/addresses?edit=ID    -> danh sách + form sửa địa chỉ đó
 * POST: action=save | delete | default (xong thì redirect về danh sách)
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
            Address a = addressDAO.findById(editId, customerId);
            if (a == null) {
                WebUtil.flash(req, "error", "Không tìm thấy địa chỉ.");
                resp.sendRedirect(req.getContextPath() + "/account/addresses");
                return;
            }
            req.setAttribute("form", a);
        }
        show(req, resp, customerId);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long customerId = AuthUtil.getCustomerId(req);
        String action = req.getParameter("action");
        Long id = WebUtil.parseId(req.getParameter("id"));

        try {
            if ("delete".equals(action) && id != null) {
                addressDAO.delete(id, customerId);
                WebUtil.flash(req, "success", "Đã xóa địa chỉ.");
            } else if ("default".equals(action) && id != null) {
                addressDAO.setDefault(id, customerId);
                WebUtil.flash(req, "success", "Đã đặt làm địa chỉ mặc định.");
            } else if ("save".equals(action)) {
                if (!save(req, resp, customerId, id)) {
                    return;   // đã hiện lại form kèm lỗi
                }
            }
        } catch (BusinessException e) {
            WebUtil.flash(req, "error", e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/account/addresses");
    }

    /** @return true nếu lưu xong; false nếu dữ liệu sai và đã forward lại form. */
    private boolean save(HttpServletRequest req, HttpServletResponse resp, Long customerId, Long id)
            throws ServletException, IOException {
        Address a = new Address();
        a.setId(id);
        a.setReceiverName(WebUtil.trim(req.getParameter("receiverName")));
        a.setReceiverPhone(WebUtil.trim(req.getParameter("receiverPhone")).replaceAll("[\\s.\\-]", ""));
        a.setStreet(WebUtil.trim(req.getParameter("street")));
        a.setWard(WebUtil.emptyToNull(WebUtil.trim(req.getParameter("ward"))));
        a.setDistrict(WebUtil.emptyToNull(WebUtil.trim(req.getParameter("district"))));
        a.setCity(WebUtil.trim(req.getParameter("city")));
        boolean makeDefault = "on".equals(req.getParameter("isDefault"));
        a.setDefault(makeDefault);

        String error = validate(a);
        if (error != null) {
            req.setAttribute("error", error);
            req.setAttribute("form", a);
            show(req, resp, customerId);
            return false;
        }
        if (id != null && addressDAO.findById(id, customerId) == null) {
            WebUtil.flash(req, "error", "Không tìm thấy địa chỉ.");
            return true;
        }
        addressDAO.save(customerId, a, makeDefault);
        WebUtil.flash(req, "success", id == null ? "Đã thêm địa chỉ." : "Đã cập nhật địa chỉ.");
        return true;
    }

    private String validate(Address a) {
        if (a.getReceiverName().isEmpty() || a.getReceiverName().length() > 100) {
            return "Họ tên người nhận không được để trống và tối đa 100 ký tự.";
        }
        if (!PHONE.matcher(a.getReceiverPhone()).matches()) {
            return "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.";
        }
        if (a.getStreet().isEmpty() || a.getStreet().length() > 150) {
            return "Số nhà, tên đường không được để trống và tối đa 150 ký tự.";
        }
        if (a.getCity().isEmpty() || a.getCity().length() > 80) {
            return "Tỉnh/thành phố không được để trống và tối đa 80 ký tự.";
        }
        if ((a.getWard() != null && a.getWard().length() > 80)
                || (a.getDistrict() != null && a.getDistrict().length() > 80)) {
            return "Phường/xã và quận/huyện tối đa 80 ký tự.";
        }
        return null;
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, Long customerId)
            throws ServletException, IOException {
        req.setAttribute("addresses", addressDAO.findByCustomer(customerId));
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
