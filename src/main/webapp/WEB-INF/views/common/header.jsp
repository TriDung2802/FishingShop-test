<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="sp" value="${requestScope['jakarta.servlet.forward.servlet_path']}" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${param.title}"/> - Fishing Shop</title>
    <jsp:include page="/WEB-INF/views/common/assets.jsp"/>
</head>
<body class="admin">
<aside class="sidebar" id="sidebar">
    <div class="sidebar-top">
        <a class="sidebar-brand" href="${ctx}/admin/categories">🎣 Fishing Shop <small>Quản trị</small></a>
        <button type="button" class="nav-toggle" data-toggle="sidebar" aria-label="Mở menu" aria-expanded="false">☰</button>
    </div>

    <nav class="sidebar-nav">
        <jsp:include page="/WEB-INF/views/common/admin-menu.jsp"/>
    </nav>

    <a class="sidebar-link" href="${ctx}/home" target="_blank">Xem trang khách &nearr;</a>
</aside>
<main class="container">
