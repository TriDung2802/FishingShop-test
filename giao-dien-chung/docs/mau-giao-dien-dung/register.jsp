<%-- MẪU CHO DŨNG: trang đăng ký. Chép vào WEB-INF/views/customer/auth/register.jsp.
     Lỗi từng ô: đặt request attribute "errors" (Map<String,String>) rồi hiện bằng <div class="field-error">. --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/views/common/shop-header.jsp">
    <jsp:param name="title" value="Đăng ký"/>
</jsp:include>

<div class="auth-wrap">
    <div class="auth-card">
        <h1>Tạo tài khoản</h1>
        <p class="auth-sub">Đăng ký để mua hàng và theo dõi đơn</p>

        <c:if test="${not empty error}">
            <div class="alert alert-error"><c:out value="${error}"/></div>
        </c:if>

        <form class="form" method="post" action="${ctx}/register">
            <label for="fullName">Họ tên <span class="req">*</span></label>
            <input type="text" id="fullName" name="fullName" required maxlength="100" value="<c:out value='${param.fullName}'/>">
            <c:if test="${not empty errors.fullName}"><div class="field-error"><c:out value="${errors.fullName}"/></div></c:if>

            <label for="email">Email <span class="req">*</span></label>
            <input type="email" id="email" name="email" required value="<c:out value='${param.email}'/>">
            <c:if test="${not empty errors.email}"><div class="field-error"><c:out value="${errors.email}"/></div></c:if>

            <label for="phone">Số điện thoại</label>
            <input type="tel" id="phone" name="phone" value="<c:out value='${param.phone}'/>">

            <label for="password">Mật khẩu <span class="req">*</span></label>
            <input type="password" id="password" name="password" required minlength="6">
            <div class="hint">Tối thiểu 6 ký tự.</div>

            <label for="confirm">Nhập lại mật khẩu <span class="req">*</span></label>
            <input type="password" id="confirm" name="confirm" required>
            <c:if test="${not empty errors.confirm}"><div class="field-error"><c:out value="${errors.confirm}"/></div></c:if>

            <button type="submit" class="btn btn-primary btn-block">Đăng ký</button>
        </form>
        <p class="auth-foot">Đã có tài khoản? <a href="${ctx}/login">Đăng nhập</a></p>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
