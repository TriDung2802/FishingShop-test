<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- KHU VỰC TÀI KHOẢN (phần của Dũng): chuông thông báo + đăng nhập/đăng ký/đăng xuất.
     Phần đăng nhập đang TẮT cho tới khi Dũng làm xong: đổi <context-param> authEnabled trong web.xml thành true.
     Quy ước với phần đăng nhập: sau khi đăng nhập thành công, đặt vào session
        currentUserName (String: tên hiển thị), customerId (Long), role ("CUSTOMER" hoặc "ADMIN"). --%>
<div class="user-area">

    <%-- Chuông thông báo (js/notification.js đọc các id bên dưới, không đổi id) --%>
    <div class="notification-wrapper">
        <button type="button" class="notification-bell" id="notificationBell"
                aria-label="Thông báo" aria-expanded="false" aria-controls="notificationDropdown">
            <span aria-hidden="true"> 🔔 </span>
            <span class="notification-badge" id="notificationBadge" hidden>0</span>
        </button>

        <div class="notification-dropdown" id="notificationDropdown">
            <div class="notification-dropdown-header">
                <h3>Thông báo</h3>
            </div>
            <div class="notification-list" id="latestNotifications"></div>
            <a class="notification-view-all" href="${ctx}/notification.jsp">Xem tất cả thông báo →</a>
        </div>
    </div>

    <c:if test="${initParam.authEnabled == 'true'}">
        <c:choose>
            <c:when test="${not empty sessionScope.currentUserName}">
                <span class="user-name">Xin chào, <c:out value="${sessionScope.currentUserName}"/></span>
                <form method="post" action="${ctx}/logout" class="inline">
                    <button type="submit" class="btn btn-ghost btn-sm">Đăng xuất</button>
                </form>
            </c:when>
            <c:otherwise>
                <a class="btn btn-ghost btn-sm" href="${ctx}/login">Đăng nhập</a>
                <a class="btn btn-accent btn-sm" href="${ctx}/register">Đăng ký</a>
            </c:otherwise>
        </c:choose>
    </c:if>
</div>
