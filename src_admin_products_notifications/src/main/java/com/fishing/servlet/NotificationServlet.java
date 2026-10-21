package com.fishing.servlet;

import com.fishing.dao.NotificationDAO;
import com.fishing.model.Notification;
import com.fishing.util.AuthUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Thông báo thực của khách hàng, được đọc từ bảng notifications trong database.
 * GET /notifications?format=json : dữ liệu cho chuông thông báo.
 * GET /notifications              : trang danh sách.
 * POST action=read|readAll         : đánh dấu đã đọc, chỉ áp dụng cho thông báo của người đang đăng nhập.
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/customer/notification/list.jsp";
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long customerId = AuthUtil.getCustomerId(req);
        if (customerId == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if ("json".equalsIgnoreCase(req.getParameter("format"))) {
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.setHeader("Cache-Control", "no-store");
            List<Notification> items = notificationDAO.findByRecipient(customerId, 8);
            long unread = notificationDAO.countUnread(customerId);
            StringBuilder json = new StringBuilder("{\"unread\":").append(unread).append(",\"items\":[");
            for (int i = 0; i < items.size(); i++) {
                Notification n = items.get(i);
                if (i > 0) json.append(',');
                json.append('{')
                    .append("\"id\":").append(n.getId()).append(',')
                    .append("\"icon\":\"").append(escapeJson(n.getType() == null ? "🔔" : n.getType().getIcon())).append("\",")
                    .append("\"title\":\"").append(escapeJson(n.getTitle())).append("\",")
                    .append("\"message\":\"").append(escapeJson(n.getContent())).append("\",")
                    .append("\"time\":\"").append(escapeJson(n.getTimeText())).append("\",")
                    .append("\"unread\":").append(!n.isRead())
                    .append('}');
            }
            json.append("]}");
            resp.getWriter().write(json.toString());
            return;
        }

        req.setAttribute("notifications", notificationDAO.findByRecipient(customerId, 100));
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long customerId = AuthUtil.getCustomerId(req);
        if (customerId == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String action = req.getParameter("action");
        if ("readAll".equals(action)) {
            notificationDAO.markAllRead(customerId);
        } else if ("read".equals(action)) {
            String rawId = req.getParameter("id");
            try {
                long id = Long.parseLong(rawId == null ? "" : rawId);
                if (id > 0) notificationDAO.markRead(id, customerId);
            } catch (NumberFormatException ignored) {
                // Dữ liệu id không hợp lệ: không thay đổi dữ liệu.
            }
        }
        String referer = req.getHeader("Referer");
        String listPath = req.getContextPath() + "/notifications";
        if (referer != null && referer.startsWith(req.getScheme() + "://" + req.getServerName())) {
            resp.sendRedirect(listPath);
        } else {
            resp.sendRedirect(listPath);
        }
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int)c));
                    else out.append(c);
            }
        }
        return out.toString();
    }
}
