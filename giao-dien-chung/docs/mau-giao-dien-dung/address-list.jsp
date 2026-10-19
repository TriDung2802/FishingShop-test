<%-- MẪU CHO DŨNG: sổ địa chỉ giao hàng. Chép vào WEB-INF/views/customer/account/addresses.jsp,
     AddressServlet (/account/addresses) đặt request attribute "addresses" (danh sách Address).
     Thêm vào menu khách (shop-menu.jsp) hoặc user-menu.jsp một link tới /account/addresses. --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/views/common/shop-header.jsp">
    <jsp:param name="title" value="Địa chỉ giao hàng"/>
</jsp:include>

<div class="account-layout">
    <nav class="account-nav">
        <a class="active" href="${ctx}/account/addresses">Địa chỉ giao hàng</a>
        <a href="${ctx}/notification.jsp">Thông báo</a>
        <a href="${ctx}/orders">Đơn hàng của tôi</a>
    </nav>

    <section>
        <div class="page-head">
            <h1>Địa chỉ giao hàng</h1>
            <a class="btn btn-primary" href="${ctx}/account/addresses?action=new">+ Thêm địa chỉ</a>
        </div>

        <c:if test="${not empty sessionScope.flashMessage}">
            <div class="alert alert-${sessionScope.flashType}"><c:out value="${sessionScope.flashMessage}"/></div>
            <c:remove var="flashMessage" scope="session"/>
            <c:remove var="flashType" scope="session"/>
        </c:if>

        <c:forEach var="a" items="${addresses}">
            <div class="address-card ${a.defaultAddress ? 'default' : ''}">
                <strong><c:out value="${a.receiverName}"/></strong>
                <c:if test="${a.defaultAddress}"><span class="badge badge-on">Mặc định</span></c:if><br>
                <c:out value="${a.phone}"/><br>
                <c:out value="${a.addressLine}"/>
                <div class="address-actions">
                    <c:if test="${!a.defaultAddress}">
                        <form method="post" action="${ctx}/account/addresses" class="inline">
                            <input type="hidden" name="action" value="setDefault">
                            <input type="hidden" name="id" value="${a.id}">
                            <button type="submit" class="btn btn-sm">Đặt mặc định</button>
                        </form>
                    </c:if>
                    <a class="btn btn-sm" href="${ctx}/account/addresses?action=edit&id=${a.id}">Sửa</a>
                    <form method="post" action="${ctx}/account/addresses" class="inline" data-confirm="Xóa địa chỉ này?">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${a.id}">
                        <button type="submit" class="btn btn-sm btn-danger">Xóa</button>
                    </form>
                </div>
            </div>
        </c:forEach>
        <c:if test="${empty addresses}">
            <div class="empty-box"><p>Bạn chưa có địa chỉ nào.</p></div>
        </c:if>
    </section>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
