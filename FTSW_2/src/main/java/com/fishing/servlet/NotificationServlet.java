package com.fishing.servlet;

import com.fishing.dao.NotificationDAO;
import com.fishing.model.Notification;
import com.fishing.util.AuthUtil;
import com.fishing.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Thông báo cá nhân của khách.
 * GET  /notifications               -> trang danh sách
 * GET  /notifications?format=json   -> JSON cho chuông trên header (5 thông báo mới nhất + số chưa đọc)
 * POST /notifications  action=read&id=.. | readAll
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/customer/notification/list.jsp";
    private static final int PAGE_LIMIT = 100;
    private static final int BELL_LIMIT = 5;

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long customerId = AuthUtil.getCustomerId(req);

        if ("json".equals(req.getParameter("format"))) {
            writeJson(resp, customerId);
            return;
        }
        req.setAttribute("notifications", notificationDAO.findByRecipient(customerId, PAGE_LIMIT));
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long customerId = AuthUtil.getCustomerId(req);
        String action = req.getParameter("action");
        if ("readAll".equals(action)) {
            notificationDAO.markAllRead(customerId);
        } else if ("read".equals(action)) {
            Long id = WebUtil.parseId(req.getParameter("id"));
            if (id != null) {
                notificationDAO.markRead(id, customerId);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/notifications");
    }

    private void writeJson(HttpServletResponse resp, Long customerId) throws IOException {
        List<Notification> list = notificationDAO.findByRecipient(customerId, BELL_LIMIT);
        long unread = notificationDAO.countUnread(customerId);

        StringBuilder sb = new StringBuilder("{\"unread\":").append(unread).append(",\"items\":[");
        for (int i = 0; i < list.size(); i++) {
            Notification n = list.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(n.getId())
                    .append(",\"icon\":\"").append(json(n.getType().getIcon()))
                    .append("\",\"title\":\"").append(json(n.getTitle()))
                    .append("\",\"message\":\"").append(json(n.getContent()))
                    .append("\",\"time\":\"").append(json(n.getTimeText()))
                    .append("\",\"unread\":").append(!n.isRead()).append('}');
        }
        sb.append("]}");

        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(sb.toString());
    }

    /** Thoát ký tự đặc biệt để chuỗi an toàn trong JSON. */
    private String json(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
