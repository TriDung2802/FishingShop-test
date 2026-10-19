<%-- MẪU CHO DŨNG: trang đăng nhập. Chép vào WEB-INF/views/customer/auth/login.jsp,
     LoginServlet (/login) forward tới đây khi GET, hoặc khi POST sai (đặt request attribute "error"). --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/views/common/shop-header.jsp">
    <jsp:param name="title" value="Đăng nhập"/>
</jsp:include>

<div class="auth-wrap">
    <div class="auth-card">
        <h1>Đăng nhập</h1>
        <p class="auth-sub">Chào mừng bạn quay lại Fishing Shop</p>

        <c:if test="${not empty error}">
            <div class="alert alert-error"><c:out value="${error}"/></div>
        </c:if>

        <form class="form" method="post" action="${ctx}/login">
            <label for="email">Email</label>
            <input type="email" id="email" name="email" required value="<c:out value='${param.email}'/>">

            <label for="password">Mật khẩu</label>
            <input type="password" id="password" name="password" required>

            <button type="submit" class="btn btn-primary btn-block">Đăng nhập</button>
        </form>
        <p class="auth-foot">Chưa có tài khoản? <a href="${ctx}/register">Đăng ký</a></p>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
