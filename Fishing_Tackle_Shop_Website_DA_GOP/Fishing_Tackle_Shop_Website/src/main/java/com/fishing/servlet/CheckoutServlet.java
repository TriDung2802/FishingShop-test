package com.fishing.servlet;

import com.fishing.dao.AddressDAO;
import com.fishing.dao.CartDAO;
import com.fishing.dao.OrderDAO;
import com.fishing.dao.PromotionDAO;
import com.fishing.enums.PaymentMethod;
import com.fishing.filter.CartCountFilter;
import com.fishing.model.Address;
import com.fishing.model.Cart;
import com.fishing.model.Order;
import com.fishing.model.Promotion;
import com.fishing.util.BusinessException;
import com.fishing.util.ShopConfig;
import com.fishing.util.AuthUtil;
import com.fishing.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Đặt hàng.
 * GET : /checkout -> form thông tin nhận hàng + tóm tắt giỏ
 * POST: /checkout -> tạo đơn từ giỏ hàng, chuyển tới trang chi tiết đơn
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/customer/order/checkout.jsp";
    // Số di động VN: 10 số, bắt đầu bằng 0
    private static final Pattern PHONE = Pattern.compile("^0\\d{9}$");
    // Lưu thông tin nhận hàng lần trước để lần sau điền sẵn
    private static final String LAST_INFO = "lastShippingInfo";

    private final CartDAO cartDAO = new CartDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final AddressDAO addressDAO = new AddressDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Object last = req.getSession().getAttribute(LAST_INFO);
        OrderDAO.ShippingInfo info = last instanceof OrderDAO.ShippingInfo
                ? (OrderDAO.ShippingInfo) last : new OrderDAO.ShippingInfo();

        // Chưa có thông tin lần trước: điền sẵn địa chỉ mặc định trong Sổ địa chỉ
        if (info.receiverName == null || info.receiverName.isBlank()) {
            List<Address> addresses = addressDAO.findByCustomer(AuthUtil.getCustomerId(req));
            if (!addresses.isEmpty()) {
                Address a = addresses.get(0);   // AddressDAO sắp địa chỉ mặc định lên đầu
                info.receiverName = a.getReceiverName();
                info.receiverPhone = a.getReceiverPhone();
                info.shippingAddress = a.getFullAddress();
            }
        }
        showForm(req, resp, info);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        OrderDAO.ShippingInfo info = new OrderDAO.ShippingInfo();
        info.receiverName = WebUtil.trim(req.getParameter("receiverName"));
        info.receiverPhone = WebUtil.trim(req.getParameter("receiverPhone")).replaceAll("[\\s.\\-]", "");
        info.shippingAddress = WebUtil.trim(req.getParameter("shippingAddress"));
        info.note = WebUtil.emptyToNull(WebUtil.trim(req.getParameter("note")));
        info.paymentMethod = parsePaymentMethod(req.getParameter("paymentMethod"));
        String promo = WebUtil.trim(req.getParameter("promoCode")).toUpperCase();
        info.promoCode = promo.isEmpty() ? null : promo;

        // Bấm "Áp dụng" mã giảm giá: chỉ tính lại tiền, chưa đặt hàng
        if ("applyPromo".equals(req.getParameter("action"))) {
            showForm(req, resp, info);
            return;
        }

        String error = validate(info);
        if (error == null && info.promoCode != null && promotionDAO.findActiveByCode(info.promoCode) == null) {
            error = "Mã giảm giá \"" + info.promoCode + "\" không hợp lệ hoặc đã hết hạn.";
        }
        if (error != null) {
            req.setAttribute("error", error);
            showForm(req, resp, info);
            return;
        }

        try {
            Order order = orderDAO.placeOrder(AuthUtil.getCustomerId(req), info);
            info.promoCode = null;      // lần sau không tự điền lại mã đã dùng
            req.getSession().setAttribute(LAST_INFO, info);
            CartCountFilter.refresh(req, cartDAO);
            WebUtil.flash(req, "success", "Đặt hàng thành công! Mã đơn của bạn là " + order.getCode() + ".");
            resp.sendRedirect(req.getContextPath() + "/orders?id=" + order.getId());
        } catch (BusinessException e) {
            // Hết hàng / giỏ trống...: quay về giỏ để khách sửa
            CartCountFilter.refresh(req, cartDAO);
            WebUtil.flash(req, "error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/cart");
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, OrderDAO.ShippingInfo info)
            throws ServletException, IOException {
        Cart cart = cartDAO.findByCustomer(AuthUtil.getCustomerId(req));
        if (cart.getItems().isEmpty()) {
            WebUtil.flash(req, "error", "Giỏ hàng đang trống.");
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        if (cart.hasProblem()) {
            WebUtil.flash(req, "error", "Có sản phẩm trong giỏ đã hết hàng hoặc vượt quá tồn kho. "
                    + "Vui lòng điều chỉnh trước khi đặt hàng.");
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        // Phí ship & phương thức thanh toán (phần của Lộc)
        BigDecimal subtotal = cart.calculateTotal();
        BigDecimal shippingFee = ShopConfig.calculateShippingFee(subtotal);
        req.setAttribute("shippingFee", shippingFee);

        // Mã giảm giá (Promotion của Dũng): tính thử để khách thấy trước số tiền được giảm.
        // Khi đặt hàng, OrderDAO kiểm tra lại mã trong transaction.
        BigDecimal discount = BigDecimal.ZERO;
        if (info.promoCode != null) {
            Promotion promotion = promotionDAO.findActiveByCode(info.promoCode);
            if (promotion == null) {
                req.setAttribute("promoError", "Mã \"" + info.promoCode + "\" không hợp lệ hoặc đã hết hạn.");
            } else {
                discount = promotion.computeDiscount(subtotal);
                req.setAttribute("promotion", promotion);
            }
        }
        req.setAttribute("discount", discount);
        req.setAttribute("grandTotal", subtotal.subtract(discount).add(shippingFee));
        req.setAttribute("freeShippingThreshold", ShopConfig.FREE_SHIPPING_THRESHOLD);
        req.setAttribute("paymentMethods", enabledPaymentMethods());

        // Sổ địa chỉ để khách chọn nhanh ở form
        req.setAttribute("addresses", addressDAO.findByCustomer(AuthUtil.getCustomerId(req)));

        req.setAttribute("cart", cart);
        req.setAttribute("info", info);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    private String validate(OrderDAO.ShippingInfo info) {
        if (info.receiverName.isEmpty()) {
            return "Vui lòng nhập họ tên người nhận.";
        }
        if (info.receiverName.length() > 100) {
            return "Họ tên tối đa 100 ký tự.";
        }
        if (!PHONE.matcher(info.receiverPhone).matches()) {
            return "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.";
        }
        if (info.shippingAddress.length() < 10) {
            return "Vui lòng nhập địa chỉ giao hàng đầy đủ (số nhà, đường, phường, quận/huyện, tỉnh/thành).";
        }
        if (info.shippingAddress.length() > 255) {
            return "Địa chỉ tối đa 255 ký tự.";
        }
        if (info.note != null && info.note.length() > 500) {
            return "Ghi chú tối đa 500 ký tự.";
        }
        if (info.paymentMethod == null) {
            return "Vui lòng chọn phương thức thanh toán.";
        }
        return null;
    }

    /** Chỉ nhận phương thức đang bật (COD, chuyển khoản); giá trị lạ -> null. */
    private PaymentMethod parsePaymentMethod(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            PaymentMethod m = PaymentMethod.valueOf(s.trim());
            return m.isEnabled() ? m : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<PaymentMethod> enabledPaymentMethods() {
        List<PaymentMethod> list = new ArrayList<>();
        for (PaymentMethod m : PaymentMethod.values()) {
            if (m.isEnabled()) {
                list.add(m);
            }
        }
        return list;
    }
}
