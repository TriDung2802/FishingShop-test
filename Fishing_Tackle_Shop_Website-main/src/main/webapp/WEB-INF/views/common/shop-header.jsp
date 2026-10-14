<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${param.title}"/> - Fishing Shop</title>
    <link rel="stylesheet" href="${ctx}/css/style.css">
    <link rel="stylesheet" href="${ctx}/css/shop.css">
    <link rel="stylesheet" href="${ctx}/css/order.css">
    <link rel="stylesheet" href="${ctx}/css/payment.css">
    <script src="${ctx}/js/main.js" defer></script>
</head>
<body>
<header class="topbar">
    <a class="brand shop-brand" href="${ctx}/">Fishing Shop</a>
    <nav class="shop-nav">
        <a href="${ctx}/orders">Đơn hàng của tôi</a>
        <a href="${ctx}/cart" class="cart-link">Giỏ hàng
            <span class="cart-count">${empty sessionScope.cartCount ? 0 : sessionScope.cartCount}</span></a>
    </nav>
</header>
<main class="container">
