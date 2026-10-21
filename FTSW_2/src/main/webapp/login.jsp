<%@page contentType = "text/html;charset=UTF-8" pageEncoding = "UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang ="vi">
<head>
    <meta charset ="UTF-8">
    <meta name ="viewport" content ="width = device-width, initial-scale = 1.0">
    <link rel ="stylesheet"
          href="${pageContext.request.contextPath}/css/login.css">
    <Title>Đăng nhập - Fishing Tackle Shop</Title>
</head>
<body class="login-page">
<div class ="login-container">
    <h1>Đăng nhập</h1>
    <p class ="description">Fishing Tackle Shop</p>
    <c:if test="${param.registered == '1'}">
        <p class="success-message">Đăng ký thành công. Vui lòng đăng nhập.</p>
    </c:if>
    <c:if test="${not empty message}">
        <p class="success-message"><c:out value="${message}"/></p>
    </c:if>
    <c:if test="${not empty error}">
        <p class ="error-message"><c:out value="${error}"/></p>
    </c:if>
    <form action = "${pageContext.request.contextPath}/login"
          method ="post">
        <input type="hidden" name="next" value="<c:out value='${param.next}'/>">
        <label for ="email">Email hoặc Username</label>
        <input type ="text" id="email" name="username"
               placeholder="Nhập email hoặc username"
               autocomplete ="username"
               required>
        <label for="password">Mật khẩu</label>
        <input type="password" id="password" name="password"
               placeholder="Nhập mật khẩu"
               autocomplete="current-password"
               required>
        <button type ="submit">Đăng nhập</button>
    </form>
    <div class="register-link">
        <a href="${pageContext.request.contextPath}/forgot-password.jsp">
            Quên mật khẩu?
        </a>
    </div>
    <div class ="register-link">
        Chưa có tài khoản
        <a href ="${pageContext.request.contextPath}/register">
            Đăng ký
        </a>
    </div>
</div>
</body>
</html>