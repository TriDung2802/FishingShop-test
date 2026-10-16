
const notifications = [
    {
        id: 1,
        type: "ORDER_CREATED",
        title: "Đặt hàng thành công",
        message: "Đơn hàng #ORD1004 của bạn đã được tạo thành công.",
        time: "2 phút trước",
        icon: "🛒",
        unread: true
    },
    {
        id: 2,
        type: "SHIPPING_UPDATE",
        title: "Cập nhật vận chuyển",
        message: "Đơn hàng #ORD1002 đang được giao đến bạn.",
        time: "15 phút trước",
        icon: "🚚",
        unread: true
    },
    {
        id: 3,
        type: "PAYMENT_SUCCESS",
        title: "Thanh toán thành công",
        message: "Thanh toán đơn hàng #ORD1001 đã hoàn tất.",
        time: "1 giờ trước",
        icon: "💳",
        unread: true
    },
    {
        id: 4,
        type: "ORDER_CANCELLED",
        title: "Đơn hàng đã bị hủy",
        message: "Đơn hàng #ORD0998 đã được hủy.",
        time: "Hôm qua",
        icon: "❌",
        unread: false
    }
];

// Chuyển nội dung thành text an toàn trước khi đưa vào HTML.
function escapeHTML(value) {
    return String(value).replace(/[&<>"']/g, function (char) {
        const entities = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        };
        return entities[char];
    });
}

function renderNotificationItem(item, compact = false) {
    return `
        <article class="notification-item ${item.unread ? "is-unread" : ""}">
            <div class="notification-icon">${escapeHTML(item.icon)}</div>

            <div class="notification-content">
                <div class="notification-item-heading">
                    <strong>${escapeHTML(item.title)}</strong>
                    ${item.unread ? '<span class="unread-dot"></span>' : ""}
                </div>

                <p>${escapeHTML(item.message)}</p>
                <span class="notification-time">${escapeHTML(item.time)}</span>

                ${compact ? "" : `
                    <span class="notification-type">
                        ${escapeHTML(item.type)}
                    </span>
                `}
            </div>
        </article>
    `;
}

document.addEventListener("DOMContentLoaded", function () {
    const bellButton = document.getElementById("notificationBell");
    const dropdown = document.getElementById("notificationDropdown");
    const latestList = document.getElementById("latestNotifications");
    const allList = document.getElementById("allNotifications");
    const badge = document.getElementById("notificationBadge");

    // Hiển thị số thông báo chưa đọc trên chuông.
    const unreadCount = notifications.filter(item => item.unread).length;

    if (badge) {
        badge.textContent = unreadCount > 9 ? "9+" : unreadCount;
        badge.hidden = unreadCount === 0;
    }

    // Dropdown trên trang chủ: tối đa 3 thông báo mới nhất.
    if (latestList) {
        latestList.innerHTML = notifications
            .slice(0, 3)
            .map(item => renderNotificationItem(item, true))
            .join("");
    }

    // Trang riêng: hiển thị tất cả 4 loại thông báo mẫu.
    if (allList) {
        allList.innerHTML = notifications
            .map(item => renderNotificationItem(item, false))
            .join("");
    }

    // Mở hoặc đóng dropdown khi nhấn chuông.
    if (bellButton && dropdown) {
        bellButton.addEventListener("click", function (event) {
            event.stopPropagation();

            const isOpen = dropdown.classList.toggle("is-open");
            bellButton.setAttribute("aria-expanded", String(isOpen));
        });

        // Nhấn bên ngoài khung sẽ đóng dropdown.
        document.addEventListener("click", function (event) {
            if (!dropdown.contains(event.target) &&
                !bellButton.contains(event.target)) {
                dropdown.classList.remove("is-open");
                bellButton.setAttribute("aria-expanded", "false");
            }
        });

        // Nhấn Escape để đóng dropdown.
        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                dropdown.classList.remove("is-open");
                bellButton.setAttribute("aria-expanded", "false");
            }
        });
    }
});
