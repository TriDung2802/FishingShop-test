<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/WEB-INF/views/common/shop-header.jsp">
    <jsp:param name="title" value="Bảng mẫu giao diện"/>
</jsp:include>

<div class="page-head">
    <h1>Bảng mẫu giao diện chung</h1>
    <span class="muted">Trang tham khảo cho cả nhóm. Có thể xóa file styleguide.jsp trước khi nộp.</span>
</div>

<section class="panel panel-wide">
    <h2>Nút (.btn)</h2>
    <a class="btn" href="#">Mặc định</a>
    <a class="btn btn-primary" href="#">btn-primary</a>
    <a class="btn btn-accent" href="#">btn-accent</a>
    <a class="btn btn-buy" href="#">btn-buy</a>
    <a class="btn btn-danger" href="#">btn-danger</a>
    <a class="btn btn-sm" href="#">btn-sm</a>
    <a class="btn disabled" href="#">disabled</a>
</section>

<section class="panel panel-wide">
    <h2>Thông báo (.alert) và nhãn trạng thái</h2>
    <div class="alert alert-success">alert-success: Đã lưu thành công.</div>
    <div class="alert alert-error">alert-error: Có lỗi xảy ra.</div>
    <p>
        <span class="badge badge-on">badge-on</span>
        <span class="badge badge-off">badge-off</span>
        <span class="status status-PENDING">Chờ xác nhận</span>
        <span class="status status-CONFIRMED">Đã xác nhận</span>
        <span class="status status-SHIPPING">Đang giao</span>
        <span class="status status-SUCCESS">Thành công</span>
        <span class="status status-CANCELLED">Đã hủy</span>
    </p>
</section>

<section class="panel panel-wide">
    <h2>Bảng (.table) và thanh lọc (.filter-bar)</h2>
    <form class="filter-bar" onsubmit="return false">
        <input type="text" placeholder="Tìm kiếm...">
        <select><option>Tất cả trạng thái</option></select>
        <button class="btn btn-primary">Lọc</button>
    </form>
    <table class="table">
        <thead><tr><th>Mã</th><th>Tên</th><th class="num">Giá</th><th>Trạng thái</th><th></th></tr></thead>
        <tbody>
        <tr><td>1</td><td>Cần câu carbon</td><td class="num">1.250.000 đ</td>
            <td><span class="badge badge-on">Đang bán</span></td>
            <td class="actions"><a class="btn btn-sm" href="#">Sửa</a> <a class="btn btn-sm btn-danger" href="#">Xóa</a></td></tr>
        <tr><td colspan="5" class="empty">Dòng trống: class "empty"</td></tr>
        </tbody>
    </table>
</section>

<section class="panel panel-wide">
    <h2>Form (.form), hai cột (.form-row)</h2>
    <form class="form" onsubmit="return false">
        <div class="form-row">
            <div><label>Họ tên <span class="req">*</span></label><input type="text" placeholder="Nguyễn Văn A"></div>
            <div><label>Số điện thoại</label><input type="tel" placeholder="09xxxxxxxx"></div>
        </div>
        <label>Email</label><input type="email" placeholder="a@example.com">
        <div class="field-error">field-error: Email không hợp lệ.</div>
        <div class="hint">hint: dòng gợi ý nhỏ dưới ô nhập.</div>
        <label>Ghi chú</label><textarea rows="2"></textarea>
        <div class="form-actions"><button class="btn btn-primary">Lưu</button><a class="btn" href="#">Hủy</a></div>
    </form>
</section>

<section class="panel panel-wide">
    <h2>Thẻ đăng nhập (.auth-card) - mẫu cho Dũng</h2>
    <div class="auth-wrap">
        <div class="auth-card">
            <h1>Đăng nhập</h1>
            <p class="auth-sub">Chào mừng bạn quay lại Fishing Shop</p>
            <form class="form" onsubmit="return false">
                <label>Email</label><input type="email">
                <label>Mật khẩu</label><input type="password">
                <button class="btn btn-primary btn-block">Đăng nhập</button>
            </form>
            <p class="auth-foot">Chưa có tài khoản? <a href="#">Đăng ký</a></p>
        </div>
    </div>
</section>

<section class="panel panel-wide">
    <h2>Khu vực tài khoản (.account-layout) và địa chỉ (.address-card) - mẫu cho Dũng</h2>
    <div class="account-layout">
        <nav class="account-nav">
            <a class="active" href="#">Địa chỉ giao hàng</a>
            <a href="#">Thông báo</a>
            <a href="#">Đơn hàng của tôi</a>
        </nav>
        <div>
            <div class="address-card default">
                <strong>Ngô Trần Ngân Đô</strong> <span class="badge badge-on">Mặc định</span><br>
                09xxxxxxxx<br>484 Lê Văn Việt, TP. Thủ Đức
                <div class="address-actions"><a class="btn btn-sm" href="#">Sửa</a><a class="btn btn-sm btn-danger" href="#">Xóa</a></div>
            </div>
            <div class="address-card">
                <strong>Nguyễn Văn B</strong><br>09xxxxxxxx<br>12 Nguyễn Huệ, Quận 1
                <div class="address-actions"><a class="btn btn-sm" href="#">Đặt mặc định</a><a class="btn btn-sm" href="#">Sửa</a><a class="btn btn-sm btn-danger" href="#">Xóa</a></div>
            </div>
        </div>
    </div>
</section>

<section class="panel panel-wide">
    <h2>Tab lọc (.tabs), tóm tắt (.summary-box), thông tin (.info-grid), hộp trống (.empty-box)</h2>
    <div class="tabs"><a class="active" href="#">Tất cả</a><a href="#">Chờ xác nhận</a><a href="#">Đang giao</a></div>
    <div class="info-grid">
        <div><span class="muted">Mã đơn</span><strong>DH000001</strong></div>
        <div><span class="muted">Ngày đặt</span><strong>19/10/2026</strong></div>
        <div><span class="muted">Tổng tiền</span><strong>1.250.000 đ</strong></div>
    </div>
    <div class="empty-box mt"><p>Giỏ hàng của bạn đang trống.</p><a class="btn btn-primary" href="#">Tiếp tục mua sắm</a></div>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
